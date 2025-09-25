package com.instagram.video.downloader.ui.player

import android.annotation.SuppressLint
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MediaAspectRatio
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.instagram.video.downloader.R
import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.common.AdmobNativeAdControllerPair
import com.instagram.video.downloader.common.AudioJobPair
import com.instagram.video.downloader.common.AudioModel
import com.instagram.video.downloader.common.CarouselAdapterPair
import com.instagram.video.downloader.common.CarouselCallbackPair
import com.instagram.video.downloader.common.VideoPlayerPair
import com.instagram.video.downloader.common.formatAudioTime
import com.instagram.video.downloader.common.lockView
import com.instagram.video.downloader.data.local.room.entities.PostWithMediaUserAndMedia
import com.instagram.video.downloader.data.remote.instaApi.dto.collections.CollectionMedia
import com.instagram.video.downloader.data.remote.instaApi.dto.explore.Media
import com.instagram.video.downloader.databinding.PlayerAudioItemViewBinding
import com.instagram.video.downloader.databinding.PlayerCarouselItemViewBinding
import com.instagram.video.downloader.databinding.PlayerImageViewItemBinding
import com.instagram.video.downloader.databinding.PlayerNativeAdItemBinding
import com.instagram.video.downloader.databinding.PlayerVideoItemViewBinding
import com.instagram.video.downloader.ui.base.PlayerBaseNativeAdViewHolder
import com.instagram.video.downloader.ui.base.PlayerBaseViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.util.VLCVideoLayout
import timber.log.Timber
import java.io.File
import java.io.Serializable
import kotlin.math.roundToInt
import org.videolan.libvlc.Media as VlcMedia
import org.videolan.libvlc.MediaPlayer as VlcMediaPlayer

class PlayerAdapter : RecyclerView.Adapter<ViewHolder>() {

    companion object {
        const val TAG = "PlayerAdapter"
    }

    data class DownloadStatus(
        val progress: Double,               // -1.0 completed, -2.0 start the new file
        val position: Int,
    )

    data class CarouselSelectedPosition(
        val adapterPosition: Int,
        val selectedPosition: Int,
    )

    data class VideoPlayerEvent(
        val event: Int,
        val currentProgress: Long = 0L,
        val maxLength: Long = 0L,
    )

    data class AudioPlayerEvent(
        val currentProgress: Int = 0,
        val maxLength: Int = 0,
    )

    val isWindowHasFocus = MutableStateFlow(false)
    private val carouselPosition = MutableStateFlow<CarouselSelectedPosition?>(null)

    val itemDownloadingInfo = MutableSharedFlow<PlayerItemDownloadingInfo?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    var posts = mutableListOf<PlayerUiModel>()

    // region [Audio]

    var audioMediaPlayers = mutableListOf<AudioModel<MediaPlayer, Boolean, Boolean, Int>>()
    val audioJobs = mutableListOf<AudioJobPair<Int, Job>>()

    // endregion

    // region [Video]

    val videoMediaPlayers = mutableListOf<VideoPlayerPair<VlcMediaPlayer, Int>>()

    // endregion

    // region [Carousel]

    val carouselCallbacks =
        mutableListOf<CarouselCallbackPair<ViewPager2.OnPageChangeCallback, Int>>()
    val carouselAdapters = mutableListOf<CarouselAdapterPair<CarouselAdapter, Int>>()

    // endregion

    // region [Admob Native Player]

    val admobAdmobVideoControllers =
        mutableListOf<AdmobNativeAdControllerPair<VideoController, Int>>()

    // endregion

    fun startProgressLoop(position: Int) {
        var currentJob = audioJobs.firstOrNull { it.position == position }
        currentJob?.job?.cancel()

        val job = CoroutineScope(Dispatchers.Main).launch {
            val playerData =
                audioMediaPlayers.firstOrNull { it.adapterPosition == position }
            while (this.isActive) {
                if (playerData?.isPrepared == true || playerData?.isPaused == false) {
                    val currentProgress = playerData.mediaPlayer.currentPosition
                    val duration = playerData.mediaPlayer.duration
                    notifyItemChanged(
                        position, AudioPlayerEvent(
                            currentProgress,
                            duration
                        )
                    )
                }
                delay(100)
            }
        }
        currentJob = AudioJobPair(position, job)
        if (!audioJobs.map { it.position }.contains(position)) {
            audioJobs.add(currentJob)
        }
    }

    var playerMediaListener: PlayerMediaListener? = null
    var audioMediaPreparedListener: AudioMediaPreparedListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            1 -> ImageMediaItemViewHolder(
                PlayerImageViewItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            2 -> VideoMediaItemViewHolder(
                PlayerVideoItemViewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            3 -> CarouselMediaItemViewHolder(
                PlayerCarouselItemViewBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            4 -> AudioMediaItemViewHolder(
                PlayerAudioItemViewBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )

            else -> PlayerNativeAdViewHolder(
                PlayerNativeAdItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (posts[position]) {
            is PlayerUiModel.ImageMediaItem -> 1
            is PlayerUiModel.VideoMediaItem -> 2
            is PlayerUiModel.CarouselMediaItem -> 3
            is PlayerUiModel.AudioMediaItem -> 4
            is PlayerUiModel.AdItem -> 5
        }
    }


    private fun setVideoPlayerEventListener(position: Int) {
        val player =
            videoMediaPlayers.firstOrNull { it.adapterPosition == position }?.mediaPlayer
        player?.setEventListener { event ->
            when (event.type) {
                VlcMediaPlayer.Event.Vout -> {
                    notifyItemChanged(
                        position, VideoPlayerEvent(
                            VlcMediaPlayer.Event.Vout,
                            player.time,
                            player.length
                        )
                    )
                }

                VlcMediaPlayer.Event.Paused -> {
                    notifyItemChanged(
                        position, VideoPlayerEvent(
                            VlcMediaPlayer.Event.Paused
                        )
                    )
                }

                VlcMediaPlayer.Event.Playing -> {
                    notifyItemChanged(
                        position, VideoPlayerEvent(
                            VlcMediaPlayer.Event.Playing
                        )
                    )
                }

                VlcMediaPlayer.Event.TimeChanged -> {
                    notifyItemChanged(
                        position,
                        VideoPlayerEvent(
                            VlcMediaPlayer.Event.TimeChanged,
                            player.time,
                            player.length
                        )
                    )
                }
            }
        }
    }

    /**
     *  Audio not working from here for some reason it keep calling this method
     *  and onViewAttached..., The viewPager listener handle the audio.
     */
    override fun onViewDetachedFromWindow(holder: ViewHolder) {
        val position = holder.bindingAdapterPosition
        if (holder is VideoMediaItemViewHolder) {
            val player =
                videoMediaPlayers.firstOrNull { it.adapterPosition == position }?.mediaPlayer
            if (player == null)
                return
            if (!player.isReleased) {
                player.pause()
                player.detachViews()
            }
        }
        if (holder is CarouselMediaItemViewHolder) {
            val carouselAdapter = carouselAdapters.firstOrNull { it.position == position }
            if (carouselAdapter == null)
                return
            if (carouselAdapter.adapter.getItemViewType(carouselAdapter.adapter.currentSelectedItem) == 2) {
                val player =
                    carouselAdapter.adapter.videoMediaPlayers.firstOrNull { it.adapterPosition == carouselAdapter.adapter.currentSelectedItem }
                if (player == null)
                    return
                if (player.mediaPlayer.isPlaying) {
                    player.mediaPlayer.pause()
                }
            }
        }
        if (holder is PlayerNativeAdViewHolder) {
            val admobVideoController =
                admobAdmobVideoControllers.firstOrNull { it.adapterPosition == position }
            if (admobVideoController == null)
                return
            admobVideoController.videoController.pause()
        }
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        val position = holder.bindingAdapterPosition
        if (holder is VideoMediaItemViewHolder) {
            val player =
                videoMediaPlayers.firstOrNull { it.adapterPosition == position }?.mediaPlayer
            val vlcView = holder.itemView.findViewById<VLCVideoLayout>(R.id.view_vlc_layout)
            val pauseIcon = holder.itemView.findViewById<AppCompatImageView>(R.id.ic_paused)
            pauseIcon.isVisible = false
            if (player?.vlcVout?.areViewsAttached() == false)
                player.attachViews(vlcView, null, false, true)

            setVideoPlayerEventListener(position)
            if (player?.isReleased == false && isWindowHasFocus.value) {  // A check again to be sure the player is there before playing it
                player.nativeSetTime(player.time, true)
                player.play()
            }
        }
        if (holder is CarouselMediaItemViewHolder) {
            val carouselAdapter = carouselAdapters.firstOrNull { it.position == position }
            if (carouselAdapter == null)
                return
            if (carouselAdapter.adapter.getItemViewType(carouselAdapter.adapter.currentSelectedItem) == 2) {
                val player =
                    carouselAdapter.adapter.videoMediaPlayers.firstOrNull { it.adapterPosition == carouselAdapter.adapter.currentSelectedItem }
                if (player == null)
                    return
                val viewPager = holder.itemView.findViewById<ViewPager2>(R.id.view_pager)
                val vlcView =
                    viewPager.findViewWithTag<VLCVideoLayout?>("vlc_layout_${carouselAdapter.adapter.currentSelectedItem}")

                if (!player.mediaPlayer.vlcVout.areViewsAttached() && vlcView != null)
                    player.mediaPlayer.attachViews(vlcView, null, false, true)
                player.mediaPlayer.nativeSetTime(player.mediaPlayer.time, true)
                player.mediaPlayer.play()
            }
        }

        if (holder is PlayerNativeAdViewHolder) {
            val admobVideoController =
                admobAdmobVideoControllers.firstOrNull { it.adapterPosition == position }
            if (admobVideoController == null)
                return
            admobVideoController.videoController.play()
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        val position = holder.bindingAdapterPosition
        if (holder is AudioMediaItemViewHolder) {
            val playerData = audioMediaPlayers.firstOrNull { it.adapterPosition == position }
            if (playerData == null)
                return
            Timber.tag(PlayerActivity.TAG).v("Audio Position $position")
            audioJobs.firstOrNull { it.position == position }?.job?.cancel()
            playerData.isPrepared = false
            playerData.mediaPlayer.stop()
            playerData.mediaPlayer.release()
            audioMediaPlayers.firstOrNull { it.adapterPosition == position }?.let { item ->
                audioMediaPlayers.remove(item)
            }
        }
        if (holder is VideoMediaItemViewHolder) {  // to be sure not to recycler when downloading
            Timber.tag(TAG).v("ViewRecycled video $position")
            videoMediaPlayers.firstOrNull { it.adapterPosition == position }?.mediaPlayer?.release()
            videoMediaPlayers.firstOrNull { it.adapterPosition == position }?.mediaPlayer?.libVLC?.release()
            videoMediaPlayers.firstOrNull { it.adapterPosition == position }?.let { item ->
                videoMediaPlayers.remove(item)
            }
        }

        if (holder is CarouselMediaItemViewHolder) {
            val adapter = carouselAdapters.firstOrNull { it.position == position }
            adapter ?: return
            carouselAdapters.remove(adapter)
            val callback = carouselCallbacks.firstOrNull { it.position == position }
            callback ?: return
            val viewPager = holder.itemView.findViewById<ViewPager2>(R.id.view_pager)
            viewPager.unregisterOnPageChangeCallback(callback.callback)
            carouselCallbacks.remove(callback)
        }
        if (holder is PlayerNativeAdViewHolder) {
            val admobVideoController =
                admobAdmobVideoControllers.firstOrNull { it.adapterPosition == position }
            if (admobVideoController == null)
                return
            admobAdmobVideoControllers.remove(admobVideoController)
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        when (val uiModel = posts[position]) {

            is PlayerUiModel.CarouselMediaItem -> if (payloads.isNotEmpty()) {
                if (payloads.first() is DownloadStatus) {
                    (holder as CarouselMediaItemViewHolder).setDownloadProgress(payloads.first() as DownloadStatus)
                }
            } else {
                (holder as CarouselMediaItemViewHolder).onBind(
                    position,
                    this,
                    uiModel.media
                )
            }

            is PlayerUiModel.ImageMediaItem -> {
                if (payloads.isNotEmpty()) {
                    if (payloads.first() is DownloadStatus) {
                        (holder as ImageMediaItemViewHolder).setDownloadProgress(payloads.first() as DownloadStatus)
                    }
                } else {
                    (holder as ImageMediaItemViewHolder).onBind(
                        position,
                        this,
                        uiModel.media
                    )
                }
            }

            is PlayerUiModel.VideoMediaItem -> {
                if (payloads.isNotEmpty()) {
                    when (payloads.first()) {
                        is DownloadStatus -> (holder as VideoMediaItemViewHolder).setDownloadProgress(
                            payloads.first() as DownloadStatus
                        )

                        is VideoPlayerEvent -> (holder as VideoMediaItemViewHolder).setPlayerStatus(
                            payloads.first() as VideoPlayerEvent
                        )
                    }
                } else (holder as VideoMediaItemViewHolder).onBind(
                    position,
                    this,
                    uiModel.media
                )
            }

            is PlayerUiModel.AudioMediaItem -> {
                if (payloads.isNotEmpty()) {
                    when (payloads.first()) {
                        is DownloadStatus -> (holder as AudioMediaItemViewHolder).setDownloadProgress(
                            payloads.first() as DownloadStatus
                        )

                        is AudioPlayerEvent -> (holder as AudioMediaItemViewHolder).setPlayerStatus(
                            payloads.first() as AudioPlayerEvent
                        )
                    }
                } else (holder as AudioMediaItemViewHolder).onBind(position, this, uiModel.media)
            }

            is PlayerUiModel.AdItem -> (holder as PlayerNativeAdViewHolder).onBind(
                position,
                this,
                uiModel.provider,
                uiModel.adId
            )
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // NO-OP
    }

    override fun getItemCount() = posts.size


    inner class ImageMediaItemViewHolder(private val playerImageViewItemBinding: PlayerImageViewItemBinding) :
        PlayerBaseViewHolder(playerImageViewItemBinding.root) {


        @SuppressLint("SetTextI18n")
        fun setDownloadProgress(downloadStatus: DownloadStatus) {
            setIsRecyclable(false)
            if (downloadStatus.progress > 0.0 && downloadStatus.progress < 100.0) {
                playerImageViewItemBinding.progressContainer.isVisible = true
                playerImageViewItemBinding.progressBar.progress = downloadStatus.progress.toFloat()
                playerImageViewItemBinding.progressValue.text =
                    "${downloadStatus.progress.roundToInt()}%"
            } else if (downloadStatus.progress == 100.0 || downloadStatus.progress == -1.0) {
                playerImageViewItemBinding.progressContainer.isVisible = true
                playerImageViewItemBinding.progressBar.progress = 100.0f
                playerImageViewItemBinding.progressValue.text = "100%"

                Handler(Looper.getMainLooper()).postDelayed({
                    setIsRecyclable(true)
                    playerImageViewItemBinding.progressContainer.isVisible = false

                    // restart the player here with the new file location
                    // for downloaded items only.
                    val newItem = posts[downloadStatus.position]
                    Timber.tag(TAG).v("Can update the image here..")
                }, 1500)

            }
        }


        private fun loadDownloadedItemItem(imageUrl: String) {
            Glide
                .with(playerImageViewItemBinding.root.context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_media_place_holder)
                .into(playerImageViewItemBinding.postImage)
        }

        @SuppressLint("SetTextI18n")
        override fun onBind(
            position: Int,
            adapter: PlayerAdapter,
            media: PlayerDataModel
        ) {
            playerImageViewItemBinding.download.setOnClickListener {
                adapter.playerMediaListener?.onDownloadClick((posts[bindingAdapterPosition] as PlayerUiModel.ImageMediaItem).media)
            }
            playerImageViewItemBinding.more.setOnClickListener {
                adapter.playerMediaListener?.onMoreClick((posts[bindingAdapterPosition] as PlayerUiModel.ImageMediaItem).media)
            }

            when (media) {
                is PlayerDataModel.DownloadedMediaItem -> {
                    val post = media.media
                    if (post.caption != null && post.caption.text.isNotBlank()) {
                        playerImageViewItemBinding.caption.isVisible = true
                        playerImageViewItemBinding.caption.text = post.caption.text
                    }

                    loadDownloadedItemItem(post.post.thumbnailUrl)

                    if (media.media.mediaUser.username.isNotBlank() && media.media.mediaUser.profilePicUrl.isNotBlank()) {
                        Glide.with(playerImageViewItemBinding.root.context)
                            .load(media.media.mediaUser.profilePicUrl)
                            .placeholder(R.drawable.profile_place_holder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(playerImageViewItemBinding.mediaUserPicture)
                        playerImageViewItemBinding.mediaUsername.text =
                            media.media.mediaUser.username
                    } else {
                        playerImageViewItemBinding.mediaUserPicture.setImageResource(R.drawable.profile_place_holder)
                        playerImageViewItemBinding.mediaUsername.text =
                            playerImageViewItemBinding.root.context.getString(R.string.unknown)
                    }
                }

                is PlayerDataModel.CollectionMediaItem -> {
                    if (media.media.user != null) {
                        Glide.with(playerImageViewItemBinding.root.context)
                            .load(media.media.user.profilePicUrl)
                            .placeholder(R.drawable.profile_place_holder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(playerImageViewItemBinding.mediaUserPicture)
                        playerImageViewItemBinding.mediaUsername.text =
                            media.media.user.username
                    }
                    if (media.media.caption?.text != null) {
                        playerImageViewItemBinding.caption.isVisible = true
                        playerImageViewItemBinding.caption.text = media.media.caption.text
                    }
                    loadDownloadedItemItem(media.media.imageVersions2?.candidates?.maxBy { it?.height!! }?.url!!)
                }

                is PlayerDataModel.ExploreMediaItem -> {
                    if (media.media.user != null) {
                        Glide.with(playerImageViewItemBinding.root.context)
                            .load(media.media.user.profilePicUrl)
                            .placeholder(R.drawable.profile_place_holder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(playerImageViewItemBinding.mediaUserPicture)
                        playerImageViewItemBinding.mediaUsername.text =
                            media.media.user.username
                    }
                    if (media.media.caption?.text != null) {
                        playerImageViewItemBinding.caption.isVisible = true
                        playerImageViewItemBinding.caption.text = media.media.caption.text
                    }
                    loadDownloadedItemItem(media.media.imageVersions2?.candidates?.maxBy { it?.height!! }?.url!!)
                }

                else -> {
                    // NO-Op
                }
            }
        }
    }

    inner class VideoMediaItemViewHolder(private val videoVideoItemViewBinding: PlayerVideoItemViewBinding) :
        PlayerBaseViewHolder(videoVideoItemViewBinding.root) {

        @SuppressLint("SetTextI18n")
        fun setDownloadProgress(downloadStatus: DownloadStatus) {
            setIsRecyclable(false)
            videoVideoItemViewBinding.player.isVisible = false
            videoVideoItemViewBinding.fileStatus.isVisible = false
            if (downloadStatus.progress > 0.0 && downloadStatus.progress < 100.0) {
                videoVideoItemViewBinding.progressContainer.isVisible = true
                videoVideoItemViewBinding.progressBar.progress = downloadStatus.progress.toFloat()
                videoVideoItemViewBinding.progressValue.text =
                    "${downloadStatus.progress.roundToInt()}%"
            } else if (downloadStatus.progress == 100.0 || downloadStatus.progress == -1.0) {
                videoVideoItemViewBinding.progressBar.progress = 100.0f
                videoVideoItemViewBinding.progressValue.text = "100%"

                Handler(Looper.getMainLooper()).postDelayed({
                    videoVideoItemViewBinding.progressContainer.isVisible = false
                    videoVideoItemViewBinding.player.isVisible = true

                    // restart the player here with the new file location
                    // for downloaded items only.
                    val newItem = posts[downloadStatus.position]
                    if (newItem is PlayerUiModel.VideoMediaItem && newItem.media is PlayerDataModel.DownloadedMediaItem) {
                        Timber.tag(TAG).v("Updating the player..")
                        loadDownloadedVideo(
                            downloadStatus.position,
                            newItem.media,
                        )
                        setVideoPlayerEventListener(bindingAdapterPosition)
                    }
                    setIsRecyclable(true)
                }, 1000)

            }
        }

        fun setPlayerStatus(videoPlayerEvent: VideoPlayerEvent) {
            Handler(Looper.getMainLooper()).post {
                when (videoPlayerEvent.event) {
                    VlcMediaPlayer.Event.Vout -> {
                        videoVideoItemViewBinding.progress.max = videoPlayerEvent.maxLength.toInt()
                    }

                    VlcMediaPlayer.Event.Paused -> {
                        videoVideoItemViewBinding.icPaused.isVisible = true
                    }

                    VlcMediaPlayer.Event.Playing -> {
                        videoVideoItemViewBinding.icPaused.isVisible = false
                    }

                    VlcMediaPlayer.Event.TimeChanged -> {
                        videoVideoItemViewBinding.progress.progress =
                            videoPlayerEvent.currentProgress.toInt()
                    }
                }
            }
        }

        private fun loadExploreAndCollectionVideo(
            position: Int,
            mediaUri: Uri,
            adapter: PlayerAdapter
        ) {
            val libVlc = LibVLC(VideoDownloaderApp.application, ArrayList<String>().apply {
//                if (BuildConfig.DEBUG)
//                    add("-vvv")
                add("--http-reconnect")
                add("--input-repeat=900")
                add("--avcodec-hw=any")
                add("--no-video-title-show")
//                add("--network-caching=1000")
//                add("--http-caching==1000")
//                addOption(":avcodec-hw=dxva2")
//                add("--sout-keep")
//                add("--clock-jitter=1")
            })
            val mediaPlayer = VlcMediaPlayer(libVlc)
            val ua = VideoDownloaderApp.application.getString(R.string.web_agent_1)
            libVlc.setUserAgent(ua, ua)
            VlcMedia(libVlc, mediaUri).apply {
                addOption(":http-user-agent=${ua}")
                mediaPlayer.media = this
                this.release()
            }
            mediaPlayer.videoScale = VlcMediaPlayer.ScaleType.SURFACE_ORIGINAL
            mediaPlayer.attachViews(videoVideoItemViewBinding.viewVlcLayout, null, false, true)
            val playerData = VideoPlayerPair(mediaPlayer, position)
            if (!adapter.videoMediaPlayers.map { it.adapterPosition }.contains(position)) {
                adapter.videoMediaPlayers.add(playerData)
            }
            if (
                videoVideoItemViewBinding.root.isAttachedToWindow
                && videoVideoItemViewBinding.root.hasWindowFocus()
            ) {
                mediaPlayer.play()
            } else {
                mediaPlayer.pause()
            }
        }

        private fun loadDownloadedVideo(
            position: Int,
            media: PlayerDataModel.DownloadedMediaItem,
        ) {
            val videoFile = File(media.media.media.first().fileLocation)
            if (videoFile.exists()) {
                videoVideoItemViewBinding.player.isVisible = true
                videoVideoItemViewBinding.fileStatus.isVisible = false
                val libVlc = LibVLC(VideoDownloaderApp.application, ArrayList<String>().apply {
                    add("--input-repeat=900")
                    add("--avcodec-hw=any")
                    add("--no-video-title-show")
                })
                val mediaPlayer = VlcMediaPlayer(libVlc)
                VlcMedia(libVlc, media.media.media.first().fileLocation).apply {
                    mediaPlayer.media = this
                    release()
                }
                mediaPlayer.videoScale = VlcMediaPlayer.ScaleType.SURFACE_ORIGINAL
                mediaPlayer.attachViews(videoVideoItemViewBinding.viewVlcLayout, null, false, true)

                val playerData = VideoPlayerPair(mediaPlayer, position)
                if (!videoMediaPlayers.map { it.adapterPosition }.contains(position)) {
                    videoMediaPlayers.add(playerData)
                }


                if (
                    videoVideoItemViewBinding.root.isAttachedToWindow
                    && videoVideoItemViewBinding.root.hasWindowFocus()
                ) {
                    Timber.tag(TAG).v("Item from here has WindowFocused")
                    mediaPlayer.play()
                } else {
                    mediaPlayer.pause()
                }
            } else {
                videoVideoItemViewBinding.progress.progress = 0
                videoVideoItemViewBinding.player.isVisible = false
                videoVideoItemViewBinding.icPaused.isVisible = false
                videoVideoItemViewBinding.fileStatus.isVisible = true
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onBind(
            position: Int,
            adapter: PlayerAdapter,
            media: PlayerDataModel
        ) {
            videoVideoItemViewBinding.player.setOnClickListener {
                lockView(videoVideoItemViewBinding.player, Handler(Looper.getMainLooper()), 10)
                val mediaPlayerData =
                    adapter.videoMediaPlayers.firstOrNull { it.adapterPosition == position }
                if (mediaPlayerData == null)
                    return@setOnClickListener
                if (mediaPlayerData.mediaPlayer.isPlaying) {
                    mediaPlayerData.mediaPlayer.pause()
                } else {
                    mediaPlayerData.mediaPlayer.play()
                    mediaPlayerData.mediaPlayer.updateVideoSurfaces()
                }
            }

            videoVideoItemViewBinding.progress.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (
                        adapter.videoMediaPlayers.firstOrNull { it.adapterPosition == position } != null
                        && !adapter.videoMediaPlayers.first { it.adapterPosition == position }.mediaPlayer.isReleased
                        && seekBar?.isPressed == true
                    ) {
                        adapter.videoMediaPlayers.first { it.adapterPosition == position }.mediaPlayer.time =
                            progress.toLong()
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    adapter.videoMediaPlayers.firstOrNull { it.adapterPosition == position }?.mediaPlayer?.pause()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    adapter.videoMediaPlayers.firstOrNull { it.adapterPosition == position }?.mediaPlayer?.play()
                }
            })
            videoVideoItemViewBinding.download.setOnClickListener {
                adapter.playerMediaListener?.onDownloadClick((posts[bindingAdapterPosition] as PlayerUiModel.VideoMediaItem).media)
            }
            videoVideoItemViewBinding.more.setOnClickListener {
                adapter.playerMediaListener?.onMoreClick((posts[bindingAdapterPosition] as PlayerUiModel.VideoMediaItem).media)
            }

            when (media) {
                is PlayerDataModel.DownloadedMediaItem -> {
                    val post = media.media
                    if (post.caption != null && post.caption.text.isNotBlank()) {
                        videoVideoItemViewBinding.caption.isVisible = true
                        videoVideoItemViewBinding.caption.text = post.caption.text
                    }
                    if (media.media.mediaUser.fullName.isNotBlank() && media.media.mediaUser.profilePicUrl.isNotBlank()) {
                        Glide.with(videoVideoItemViewBinding.root.context)
                            .load(media.media.mediaUser.profilePicUrl)
                            .placeholder(R.drawable.profile_place_holder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(videoVideoItemViewBinding.mediaUserPicture)
                        videoVideoItemViewBinding.mediaUsername.text =
                            media.media.mediaUser.username
                    } else {
                        videoVideoItemViewBinding.mediaUserPicture.setImageResource(R.drawable.profile_place_holder)
                        videoVideoItemViewBinding.mediaUsername.text =
                            videoVideoItemViewBinding.root.context.getString(R.string.unknown)
                    }
                    loadDownloadedVideo(position, media)
                }

                is PlayerDataModel.ExploreMediaItem -> {
                    videoVideoItemViewBinding.player.isVisible = true
                    if (media.media.caption != null && media.media.caption.text?.isNotBlank() == true) {
                        videoVideoItemViewBinding.caption.isVisible = true
                        videoVideoItemViewBinding.caption.text = media.media.caption.text
                        videoVideoItemViewBinding.mediaUsername.text = media.media.user?.username
                    }
                    if (media.media.user?.fullName?.isNotBlank() == true) {
                        Glide.with(videoVideoItemViewBinding.root.context)
                            .load(media.media.user.profilePicUrl!!)
                            .placeholder(R.drawable.profile_place_holder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(videoVideoItemViewBinding.mediaUserPicture)
                    } else {
                        videoVideoItemViewBinding.mediaUserPicture.setImageResource(R.drawable.profile_place_holder)
                        videoVideoItemViewBinding.mediaUsername.text =
                            videoVideoItemViewBinding.root.context.getString(R.string.unknown)
                    }
                    loadExploreAndCollectionVideo(
                        position,
                        media.media.videoVersions?.first()?.url!!.toUri(),
                        adapter
                    )
                }

                is PlayerDataModel.CollectionMediaItem -> {
                    videoVideoItemViewBinding.player.isVisible = true
                    if (media.media.caption != null && media.media.caption.text?.isNotBlank() == true) {
                        videoVideoItemViewBinding.caption.isVisible = true
                        videoVideoItemViewBinding.caption.text = media.media.caption.text
                    }
                    if (media.media.user?.fullName?.isNotBlank() == true) {
                        Glide.with(videoVideoItemViewBinding.root.context)
                            .load(media.media.user.profilePicUrl!!)
                            .placeholder(R.drawable.profile_place_holder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(videoVideoItemViewBinding.mediaUserPicture)
                        videoVideoItemViewBinding.mediaUsername.text = media.media.user.username
                    } else {
                        videoVideoItemViewBinding.mediaUserPicture.setImageResource(R.drawable.profile_place_holder)
                        videoVideoItemViewBinding.mediaUsername.text =
                            videoVideoItemViewBinding.root.context.getString(R.string.unknown)
                    }
                    loadExploreAndCollectionVideo(
                        position,
                        media.media.videoVersions?.first()?.url!!.toUri(),
                        adapter
                    )
                }

                else -> {
                    // NO-Op
                }
            }
        }
    }

    inner class CarouselMediaItemViewHolder(private val carouselBinding: PlayerCarouselItemViewBinding) :
        PlayerBaseViewHolder(carouselBinding.root) {


        @SuppressLint("SetTextI18n")
        fun setDownloadProgress(downloadStatus: DownloadStatus) {
            setIsRecyclable(false)
            if (downloadStatus.progress > 0.0 && downloadStatus.progress < 100.0) {
                carouselBinding.progressContainer.isVisible = true
                carouselBinding.progressBar.progress = downloadStatus.progress.toFloat()
                carouselBinding.progressValue.text =
                    "${downloadStatus.progress.roundToInt()}%"
            } else if (downloadStatus.progress == 100.0 || downloadStatus.progress == -1.0) {
                carouselBinding.progressBar.progress = 100.0f
                carouselBinding.progressValue.text = "100%"

                Handler(Looper.getMainLooper()).postDelayed({
                    setIsRecyclable(true)
                    carouselBinding.progressContainer.isVisible = false

                    // restart the player here with the new file location
                    // for downloaded items only.
                    val newItem = posts[downloadStatus.position]
                    if (newItem is PlayerUiModel.CarouselMediaItem && newItem.media is PlayerDataModel.DownloadedMediaItem) {
                        Timber.tag(TAG).v("Updating the Carousel..")
                        onViewRecycled(this)
                        notifyItemChanged(downloadStatus.position)
                    }
                }, 800)

            }
        }


        @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
        override fun onBind(
            position: Int,
            adapter: PlayerAdapter,
            media: PlayerDataModel,
        ) {
            carouselBinding.download.setOnClickListener {
                adapter.playerMediaListener?.onDownloadClick((posts[bindingAdapterPosition] as PlayerUiModel.CarouselMediaItem).media)
            }
            carouselBinding.more.setOnClickListener {
                adapter.playerMediaListener?.onMoreClick((posts[bindingAdapterPosition] as PlayerUiModel.CarouselMediaItem).media)
            }

            val carouselAdapter = CarouselAdapter()
            val carouselList = mutableListOf<CarouselPlayerUiModel>()

            when (media) {
                is PlayerDataModel.DownloadedMediaItem -> {
                    if (media.media.caption != null && media.media.caption.text.isNotBlank()) {
                        carouselBinding.caption.isVisible = true
                        carouselBinding.caption.text = media.media.caption.text
                    }

                    if (media.media.mediaUser.username.isNotBlank() && media.media.mediaUser.profilePicUrl.isNotBlank()) {
                        Glide.with(carouselBinding.root.context)
                            .load(media.media.mediaUser.profilePicUrl)
                            .placeholder(R.drawable.profile_place_holder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(carouselBinding.mediaUserPicture)
                        carouselBinding.mediaUsername.text =
                            media.media.mediaUser.username
                    } else {
                        carouselBinding.mediaUserPicture.setImageResource(R.drawable.profile_place_holder)
                        carouselBinding.mediaUsername.text =
                            carouselBinding.root.context.getString(R.string.unknown)
                    }

                    media.media.media.forEach { mediaItem ->
                        if (mediaItem.type == "image") {
                            carouselList.add(
                                CarouselPlayerUiModel.ImageMediaItem(
                                    CarouselPlayerDataModel.DownloadedMediaItem(mediaItem)
                                )
                            )
                        } else {
                            carouselList.add(
                                CarouselPlayerUiModel.VideoMediaItem(
                                    CarouselPlayerDataModel.DownloadedMediaItem(mediaItem)
                                )
                            )
                        }
                    }
                }

                is PlayerDataModel.ExploreMediaItem -> {
                    if (media.media.user != null) {
                        Glide.with(carouselBinding.root.context)
                            .load(media.media.user.profilePicUrl)
                            .placeholder(R.drawable.profile_place_holder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(carouselBinding.mediaUserPicture)
                        carouselBinding.mediaUsername.text =
                            media.media.user.username
                    }
                    if (media.media.caption?.text != null) {
                        carouselBinding.caption.isVisible = true
                        carouselBinding.caption.text = media.media.caption.text
                    }



                    media.media.carouselMedia?.forEach { mediaItem ->
                        if (mediaItem == null)
                            return
                        if (mediaItem.mediaType == 1) {
                            carouselList.add(
                                CarouselPlayerUiModel.ImageMediaItem(
                                    CarouselPlayerDataModel.ExploreMediaItem(mediaItem)
                                )
                            )
                        } else {
                            carouselList.add(
                                CarouselPlayerUiModel.VideoMediaItem(
                                    CarouselPlayerDataModel.ExploreMediaItem(mediaItem)
                                )
                            )
                        }
                    }
                }

                is PlayerDataModel.CollectionMediaItem -> {
                    if (media.media.user != null) {
                        Glide.with(carouselBinding.root.context)
                            .load(media.media.user.profilePicUrl)
                            .placeholder(R.drawable.profile_place_holder)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(carouselBinding.mediaUserPicture)
                        carouselBinding.mediaUsername.text =
                            media.media.user.username
                    }
                    if (media.media.caption?.text != null) {
                        carouselBinding.caption.isVisible = true
                        carouselBinding.caption.text = media.media.caption.text
                    }
                    media.media.carouselMedias?.forEach { mediaItem ->
                        if (mediaItem.mediaType == 1) {
                            carouselList.add(
                                CarouselPlayerUiModel.ImageMediaItem(
                                    CarouselPlayerDataModel.CollectionMediaItem(mediaItem)
                                )
                            )
                        } else {
                            carouselList.add(
                                CarouselPlayerUiModel.VideoMediaItem(
                                    CarouselPlayerDataModel.CollectionMediaItem(mediaItem)
                                )
                            )
                        }
                    }
                }

                else -> {
                    // NO-Op
                }
            }

            val callback = object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    carouselPosition.value = CarouselSelectedPosition(
                        bindingAdapterPosition,
                        position
                    )
                }
            }

            carouselAdapter.carouselMediaItems = carouselList
            carouselBinding.viewPager.adapter = carouselAdapter
            carouselBinding.viewPager.registerOnPageChangeCallback(callback)
            carouselAdapter.notifyDataSetChanged()

            if (!carouselAdapters.map { it.position }.contains(position)) {
                carouselAdapters.add(CarouselAdapterPair(carouselAdapter, bindingAdapterPosition))
            }

            carouselBinding.maxPages.text = carouselList.count().toString()
            carouselBinding.currentPage.text = "1"

            val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
                @SuppressLint("SetTextI18n")
                override fun onPageSelected(position: Int) {
                    carouselBinding.currentPage.text = "${position + 1}"
                    carouselAdapters.firstOrNull { it.position == bindingAdapterPosition }?.let {
                        it.adapter.currentSelectedItem = position
                    }
                }
            }
            carouselBinding.viewPager.registerOnPageChangeCallback(pageChangeCallback)

            if (!carouselCallbacks.map { it.position }.contains(bindingAdapterPosition)) {
                carouselCallbacks.add(
                    CarouselCallbackPair(
                        pageChangeCallback,
                        bindingAdapterPosition
                    )
                )
            }
        }
    }

    inner class AudioMediaItemViewHolder(private val playerAudioItemViewBinding: PlayerAudioItemViewBinding) :
        PlayerBaseViewHolder(playerAudioItemViewBinding.root) {

        fun setPlayerStatus(playerEvent: AudioPlayerEvent) {
            if (playerEvent.currentProgress != 0) {
                playerAudioItemViewBinding.progress.progress = playerEvent.currentProgress
                playerAudioItemViewBinding.currentTime.text =
                    formatAudioTime(playerEvent.currentProgress)
            }
            if (playerEvent.maxLength != 0) {
                playerAudioItemViewBinding.progress.max = playerEvent.maxLength
                playerAudioItemViewBinding.totalTime.text = formatAudioTime(playerEvent.maxLength)
            }
        }

        fun setDownloadProgress(downloadStatus: DownloadStatus) {
            setIsRecyclable(false)
            if (downloadStatus.progress > 0.0 && downloadStatus.progress < 100.0) {
                playerAudioItemViewBinding.downloadProgress.isVisible = true
                playerAudioItemViewBinding.downloadProgress.progress =
                    downloadStatus.progress.toInt()
            } else if (downloadStatus.progress == 100.0 || downloadStatus.progress == -1.0) {
                playerAudioItemViewBinding.downloadProgress.isVisible = true
                playerAudioItemViewBinding.downloadProgress.progress = 100

                Handler(Looper.getMainLooper()).postDelayed({
                    setIsRecyclable(true)
                    playerAudioItemViewBinding.downloadProgress.isVisible = false
                    // restart the player here with the new file location
                    // for downloaded items only.
                    val newItem =
                        (posts[downloadStatus.position] as PlayerUiModel.AudioMediaItem).media as PlayerDataModel.DownloadedMediaItem
                    Timber.tag(TAG).v("Can update the audio here..")
                    initDownloadedMediaPlayer(newItem)
                }, 1000)

            }
        }

        private fun resumeAnimation() {
            if (!playerAudioItemViewBinding.audioPlayingAnimation.isAnimating) {
                playerAudioItemViewBinding.audioPlayingAnimation.resumeAnimation()
            }
        }

        private fun pauseAnimation() {
            if (playerAudioItemViewBinding.audioPlayingAnimation.isAnimating) {
                playerAudioItemViewBinding.audioPlayingAnimation.pauseAnimation()
            }
        }

        @SuppressLint("SetTextI18n")
        private fun initDownloadedMediaPlayer(
            media: PlayerDataModel.DownloadedMediaItem,
        ) {
            val file = File(media.media.media.first().fileLocation)
            if (!file.exists()) {
                playerAudioItemViewBinding.totalTime.text = "--"
                playerAudioItemViewBinding.currentTime.text = "--"
                playerAudioItemViewBinding.audioPlayingAnimation.pauseAnimation()
                playerAudioItemViewBinding.fileStatus.isVisible = true
                return
            }
            playerAudioItemViewBinding.fileStatus.isVisible = false
            resumeAnimation()

            Handler(Looper.getMainLooper()).post {
                val mediaPlayer = MediaPlayer()
                mediaPlayer.isLooping = true
                mediaPlayer.setDataSource(media.media.media.first().fileLocation)
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                mediaPlayer.prepareAsync()
                mediaPlayer.setWakeMode(
                    VideoDownloaderApp.application,
                    PowerManager.PARTIAL_WAKE_LOCK
                )

                mediaPlayer.setOnPreparedListener {
                    audioMediaPlayers.add(
                        AudioModel(
                            mediaPlayer = mediaPlayer,
                            isPrepared = true,
                            isPaused = true,
                            adapterPosition = bindingAdapterPosition
                        )
                    )
                    audioMediaPreparedListener?.onAudioMediaPrepared(it, bindingAdapterPosition)
                }
            }

        }


        @SuppressLint("SetTextI18n")
        override fun onBind(
            position: Int,
            adapter: PlayerAdapter,
            media: PlayerDataModel,
        ) {
            when (media) {
                is PlayerDataModel.DownloadedMediaItem -> {
                    try {
                        playerAudioItemViewBinding.download.setOnClickListener {
                            lockView(
                                playerAudioItemViewBinding.downloadProgress,
                                Handler(Looper.getMainLooper()),
                                500,
                            )
                            adapter.playerMediaListener?.onDownloadClick((posts[bindingAdapterPosition] as PlayerUiModel.AudioMediaItem).media)
                        }

                        playerAudioItemViewBinding.more.setOnClickListener {
                            adapter.playerMediaListener?.onMoreClick((posts[bindingAdapterPosition] as PlayerUiModel.AudioMediaItem).media)
                        }

                        if (media.media.mediaUser.fullName.isNotBlank() && media.media.mediaUser.profilePicUrl.isNotBlank()) {
                            Glide.with(playerAudioItemViewBinding.root.context)
                                .load(media.media.mediaUser.profilePicUrl)
                                .placeholder(R.drawable.profile_place_holder)
                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                .into(playerAudioItemViewBinding.mediaUserPicture)
                            playerAudioItemViewBinding.mediaUsername.text =
                                media.media.mediaUser.username
                        } else {
                            playerAudioItemViewBinding.mediaUserPicture.setImageResource(R.drawable.profile_place_holder)
                            playerAudioItemViewBinding.mediaUsername.text =
                                playerAudioItemViewBinding.root.context.getString(R.string.unknown)
                        }

                        val clickListener = OnClickListener {
                            val mdp =
                                adapter.audioMediaPlayers.firstOrNull { it.adapterPosition == position }
                            if (mdp == null)
                                return@OnClickListener
                            if (mdp.isPrepared && !mdp.isPaused) {
                                playerAudioItemViewBinding.audioPlayingAnimation.pauseAnimation()
                                mdp.isPaused = true
                                mdp.mediaPlayer.pause()
                            } else if (mdp.isPrepared) {
                                playerAudioItemViewBinding.audioPlayingAnimation.resumeAnimation()
                                mdp.isPaused = false
                                mdp.mediaPlayer.start()
                            }
                        }

                        playerAudioItemViewBinding.audioPlayingAnimation.setOnClickListener(
                            clickListener
                        )
                        playerAudioItemViewBinding.container.setOnClickListener(clickListener)

                        playerAudioItemViewBinding.progress.setOnSeekBarChangeListener(object :
                            SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(
                                seekBar: SeekBar?,
                                progress: Int,
                                fromUser: Boolean
                            ) {
                                if (
                                    seekBar?.isPressed == true &&
                                    adapter.audioMediaPlayers.first { it.adapterPosition == position }.isPrepared
                                ) {
                                    adapter.audioMediaPlayers.first { it.adapterPosition == position }.mediaPlayer.seekTo(
                                        progress
                                    )
                                }
                            }

                            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                                // NO-OP
                            }

                            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                                // NO-OP
                            }
                        })

                        initDownloadedMediaPlayer(media)

                    } catch (e: Exception) {
                        Timber.tag(TAG).e(e)
                    }
                }

                is PlayerDataModel.ExploreMediaItem -> {
                    // NO-OP
                }

                is PlayerDataModel.CollectionMediaItem -> {
                    // NO-OP
                }

                else -> {
                    // NO-Op
                }
            }
        }
    }

    inner class PlayerNativeAdViewHolder(private val playerAdBinding: PlayerNativeAdItemBinding) :
        PlayerBaseNativeAdViewHolder(playerAdBinding) {

        override fun onBind(
            position: Int,
            adapter: PlayerAdapter,
            adProvider: String,
            adId: String
        ) {
            when (adProvider) {
                "admob" -> {
                    Handler(Looper.getMainLooper()).post {
                        val videoOptions = VideoOptions.Builder().setStartMuted(false).build()
                        val nativeOptions = NativeAdOptions.Builder()
                            .setMediaAspectRatio(MediaAspectRatio.ANY)
                            .setVideoOptions(videoOptions)
                            .build()

                        val adLoader = AdLoader
                            .Builder(
                                VideoDownloaderApp.application,
                                adId
                            )
                            .forNativeAd { nativeAd: NativeAd ->

                                playerAdBinding.nativeAdView.mediaView = playerAdBinding.adMedia

                                playerAdBinding.nativeAdView.headlineView =
                                    playerAdBinding.adHeadline
                                playerAdBinding.nativeAdView.bodyView = playerAdBinding.adBody
                                playerAdBinding.nativeAdView.callToActionView =
                                    playerAdBinding.adCallToAction

                                val imageView = playerAdBinding.adAppIcon
                                imageView.clipToOutline = true
                                playerAdBinding.nativeAdView.iconView = imageView




                                playerAdBinding.adMedia.mediaContent = nativeAd.mediaContent
                                playerAdBinding.adHeadline.text = nativeAd.headline

                                if (nativeAd.body == null) {
                                    playerAdBinding.adBody.visibility = View.INVISIBLE
                                    playerAdBinding.adBody.text = ""
                                } else {
                                    playerAdBinding.adBody.visibility = View.VISIBLE
                                    playerAdBinding.adBody.text = nativeAd.body
                                }

                                if (nativeAd.callToAction == null) {
                                    playerAdBinding.adCallToAction.visibility = View.INVISIBLE
                                    playerAdBinding.adBody.text = ""
                                } else {
                                    playerAdBinding.adCallToAction.visibility = View.VISIBLE
                                    playerAdBinding.adCallToAction.text = nativeAd.callToAction
                                }

                                if (nativeAd.icon == null) {
                                    playerAdBinding.adAppIcon.setVisibility(View.INVISIBLE)
                                } else {
                                    playerAdBinding.adAppIcon.setVisibility(View.VISIBLE)
                                    playerAdBinding.adAppIcon.setImageDrawable(nativeAd.icon?.drawable)
                                }

                                playerAdBinding.nativeAdView.setNativeAd(nativeAd)

                                val vc = nativeAd.mediaContent?.videoController


                                // Updates the UI to say whether or not this ad has a video asset.
                                if (vc != null && vc.hasVideoContent() == true) {
                                    admobAdmobVideoControllers.add(
                                        AdmobNativeAdControllerPair(
                                            vc,
                                            bindingAdapterPosition
                                        )
                                    )
                                    // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
                                    // VideoController will call methods on this object when events occur in the video
                                    // lifecycle.
                                    vc.videoLifecycleCallbacks =
                                        object : VideoController.VideoLifecycleCallbacks() {
                                            //
                                        }
                                }
                            }
                            .withAdListener(object : AdListener() {
                                override fun onAdFailedToLoad(adError: LoadAdError) {
                                    Timber.tag(TAG).v("{${adError.message}}")
                                }
                            })
                            .withNativeAdOptions(nativeOptions)
                            .build()
                        adLoader.loadAd(AdRequest.Builder().build())
                    }
                }

                "facebook" -> {

                }
            }
        }
    }

}

sealed class PlayerUiModel(val mediaId: String) {
    data class CarouselMediaItem(
        val media: PlayerDataModel,
    ) : PlayerUiModel(media.mediaId)

    data class VideoMediaItem(val media: PlayerDataModel) :
        PlayerUiModel(media.mediaId)

    data class ImageMediaItem(val media: PlayerDataModel) :
        PlayerUiModel(media.mediaId)

    data class AudioMediaItem(val media: PlayerDataModel) :
        PlayerUiModel(media.mediaId)

    data class AdItem(val provider: String, val adId: String) :
        PlayerUiModel(adId)
}

sealed class PlayerDataModel(val mediaType: Int, val mediaId: String) : Serializable {
    data class ExploreMediaItem(val media: Media) : PlayerDataModel(media.mediaType!!, media.id!!),
        Serializable

    data class CollectionMediaItem(val media: CollectionMedia) :
        PlayerDataModel(media.mediaType!!, media.id!!),
        Serializable

    data class DownloadedMediaItem(val media: PostWithMediaUserAndMedia) :
        PlayerDataModel(media.post.mediaType, media.post.mediaId),
        Serializable

    data class AdMediaItem(val provider: String, val adId: String) : PlayerDataModel(-9, provider),
        Serializable
}

interface PlayerMediaListener {
    fun onDownloadClick(media: PlayerDataModel)
    fun onMoreClick(media: PlayerDataModel)
    suspend fun onMediaDownloaded(media: PlayerDataModel.DownloadedMediaItem, position: Int)
}

interface AudioMediaPreparedListener {
    fun onAudioMediaPrepared(mediaPlayer: MediaPlayer, position: Int)
}