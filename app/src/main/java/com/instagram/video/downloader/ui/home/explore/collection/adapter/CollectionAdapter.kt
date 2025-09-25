package com.instagram.video.downloader.ui.home.explore.collection.adapter

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.instagram.video.downloader.R
import com.instagram.video.downloader.common.lockView
import com.instagram.video.downloader.data.remote.instaApi.dto.collections.CollectionMedia
import com.instagram.video.downloader.databinding.BottomLoadingStateViewBinding
import com.instagram.video.downloader.databinding.CollectionMediaItemBinding
import com.instagram.video.downloader.ui.base.NativeAdViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt

class CollectionAdapter :
    PagingDataAdapter<CollectionUiModel, ViewHolder>(CollectionUiModeDiffCallBack()) {

    var collectionListener: OnCollectionListener? = null


    val selectedItems = MutableStateFlow<MutableList<Int>>(mutableListOf())
    val selectedItemsCount = MutableStateFlow(0)
    var isMultiSelectMode = MutableStateFlow(false)


    val selectionScope = CoroutineScope(Dispatchers.Main).launch {
        isMultiSelectMode
            .collect {
                setItemSelectionMode()
            }
    }


    fun setItemSelectionMode() {
        snapshot().items.forEachIndexed { index, item ->
            (item as CollectionUiModel.MediaItem).collectionItem.isCheckable =
                isMultiSelectMode.value
            notifyItemChanged(index)
        }
    }


    fun disableSelection() {
        snapshot().items.filter { (it as CollectionUiModel.MediaItem).collectionItem.isChecked }
            .let { list ->
                if (list.isNotEmpty()) {
                    list.forEach { item ->
                        (item as CollectionUiModel.MediaItem).collectionItem.isChecked = false
                    }
                }
            }
        selectedItems.value.forEach { index ->
            CoroutineScope(Dispatchers.Main).launch {
                notifyItemChanged(index)
            }
        }
        selectedItems.value.clear()
        selectedItemsCount.value = 0
        isMultiSelectMode.value = false
    }

    fun enableSelection() {
        isMultiSelectMode.value = true
    }


    fun addItemToSelection(position: Int) {
        selectedItems.update {
            selectedItems.value.apply { add(position) }
            it
        }
        selectedItemsCount.value = selectedItems.value.size
        notifyItemChanged(position)
    }

    fun removeItemFromSelection(position: Int) {
        selectedItems.update {
            selectedItems.value.apply { remove(position) }
            it
        }
        selectedItemsCount.value = selectedItems.value.size
        notifyItemChanged(position)
    }

    fun removeLastItemFromSelection() {
        val lastItemSelectedPosition = selectedItems.value[selectedItems.value.lastIndex]
        (snapshot()[lastItemSelectedPosition] as CollectionUiModel.MediaItem).collectionItem.isChecked =
            false
        CoroutineScope(Dispatchers.Main).launch {
            notifyItemChanged(lastItemSelectedPosition)
        }
        selectedItems.value.removeAt(selectedItems.value.lastIndex)
        selectedItemsCount.value = selectedItems.value.size
    }

    fun isItemSelected(position: Int) = selectedItems.value.contains(position)

    fun isLastSelectedItem(position: Int) =
        isItemSelected(position) && selectedItems.value.size == 1

    fun getAllSelectedItems(): List<CollectionUiModel.MediaItem> {
        val items = arrayListOf<CollectionUiModel.MediaItem>()
        for (position in selectedItems.value) {
            items.add(snapshot().items[position] as CollectionUiModel.MediaItem)
        }
        return items
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uiModel = getItem(position)
        uiModel?.let {
            when (it) {
                is CollectionUiModel.MediaItem -> (holder as CollectionMediaViewHolder).onBind(
                    position,
                    this,
                    it.collectionItem
                )

                is CollectionUiModel.AdItem -> (holder as NativeAdViewHolder).onBind(it.stringValue)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == R.layout.collection_media_item) CollectionMediaItemViewHolder(
            CollectionMediaItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        ) else NativeAdViewHolder.create(parent)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CollectionUiModel.MediaItem -> R.layout.collection_media_item
            is CollectionUiModel.AdItem -> R.layout.explore_collection_native_ad_container
            null -> throw UnsupportedOperationException("Unknown view")
        }
    }

    inner class CollectionMediaItemViewHolder(private val collectionMediaItemBinding: CollectionMediaItemBinding) :
        CollectionMediaViewHolder(collectionMediaItemBinding.root) {
        @SuppressLint("SetTextI18n")
        override fun onBind(
            position: Int,
            adapter: CollectionAdapter,
            collectionItem: CollectionMedia
        ) {
            collectionItem.productType?.let { productType ->
                when (productType) {
                    "clips" -> {
                        collectionMediaItemBinding.mediaIconType.setImageResource(R.drawable.ic_instagram_video)
                    }

                    "feed" -> {
                        collectionMediaItemBinding.mediaIconType.visibility = View.GONE
                    }

                    "carousel_container" -> {
                        collectionMediaItemBinding.mediaIconType.setImageResource(R.drawable.ic_instagram_carousel)
                    }
                }
            }
            try {
                if (collectionItem.mediaType == 8) {
                    Glide.with(collectionMediaItemBinding.root.context)
                        .load(collectionItem.carouselMedias?.first()?.imageVersions2?.candidates?.maxBy { it?.height!! }?.url!!)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_media_place_holder)
                        .into(collectionMediaItemBinding.mediaPicture)
                } else {
                    Glide.with(collectionMediaItemBinding.root.context)
                        .load(collectionItem.imageVersions2?.candidates?.maxBy { it?.height!! }?.url!!)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_media_place_holder)
                        .into(collectionMediaItemBinding.mediaPicture)
                }
            } catch (e: Exception) {
                Timber.tag("CollectionAdapter").e(e)
            }

            when {
                collectionItem.isCheckable && !collectionItem.isDownloaded -> {
                    collectionMediaItemBinding.selectionBox.isVisible = true
                    collectionMediaItemBinding.mediaIconType.isVisible = false
                }

                else -> {
                    collectionMediaItemBinding.selectionBox.isVisible = false
                    collectionMediaItemBinding.mediaIconType.isVisible = true
                }
            }

            when {
                collectionItem.isChecked -> {
                    collectionMediaItemBinding.selectionBox.jumpDrawablesToCurrentState()
                    collectionMediaItemBinding.selectionBox.isChecked = true
                }

                else -> {
                    collectionMediaItemBinding.selectionBox.jumpDrawablesToCurrentState()
                    collectionMediaItemBinding.selectionBox.isChecked = false
                }
            }


            when {
                collectionItem.isDownloading && collectionItem.downloadProgress == 0.0 && !collectionItem.isDownloaded -> {
                    setIsRecyclable(false)
                    collectionMediaItemBinding.progressContainer.isVisible = true
                    collectionMediaItemBinding.effect.isVisible = true
                    collectionMediaItemBinding.progressBar.progress = 0.0f
                    collectionMediaItemBinding.progressValue.text = "0%"
                    collectionMediaItemBinding.icDownloaded.isVisible = false
                }

                collectionItem.isDownloaded && !collectionItem.isDownloading -> {
                    setIsRecyclable(false)
                    collectionMediaItemBinding.effect.isVisible = true
                    collectionMediaItemBinding.progressContainer.isVisible = false
                    collectionMediaItemBinding.icDownloaded.isVisible = true
                }

                !collectionItem.isDownloading -> {
                    collectionMediaItemBinding.effect.isVisible = false
                    collectionMediaItemBinding.icDownloaded.isVisible = false
                    collectionMediaItemBinding.progressContainer.isVisible = false
                }

                collectionItem.isDownloading && collectionItem.downloadProgress <= 100.0 && collectionItem.downloadProgress > 0.0 -> {
                    setIsRecyclable(false)
                    collectionMediaItemBinding.progressContainer.isVisible = true
                    collectionMediaItemBinding.effect.isVisible = true
                    collectionMediaItemBinding.progressBar.progress =
                        collectionItem.downloadProgress.toFloat()
                    val progress = "${collectionItem.downloadProgress.roundToInt()}%"
                    collectionMediaItemBinding.progressValue.text = progress
                }
            }



            collectionMediaItemBinding.root.setOnClickListener {
                lockView(collectionMediaItemBinding.root, Handler(Looper.getMainLooper()))
                if (adapter.isMultiSelectMode.value) {
                    if (adapter.isLastSelectedItem(position)) {
                        adapter.disableSelection()
                        return@setOnClickListener
                    }
                    if (!adapter.isItemSelected(position)) {
                        collectionItem.isChecked = true
                        adapter.addItemToSelection(position)
                    } else {
                        collectionItem.isChecked = false
                        adapter.removeItemFromSelection(position)
                    }
                } else {
                    collectionListener?.onCollectionClick(collectionItem)
                }
            }

            collectionMediaItemBinding.root.setOnLongClickListener {
                if (!adapter.isMultiSelectMode.value && !collectionItem.isDownloaded && !collectionItem.isDownloading) {
                    adapter.enableSelection()
                    collectionItem.isChecked = true
                    adapter.addItemToSelection(position)
                }
                return@setOnLongClickListener true
            }

        }
    }


    class CollectionUiModeDiffCallBack : DiffUtil.ItemCallback<CollectionUiModel>() {
        override fun areItemsTheSame(
            oldItem: CollectionUiModel,
            newItem: CollectionUiModel
        ): Boolean {
            return (oldItem is CollectionUiModel.MediaItem && newItem is CollectionUiModel.MediaItem && oldItem.collectionItem.id == newItem.collectionItem.id)
        }

        override fun areContentsTheSame(
            oldItem: CollectionUiModel,
            newItem: CollectionUiModel
        ): Boolean {
            return oldItem == newItem
        }
    }
}

abstract class CollectionMediaViewHolder(private val viewItem: View) : ViewHolder(viewItem) {
    abstract fun onBind(position: Int, adapter: CollectionAdapter, collectionItem: CollectionMedia)
}

sealed class CollectionUiModel {
    data class MediaItem(val collectionItem: CollectionMedia, val index: Int) : CollectionUiModel()
    data class AdItem(val stringValue: String) : CollectionUiModel()
}

class CollectionLoadingViewHolder(
    private val binding: BottomLoadingStateViewBinding
) : ViewHolder(binding.root) {

    fun onBind(loadState: LoadState) {
        binding.progressBar.isVisible = loadState is LoadState.Loading
    }

    companion object {
        fun create(parent: ViewGroup): CollectionLoadingViewHolder {
            return CollectionLoadingViewHolder(
                BottomLoadingStateViewBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    )
                )
            )
        }
    }
}

interface OnCollectionListener {
    fun onCollectionClick(media: CollectionMedia)
}