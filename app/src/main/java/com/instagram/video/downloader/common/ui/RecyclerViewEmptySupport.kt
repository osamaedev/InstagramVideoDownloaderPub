package com.instagram.video.downloader.common.ui

import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.recyclerview.widget.RecyclerView
import com.instagram.video.downloader.R
import com.instagram.video.downloader.common.ui.compose.InstaDownloaderTheme
import com.instagram.video.downloader.common.ui.extensions.inflate
import com.instagram.video.downloader.common.ui.utils.setMarkdown
import com.instagram.video.downloader.common.ui.views.InstaDownloaderCircularProgressView
import com.instagram.video.downloader.databinding.RecyclerViewEmptyItemBinding
import com.instagram.video.downloader.databinding.RecyclerViewFailedItemBinding

data class EmptyRecyclerItemView(
    var title: String,
    var text: String? = null,
    var iconResource: Int? = null,
    var onButtonTap: (() -> Unit)? = null,
)


enum class RecyclerViewState {
    LOADING,
    EMPTY,
    DISPLAYING_DATA,
    FAILED,
}

class FailedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val binding = RecyclerViewFailedItemBinding.bind(itemView)

    fun bind(onRefresh: (() -> Unit)?) {
        if (onRefresh != null) {
            binding.refreshButton.visibility = View.VISIBLE
            binding.refreshButton.setOnClickListener { onRefresh() }
        } else {
            binding.refreshButton.visibility = View.GONE
        }
    }
}

class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val binding = RecyclerViewEmptyItemBinding.bind(itemView)

    fun bind(emptyItem: EmptyRecyclerItemView?) {
        emptyItem?.iconResource?.let { binding.emptyIconView.setImageResource(it) }
        binding.emptyViewTitle.text = emptyItem?.title
        binding.emptyViewDescription.setMarkdown(emptyItem?.text)
        if (emptyItem?.onButtonTap != null) {
            binding.emptyView.setOnClickListener { emptyItem.onButtonTap?.invoke() }
        }
    }
}

class RecyclerViewStateAdapter(val showLoadingAsEmpty: Boolean = false) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onRefresh: (() -> Unit)? = null
    var emptyViewBuilder: (() -> View)? = null
    var emptyItem: EmptyRecyclerItemView? = null
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    var state: RecyclerViewState = RecyclerViewState.LOADING
        set(value) {
            field = value
            notifyItemChanged(0)
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view = parent.inflate(R.layout.recycler_view_loading_item)
                val animation1 = AlphaAnimation(0.0f, 1.0f)
                animation1.duration = 300
                animation1.startOffset = 500
                animation1.fillAfter = true
                view.findViewById<ComposeView>(R.id.compose_view).startAnimation(animation1)
                view.findViewById<ComposeView>(R.id.compose_view).setContent {
                    InstaDownloaderTheme {
                        InstaDownloaderCircularProgressView(Modifier.size(40.dp))
                    }
                }
                object : RecyclerView.ViewHolder(view) {}
            }

            1 -> FailedViewHolder(parent.inflate(R.layout.recycler_view_failed_item))
            else ->
                if (emptyViewBuilder != null) {
                    HolderViewHolder(emptyViewBuilder?.invoke() ?: View(parent.context))
                } else {
                    EmptyViewHolder(parent.inflate(R.layout.recycler_view_empty_item))
                }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        if (holder is EmptyViewHolder) {
            holder.bind(emptyItem)
        } else if (holder is FailedViewHolder) {
            holder.bind(onRefresh)
        }
        (holder as? EmptyViewHolder)?.bind(emptyItem)
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            state == RecyclerViewState.LOADING && !showLoadingAsEmpty -> 0
            state == RecyclerViewState.FAILED -> 1
            else -> 2
        }
    }
}

class HolderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)