package com.instagram.video.downloader.ui.home.downloads

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.instagram.video.downloader.R
import com.instagram.video.downloader.data.local.room.entities.PostWithMediaUserAndMedia
import com.instagram.video.downloader.databinding.LocalPostItemBinding
import com.instagram.video.downloader.ui.base.BaseViewHolder

class DownloadedPostsAdapter : RecyclerView.Adapter<BaseViewHolder>() {

    var posts: List<PostWithMediaUserAndMedia> = arrayListOf()
    var postItemListener: PostItemListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return DownloadedPostItem(
            LocalPostItemBinding.inflate(
                LayoutInflater.from(parent.context)
            )
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.onBind(position)
    }

    override fun getItemCount() = posts.size


    inner class DownloadedPostItem(private val localPostItemBinding: LocalPostItemBinding) :
        BaseViewHolder(localPostItemBinding.root) {
        override fun onBind(position: Int) {
            posts[position].let { post ->
                when (post.post.productType) {
                    "clips" -> localPostItemBinding.mediaIconType.setImageResource(R.drawable.ic_instagram_video)
                    "feed", "audio" -> localPostItemBinding.mediaIconType.isVisible = false
                    "carousel_container" -> localPostItemBinding.mediaIconType.setImageResource(R.drawable.ic_instagram_carousel)
                }

                Glide.with(localPostItemBinding.root.context)
                    .load(post.post.thumbnailUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(if (post.post.productType == "audio") R.drawable.audio_place_holder else R.drawable.ic_media_place_holder)
                    .into(localPostItemBinding.mediaPicture)

                localPostItemBinding.root.setOnClickListener {
                    postItemListener?.onPostItemClick(
                        post
                    )
                }

                localPostItemBinding.more.setOnClickListener {
                    postItemListener?.onPostMoreClick(
                        post
                    )
                }
            }
        }
    }
}

interface PostItemListener {
    fun onPostItemClick(
        post: PostWithMediaUserAndMedia,
    )

    fun onPostMoreClick(
        post: PostWithMediaUserAndMedia,
    )
}