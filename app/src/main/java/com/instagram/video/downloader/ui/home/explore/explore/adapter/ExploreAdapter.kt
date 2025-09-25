package com.instagram.video.downloader.ui.home.explore.explore.adapter

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
import com.instagram.video.downloader.data.remote.instaApi.dto.explore.Media
import com.instagram.video.downloader.databinding.BottomLoadingStateViewBinding
import com.instagram.video.downloader.databinding.ExploreMediaItemBinding
import com.instagram.video.downloader.ui.base.NativeAdViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class ExploreAdapter :
    PagingDataAdapter<ExploreUiModel, ViewHolder>(ExploreUiModelDiffCallBack()) {


    var exploreItemListener: ExploreItemListener? = null

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
            (item as ExploreUiModel.MediaItem).media.isCheckable = isMultiSelectMode.value
            notifyItemChanged(index)
        }
    }

    fun disableSelection() {
        snapshot().items.filter { (it as ExploreUiModel.MediaItem).media.isChecked }.let { list ->
            if (list.isNotEmpty()) {
                list.forEach { item ->
                    (item as ExploreUiModel.MediaItem).media.isChecked = false
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
        (snapshot()[lastItemSelectedPosition] as ExploreUiModel.MediaItem).media.isChecked = false
        CoroutineScope(Dispatchers.Main).launch {
            notifyItemChanged(lastItemSelectedPosition)
        }
        selectedItems.value.removeAt(selectedItems.value.lastIndex)
        selectedItemsCount.value = selectedItems.value.size
    }

    fun isItemSelected(position: Int) = selectedItems.value.contains(position)

    fun isLastSelectedItem(position: Int) =
        isItemSelected(position) && selectedItems.value.size == 1

    fun selectAll() {
        for (i in 0 until itemCount) {
            if (!isItemSelected(i)) {
                addItemToSelection(i)
            }
        }
        selectedItemsCount.value = selectedItems.value.size
    }

    fun getAllSelectedItems(): List<ExploreUiModel.MediaItem> {
        val items = arrayListOf<ExploreUiModel.MediaItem>()
        for (position in selectedItems.value) {
            items.add(snapshot().items[position] as ExploreUiModel.MediaItem)
        }
        return items
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uiModel = getItem(position)
        uiModel?.let {
            when (it) {
                is ExploreUiModel.MediaItem -> (holder as ExploreMediaViewHolder).onBind(
                    position,
                    this,
                    it.media
                )

                is ExploreUiModel.AdItem -> (holder as NativeAdViewHolder).onBind(it.stringValue)
            }
        }
    }

//    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
//        when {
//            payloads.isEmpty() -> onBindViewHolder(holder, position)
//            else -> (holder as ExploreMediaItemViewHolder).bindProgress(payloads[0] as Double)
//        }
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == R.layout.explore_media_item) ExploreMediaItemViewHolder(
            exploreMediaItemBinding = ExploreMediaItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        ) else NativeAdViewHolder.create(parent)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ExploreUiModel.MediaItem -> R.layout.explore_media_item
            is ExploreUiModel.AdItem -> R.layout.explore_collection_native_ad_container
            null -> throw UnsupportedOperationException("Unknown view")
        }
    }

    inner class ExploreMediaItemViewHolder(private val exploreMediaItemBinding: ExploreMediaItemBinding) :
        ExploreMediaViewHolder(exploreMediaItemBinding.root) {


        @SuppressLint("SetTextI18n")
        override fun onBind(position: Int, adapter: ExploreAdapter, media: Media) {
            media.productType?.let { productType ->
                when (productType) {
                    "clips" -> {
                        exploreMediaItemBinding.mediaIconType.setImageResource(R.drawable.ic_instagram_video)
                    }

                    "feed" -> {
                        exploreMediaItemBinding.mediaIconType.visibility = View.GONE
                    }

                    "carousel_container" -> {
                        exploreMediaItemBinding.mediaIconType.setImageResource(R.drawable.ic_instagram_carousel)
                    }
                }
            }
            Glide.with(exploreMediaItemBinding.root.context)
                .load(media.imageVersions2?.candidates?.maxBy { it?.height!! }?.url!!)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_media_place_holder)
                .into(exploreMediaItemBinding.mediaPicture)

            when {
                media.isCheckable && !media.isDownloaded -> {
                    exploreMediaItemBinding.selectionBox.isVisible = true
                    exploreMediaItemBinding.mediaIconType.isVisible = false
                }

                else -> {
                    exploreMediaItemBinding.selectionBox.isVisible = false
                    exploreMediaItemBinding.mediaIconType.isVisible = true
                }
            }

            when {
                media.isChecked -> {
                    exploreMediaItemBinding.selectionBox.jumpDrawablesToCurrentState()
                    exploreMediaItemBinding.selectionBox.isChecked = true
                }

                else -> {
                    exploreMediaItemBinding.selectionBox.jumpDrawablesToCurrentState()
                    exploreMediaItemBinding.selectionBox.isChecked = false
                }
            }

            when {
                media.isDownloading && media.downloadProgress == 0.0 && !media.isDownloaded -> {
                    setIsRecyclable(false)
                    exploreMediaItemBinding.progressContainer.isVisible = true
                    exploreMediaItemBinding.effect.isVisible = true
                    exploreMediaItemBinding.progressBar.progress = 0.0f
                    exploreMediaItemBinding.progressValue.text = "0%"
                    exploreMediaItemBinding.icDownloaded.isVisible = false
                }

                media.isDownloaded && !media.isDownloading -> {
                    setIsRecyclable(false)
                    exploreMediaItemBinding.effect.isVisible = true
                    exploreMediaItemBinding.progressContainer.isVisible = false
                    exploreMediaItemBinding.icDownloaded.isVisible = true
                }

                !media.isDownloading -> {
                    exploreMediaItemBinding.effect.isVisible = false
                    exploreMediaItemBinding.icDownloaded.isVisible = false
                    exploreMediaItemBinding.progressContainer.isVisible = false
                }

                media.isDownloading && media.downloadProgress <= 100.0 && media.downloadProgress > 0.0 -> {
                    setIsRecyclable(false)
                    exploreMediaItemBinding.progressContainer.isVisible = true
                    exploreMediaItemBinding.effect.isVisible = true
                    exploreMediaItemBinding.progressBar.progress = media.downloadProgress.toFloat()
                    val progress = "${media.downloadProgress.roundToInt()}%"
                    exploreMediaItemBinding.progressValue.text = progress
                }
            }

            exploreMediaItemBinding.root.setOnClickListener {
                lockView(exploreMediaItemBinding.root, Handler(Looper.getMainLooper()))
                if (adapter.isMultiSelectMode.value && !media.isDownloaded) {
                    if (adapter.isLastSelectedItem(position)) {
                        adapter.disableSelection()
                        return@setOnClickListener
                    }
                    if (!adapter.isItemSelected(position)) {
                        media.isChecked = true
                        adapter.addItemToSelection(position)
                    } else {
                        media.isChecked = false
                        adapter.removeItemFromSelection(position)
                    }
                } else {
                    exploreItemListener?.onExploreItemClick(media)
                }
            }

            exploreMediaItemBinding.root.setOnLongClickListener {
                if (!adapter.isMultiSelectMode.value && !media.isDownloaded && !media.isDownloading) {
                    adapter.enableSelection()
                    media.isChecked = true
                    adapter.addItemToSelection(position)
                }
                return@setOnLongClickListener true
            }
        }

        internal fun bindProgress(progress: Double) {
            exploreMediaItemBinding.progressContainer.isVisible = true
            exploreMediaItemBinding.progressBar.progress = progress.toFloat()
            val intProgress = "${progress.roundToInt()}%"
            exploreMediaItemBinding.progressValue.text = intProgress
        }
    }

    class ExploreUiModelDiffCallBack : DiffUtil.ItemCallback<ExploreUiModel>() {
        override fun areItemsTheSame(oldItem: ExploreUiModel, newItem: ExploreUiModel): Boolean {
            return (oldItem as ExploreUiModel.MediaItem).media.id == (newItem as ExploreUiModel.MediaItem).media.id
        }

        override fun areContentsTheSame(oldItem: ExploreUiModel, newItem: ExploreUiModel): Boolean {
            return oldItem as ExploreUiModel.MediaItem == newItem as ExploreUiModel.MediaItem
        }

//        override fun getChangePayload(
//            oldItem: ExploreUiModel,
//            newItem: ExploreUiModel
//        ): Any? {
//            return when {
//                (oldItem as ExploreUiModel.MediaItem).media.downloadProgress != (newItem as ExploreUiModel.MediaItem).media.downloadProgress -> {
//                    ExploreUiChangePayload.Progress(newItem.media.downloadProgress)
//                }
//
//                else -> super.getChangePayload(oldItem, newItem)
//            }
//        }
    }
}

sealed interface ExploreUiChangePayload {
    data class Progress(val progress: Double) : ExploreUiChangePayload
}

sealed class ExploreUiModel {
    data class MediaItem(val media: Media, val index: Int) : ExploreUiModel()
    data class AdItem(val stringValue: String) :
        ExploreUiModel()           // change the string the ad type
}

class ExploreLoadingViewHolder(
    private val binding: BottomLoadingStateViewBinding
) : ViewHolder(binding.root) {

    fun onBind(loadState: LoadState) {
        binding.progressBar.isVisible = loadState is LoadState.Loading
    }

    companion object {
        fun create(parent: ViewGroup): ExploreLoadingViewHolder {
            return ExploreLoadingViewHolder(
                BottomLoadingStateViewBinding.inflate(LayoutInflater.from(parent.context))
            )
        }
    }
}

abstract class ExploreMediaViewHolder(private val viewItem: View) :
    ViewHolder(viewItem) {
    abstract fun onBind(position: Int, adapter: ExploreAdapter, media: Media)
}

interface ExploreItemListener {
    fun onExploreItemClick(media: Media)
}