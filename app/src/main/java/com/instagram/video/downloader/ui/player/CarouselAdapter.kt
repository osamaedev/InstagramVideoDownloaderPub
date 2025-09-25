package com.instagram.video.downloader.ui.player

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.instagram.video.downloader.R
import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.common.VideoPlayerPair
import com.instagram.video.downloader.data.remote.instaApi.dto.collections.CollectionCarouselItem
import com.instagram.video.downloader.data.remote.instaApi.dto.explore.CarouselMediaItem
import com.instagram.video.downloader.databinding.CarouselPlayerImageViewItemBinding
import com.instagram.video.downloader.databinding.CarouselPlayerVideoViewItemBinding
import com.instagram.video.downloader.ui.base.CarouselPlayerBaseViewHolder
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import timber.log.Timber
import java.io.File
import com.instagram.video.downloader.data.local.room.entities.Media as LocalMedia

class CarouselAdapter :
    RecyclerView.Adapter<ViewHolder>() {

    companion object {
        const val TAG = "CarouselAdapter"
    }

    var carouselMediaItems = mutableListOf<CarouselPlayerUiModel>()
    val videoMediaPlayers = mutableListOf<VideoPlayerPair<MediaPlayer, Int>>()
    var currentSelectedItem = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            1 -> CarouselImageMediaItemViewHolder(
                CarouselPlayerImageViewItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> CarouselVideoMediaItemViewHolder(
                CarouselPlayerVideoViewItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (carouselMediaItems[position]) {
            is CarouselPlayerUiModel.ImageMediaItem -> 1
            is CarouselPlayerUiModel.VideoMediaItem -> 2
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        when (val uiModel = carouselMediaItems[position]) {
            is CarouselPlayerUiModel.ImageMediaItem -> {
                (holder as CarouselImageMediaItemViewHolder).onBind(position, this, uiModel.media)
            }

            is CarouselPlayerUiModel.VideoMediaItem -> {
                (holder as CarouselVideoMediaItemViewHolder).onBind(position, this, uiModel.media)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // NO-OP
    }

    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        val position = holder.bindingAdapterPosition
        Timber.tag(TAG).e("Position $position detached from window")
        if (holder is CarouselVideoMediaItemViewHolder) {
            val player =
                videoMediaPlayers.firstOrNull { it.adapterPosition == position }?.mediaPlayer
            player ?: return
            if (!player.isReleased) {
                player.pause()
                player.detachViews()
            }
        }
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        val position = holder.bindingAdapterPosition
        Timber.tag(TAG).e("Position $position attached to window")
        if (holder is CarouselVideoMediaItemViewHolder) {
            val player =
                videoMediaPlayers.firstOrNull { it.adapterPosition == position }?.mediaPlayer
            val vlcView = holder.itemView.findViewById<VLCVideoLayout>(R.id.view_vlc_layout)
            if (player?.vlcVout?.areViewsAttached() == false) {
                Timber.tag(TAG).v("Vlc out views are detached, attaching Views..")
                player.attachViews(vlcView, null, false, true)
            }
            if (player?.isReleased == false) {
                player.nativeSetTime(player.time, true)
                player.play()
                Timber.tag(TAG).i("Player play is called $position")
            }
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        val position = holder.bindingAdapterPosition
        Timber.tag(TAG).v("Item at $position was recycled")
        if (holder is CarouselVideoMediaItemViewHolder) {
            videoMediaPlayers.firstOrNull { it.adapterPosition == position }?.mediaPlayer?.release()
            videoMediaPlayers.firstOrNull { it.adapterPosition == position }?.mediaPlayer?.libVLC?.release()
            videoMediaPlayers.firstOrNull { it.adapterPosition == position }?.let { item ->
                videoMediaPlayers.remove(item)
            }
        }
    }

    override fun getItemCount() = carouselMediaItems.size


    inner class CarouselImageMediaItemViewHolder(private val imageItemBinding: CarouselPlayerImageViewItemBinding) :
        CarouselPlayerBaseViewHolder(imageItemBinding.root) {
        override fun onBind(
            position: Int,
            adapter: CarouselAdapter,
            media: CarouselPlayerDataModel
        ) {
            when (media) {
                is CarouselPlayerDataModel.ExploreMediaItem -> {
                    Glide
                        .with(imageItemBinding.root.context)
                        .load(media.media.imageVersions2?.candidates?.maxBy { it?.height!! }?.url!!)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_media_place_holder)
                        .into(imageItemBinding.postImage)
                }

                is CarouselPlayerDataModel.CollectionMediaItem -> {
                    Glide
                        .with(imageItemBinding.root.context)
                        .load(media.media.imageVersions2?.candidates?.maxBy { it?.height!! }?.url!!)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_media_place_holder)
                        .into(imageItemBinding.postImage)
                }

                is CarouselPlayerDataModel.DownloadedMediaItem -> {
                    Glide
                        .with(imageItemBinding.root.context)
                        .load(media.media.thumbnailUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_media_place_holder)
                        .into(imageItemBinding.postImage)
                }
            }
        }
    }

    inner class CarouselVideoMediaItemViewHolder(private val videoItemBinding: CarouselPlayerVideoViewItemBinding) :
        CarouselPlayerBaseViewHolder(videoItemBinding.root) {

        private fun loadExploreAndCollectionVideo(
            position: Int,
            mediaUri: Uri
        ) {
            videoItemBinding.player.isVisible = true
            videoItemBinding.fileStatus.isVisible = false
            val libVlc = LibVLC(VideoDownloaderApp.application, ArrayList<String>().apply {
//                if (BuildConfig.DEBUG)
//                    add("-vvv")
                add("--http-reconnect")
//                add("--network-caching=1000")
//                add("--http-caching==1000")
//                addOption(":avcodec-hw=dxva2")
//                add("--sout-keep")
//                add("--clock-jitter=1")
            })
            val mediaPlayer = MediaPlayer(libVlc)
            val ua = VideoDownloaderApp.application.getString(R.string.web_agent_1)
            libVlc.setUserAgent(ua, ua)
            Media(libVlc, mediaUri).apply {
                addOption(":http-user-agent=${ua}")
                mediaPlayer.media = this
                this.release()
            }
            mediaPlayer.videoScale = MediaPlayer.ScaleType.SURFACE_ORIGINAL
            mediaPlayer.attachViews(videoItemBinding.viewVlcLayout, null, false, true)
            val playerData = VideoPlayerPair(mediaPlayer, position)
            if (!videoMediaPlayers.map { it.adapterPosition }.contains(position)) {
                videoMediaPlayers.add(playerData)
            }
            if (videoItemBinding.root.isAttachedToWindow)
                mediaPlayer.play()
            else
                mediaPlayer.pause()
        }

        private fun loadDownloadedVideo(
            position: Int,
            media: CarouselPlayerDataModel.DownloadedMediaItem
        ) {
            val videoFile = File(media.media.fileLocation)
            if (videoFile.exists()) {
                videoItemBinding.player.isVisible = true
                videoItemBinding.fileStatus.isVisible = false
                val libVlc = LibVLC(VideoDownloaderApp.application, ArrayList<String>().apply {
                    add("--input-repeat=900")
                    add("--file-caching=1200")
//                    add("--sout-keep")
//                    add("--avcodec-hw=any")
//                    add("--no-video-title-show")
                })
                val mediaPlayer = MediaPlayer(libVlc)
                Media(libVlc, media.media.fileLocation).apply {
                    mediaPlayer.media = this
                    release()
                }
                mediaPlayer.videoScale = MediaPlayer.ScaleType.SURFACE_ORIGINAL
                mediaPlayer.attachViews(videoItemBinding.viewVlcLayout, null, false, true)

                val playerData = VideoPlayerPair(mediaPlayer, position)
                if (!videoMediaPlayers.map { it.adapterPosition }.contains(position)) {
                    videoMediaPlayers.add(playerData)
                }
                if (videoItemBinding.root.isAttachedToWindow)
                    mediaPlayer.play()
                else
                    mediaPlayer.pause()
            } else {
                videoItemBinding.player.isVisible = false
                videoItemBinding.fileStatus.isVisible = true
            }
        }

        override fun onBind(
            position: Int,
            adapter: CarouselAdapter,
            media: CarouselPlayerDataModel
        ) {
            videoItemBinding.viewVlcLayout.tag = "vlc_layout_${position}"
            when (media) {
                is CarouselPlayerDataModel.ExploreMediaItem -> {
                    loadExploreAndCollectionVideo(
                        position,
                        media.media.videoVersions?.first()?.url!!.toUri(),
                    )
                }

                is CarouselPlayerDataModel.CollectionMediaItem -> {
                    loadExploreAndCollectionVideo(
                        position,
                        media.media.videoVersions?.first()?.url!!.toUri(),
                    )
                }

                is CarouselPlayerDataModel.DownloadedMediaItem -> {
                    loadDownloadedVideo(position, media)
                }
            }
        }
    }
}

sealed class CarouselPlayerUiModel(val mediaId: String) {
    data class VideoMediaItem(val media: CarouselPlayerDataModel) :
        CarouselPlayerUiModel(media.mediaId)

    data class ImageMediaItem(val media: CarouselPlayerDataModel) :
        CarouselPlayerUiModel(media.mediaId)
}

sealed class CarouselPlayerDataModel(val mediaType: Int, val mediaId: String) {
    data class ExploreMediaItem(val media: CarouselMediaItem) :
        CarouselPlayerDataModel(media.mediaType!!, media.id!!)

    data class CollectionMediaItem(val media: CollectionCarouselItem) :
        CarouselPlayerDataModel(media.mediaType!!, media.id!!)

    data class DownloadedMediaItem(val media: LocalMedia) :
        CarouselPlayerDataModel(if (media.type == "image") 1 else 2, media.instagramId)
}