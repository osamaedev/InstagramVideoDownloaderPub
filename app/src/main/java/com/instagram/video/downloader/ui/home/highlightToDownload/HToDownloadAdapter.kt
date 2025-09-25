package com.instagram.video.downloader.ui.home.highlightToDownload

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.instagram.video.downloader.data.remote.api.dto.HighlightMedia
import com.instagram.video.downloader.databinding.HighlightToDownloadItemBinding
import com.instagram.video.downloader.ui.base.BaseViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HToDownloadAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var highlights: List<HighlightMedia> = arrayListOf()
    val selectedItems = MutableStateFlow<ArrayList<Int>>(arrayListOf())
    var isSelectAll = MutableStateFlow(false)

    fun addItemToSelection(position: Int): Boolean {
        val result = selectedItems.value.add(position)
        return result
    }

    fun removeItemFromSelection(position: Int): Boolean {
        val result = selectedItems.value.remove(position)
        return result
    }

    fun isItemSelected(position: Int) = selectedItems.value.contains(position)

    fun selectAll() {
        isSelectAll.value = true
        for (i in 0 until itemCount) {
            if (!isItemSelected(i)) {
                addItemToSelection(i)
            }
        }
    }

    fun unSelectAll() {
        isSelectAll.value = false
        for (i in 0 until itemCount) {
            if (isItemSelected(i)) {
                removeItemFromSelection(i)
            }
        }
    }

    fun getAllSelectedItems(): List<HighlightMedia> {
        val items = arrayListOf<HighlightMedia>()
        for (position in selectedItems.value) {
            items.add(highlights[position])
        }
        return items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return HToDownloadItemViewHolder(
            HighlightToDownloadItemBinding.inflate(LayoutInflater.from(parent.context)), this
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount() = highlights.size

    override fun getItemId(position: Int) = position.toLong()


    inner class HToDownloadItemViewHolder(
        private val highlightItemBinding: HighlightToDownloadItemBinding,
        private val adapter: HToDownloadAdapter
    ) :
        BaseViewHolder(highlightItemBinding.root) {
        override fun onBind(position: Int) {
            highlights[position].let { media ->
                Glide.with(highlightItemBinding.root.context)
                    .load(media.thumbnailUrl!!)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(highlightItemBinding.mediaPicture)

                highlightItemBinding.root.setOnClickListener {
                    highlightItemBinding.selectionBox.isChecked =
                        !highlightItemBinding.selectionBox.isChecked
                }

                highlightItemBinding.selectionBox.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (buttonView.isPressed) {
                        if (isChecked) adapter.addItemToSelection(position) else adapter.removeItemFromSelection(
                            position
                        )
                    }
                }

                CoroutineScope(Dispatchers.Main).launch {
                    adapter.isSelectAll.collect {
                        highlightItemBinding.selectionBox.isChecked = it
                    }
                }
            }
        }
    }
}