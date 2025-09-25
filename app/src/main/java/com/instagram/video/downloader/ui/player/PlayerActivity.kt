package com.instagram.video.downloader.ui.player

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.play.core.review.ReviewManagerFactory
import com.instagram.video.downloader.R
import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.common.DataReference
import com.instagram.video.downloader.common.TempPlayerDataModel
import com.instagram.video.downloader.common.buildInstagramLink
import com.instagram.video.downloader.common.download.DownloadProgressReceiver
import com.instagram.video.downloader.common.download.DownloadProgressReceiver.Companion.DOWNLOAD_IN_PROGRESS
import com.instagram.video.downloader.common.download.DownloadProgressReceiver.Companion.DOWNLOAD_LIST_POSTS_COMPLETED
import com.instagram.video.downloader.common.download.DownloadProgressReceiver.Companion.DOWNLOAD_POST_COMPLETED
import com.instagram.video.downloader.common.download.VDDownloadManager
import com.instagram.video.downloader.common.getExtension
import com.instagram.video.downloader.common.openInstagram
import com.instagram.video.downloader.common.shareFile
import com.instagram.video.downloader.common.shareFiles
import com.instagram.video.downloader.common.shareText
import com.instagram.video.downloader.common.ui.compose.CollectionMediaPlayerActions
import com.instagram.video.downloader.common.ui.compose.DownloadedPostPlayerActions
import com.instagram.video.downloader.common.ui.compose.ExploreMediaPlayerActions
import com.instagram.video.downloader.common.ui.compose.InstaDownloaderTheme
import com.instagram.video.downloader.data.remote.api.dto.hasValidSubscription
import com.instagram.video.downloader.databinding.ActivityPlayerBinding
import com.instagram.video.downloader.ui.base.BaseActivity
import com.instagram.video.downloader.ui.base.BaseAlertDialog
import com.instagram.video.downloader.ui.base.showMessage
import com.instagram.video.downloader.ui.home.HomeActivity
import com.instagram.video.downloader.ui.plans.PlansActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import com.facebook.ads.InterstitialAd as FbInterstitialAd


@AndroidEntryPoint
class PlayerActivity : BaseActivity<ActivityPlayerBinding>(), PlayerMediaListener,
    AudioMediaPreparedListener {

    companion object {
        const val TAG = "PlayerActivity"
        fun getIntent(context: Context, sessionId: String, scrollTo: Int = 0) =
            Intent(context, PlayerActivity::class.java).apply {
                this.putExtra("session_id", sessionId)
                this.putExtra("scroll_to", scrollTo)
                return this
            }
    }

    @Inject
    lateinit var downloadManager: VDDownloadManager

    private val viewModel: PlayerViewModel by viewModels()

    private val adapter = PlayerAdapter()

    private var admobRewardedAd: RewardedAd? = null
    private var massDownloadsRewardedAd: RewardedAd? = null

    private var fbDownloadInterstitialAd: FbInterstitialAd? = null          // download finish inter
    private var fbInterstitialAd: FbInterstitialAd? = null                  // exit inter

    private var downloadInterstitialAd: InterstitialAd? = null              // download finish inter
    private var admobInterstitialAd: InterstitialAd? = null                 // exit inter
        set(value) {
            field = value
            if (value != null) {
                value.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        runOnUiThread {
                            finish()
                        }
                    }
                }
            }
        }

    private val plansStartForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                viewModel.updateRemoteUser()
            }
        }


    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            CoroutineScope(Dispatchers.Main).launch {
                if (adapter.posts[position] is PlayerUiModel.AudioMediaItem) {
                    val playerData =
                        adapter.audioMediaPlayers.firstOrNull { it.adapterPosition == position }
                    if (playerData == null)
                        return@launch
                    if (playerData.isPrepared && playerData.isPaused) {
                        playerData.isPaused = false
                        playerData.mediaPlayer.start()
                        Timber.tag(TAG).v("Audio PLayer Started ${playerData.adapterPosition}")
                    } else {
                        Timber.tag(TAG).v("Media not prepared.. ${playerData.adapterPosition}")
                    }
                    adapter.startProgressLoop(position)

                    // pause others
                    adapter.audioMediaPlayers.filterNot { it.adapterPosition == position }
                        .forEach { pd ->
                            if (pd.isPrepared && !pd.isPaused) {
                                pd.isPaused = true
                                pd.mediaPlayer.pause()
                                Timber.tag(TAG).v("Media Player Paused ${pd.adapterPosition}")
                            }
                        }
                } else {
                    // pause all
                    adapter.audioMediaPlayers.forEach { pd ->
                        if (pd.isPrepared && !pd.isPaused) {
                            pd.isPaused = true
                            pd.mediaPlayer.pause()
                            Timber.tag(TAG).v("All Media Player Paused ${pd.adapterPosition}")
                        }
                    }
                }
            }
        }
    }

    override fun getLayoutId() = R.layout.activity_player

    private val downloadProgressReceiver = object : DownloadProgressReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when {
                intent != null && intent.action == DOWNLOAD_IN_PROGRESS -> {
                    val progress = intent.getDoubleExtra(DOWNLOAD_PROGRESS_VALUE_KEY, 0.0)
                    val downloadId = intent.getLongExtra(DOWNLOAD_MANAGER_ID_KEY, -1L)
                    val instagramId =
                        intent.getStringExtra(DOWNLOAD_POST_INSTAGRAM_ID_KEY) ?: ""

                    Timber.tag(TAG)
                        .v("Progress: $progress, Current Download Id: ${downloadManager.getCurrentDownloadDbId()}")
                    CoroutineScope(Dispatchers.Main).launch {
                        adapter.itemDownloadingInfo.emit(
                            PlayerItemDownloadingInfo(
                                instagramId = instagramId,
                                currentProgress = progress,
                                downloadManagerId = downloadId
                            )
                        )
                    }
                }

                intent != null && intent.action == DOWNLOAD_POST_COMPLETED -> {
                    val instagramId =
                        intent.getStringExtra(DOWNLOAD_POST_INSTAGRAM_ID_KEY) ?: ""
                    CoroutineScope(Dispatchers.Main).launch {
                        adapter.itemDownloadingInfo.emit(
                            PlayerItemDownloadingInfo(
                                instagramId = instagramId,
                                currentProgress = -1.0,
                                downloadManagerId = -1L
                            )
                        )
//                        delay(100)
//                        adapter.itemDownloadingInfo.emit(null)
                    }
                }

                intent != null && intent.action == DOWNLOAD_LIST_POSTS_COMPLETED -> {
                    showMessage(R.string.media_downloaded_successfully)
                    showDownloadFlowFinishedAd()   // always show ad at the end of download list (including mass download)
                }
            }
        }
    }

    override fun onAudioMediaPrepared(mediaPlayer: MediaPlayer, position: Int) {
        if (binding.viewPager.currentItem == position) {
            if (adapter.posts[position] is PlayerUiModel.AudioMediaItem) {
                val playerData =
                    adapter.audioMediaPlayers.firstOrNull { it.adapterPosition == position }
                if (playerData == null)
                    return
                adapter.startProgressLoop(position)
                if (hasWindowFocus()) {
                    playerData.isPaused = false
                    playerData.mediaPlayer.start()
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val selectedPosition = intent.getIntExtra("scroll_to", 0)
        val sessionId = intent.getStringExtra("session_id")
        val dataReference = DataReference<TempPlayerDataModel>(sessionId = sessionId!!)

        val posts =
            dataReference.load(VideoDownloaderApp.application, TempPlayerDataModel::class)?.list

        val adapterPosts = mutableListOf<PlayerUiModel>()

        // Here I can add the native ad based on index calculation for example
        posts?.forEach { postData ->
            when (postData.mediaType) {
                1 -> adapterPosts.add(PlayerUiModel.ImageMediaItem(postData))
                2 -> adapterPosts.add(PlayerUiModel.VideoMediaItem(postData))
                8 -> adapterPosts.add(PlayerUiModel.CarouselMediaItem(postData))
                -1 -> adapterPosts.add(PlayerUiModel.AudioMediaItem(postData))

                -9 -> adapterPosts.add(PlayerUiModel.AdItem((postData as PlayerDataModel.AdMediaItem).provider, postData.adId))
            }
        }

        adapter.playerMediaListener = this
        adapter.audioMediaPreparedListener = this
        adapter.posts = adapterPosts
        binding.viewPager.adapter = adapter
        adapter.notifyDataSetChanged()

        if (selectedPosition != 0)
            binding.viewPager.setCurrentItem(selectedPosition, false)

        lifecycleScope.launch {
            viewModel
                .playerState
                .distinctUntilChanged(areEquivalent = { old, new ->
                    return@distinctUntilChanged old.isLoading != new.isLoading &&
                            old.isNetworkAvailable != new.isNetworkAvailable &&
                            old.error != new.error &&
                            old.currentUser != new.currentUser
                })
                .collect { state ->
                    if (state.isLoading) showLoading() else hideLoading()

                    if (state.isNetworkAvailable && state.currentUser == null) {
                        viewModel.getRemoteUser()
                    }

                    if (state.error != null
                        && state.isFileDownloading.not()
                        && state.isLoading.not()
                    ) showMessage(
                        state.error!!
                    )

                    if (state.isNetworkAvailable
                        && state.currentUser != null
                        && state.appConfig != null
                        && state.appConfig?.isAdsEnabled == true
                    ) initAds()

                    if (state.postWithMediaToDownload != null && state.isFileDownloading) {
                        CoroutineScope(Dispatchers.IO).launch {
                            downloadManager.addToDownloadItems(state.postWithMediaToDownload!!)
                        }
                    }

                    if (state.isFileDownloading && state.postWithMediaToDownload == null) {
                        // just a message for now
                        showMessage(R.string.download_started)
                    }
                }
        }

        lifecycleScope.launch {
            viewModel
                .playerChannel
                .collect { event ->
                    when (event) {
                        PlayerAction.OnShowShouldSubscribeDialog -> PlansActivity.open(
                            plansStartForResult,
                            this@PlayerActivity
                        )

                        is PlayerAction.OnShowReachedLimitWithoutLogin -> showDownloadLimitReached()
                        is PlayerAction.OnShowAdOrReviewRequiredDialog -> showRewardedAdOrReviewIsRequired(
                            true,
                            event.media
                        )

                        is PlayerAction.OnShowRewardedAdRequiredDialog -> showRewardedAdOrReviewIsRequired(
                            false,
                            event.media
                        )

                        PlayerAction.OnShowHighlightsToDownloadDialog -> {

                        }

                        PlayerAction.OnShowTempInvalidSubscription -> showTempInvalidSubscription()

                        PlayerAction.OnShowReachedMaxMassDownloadDialog -> showReachedMaxMassDownloadCount()
                        PlayerAction.OnShowFakeDrag -> showFakeDrag()
                        PlayerAction.OnHideAds -> {
                            admobInterstitialAd = null
                            downloadInterstitialAd = null
                            fbInterstitialAd = null
                            fbDownloadInterstitialAd = null
                            massDownloadsRewardedAd = null
                            admobRewardedAd = null
                            // ad here native ones
                        }
                    }
                }
        }

        lifecycleScope.launch {
            adapter
                .itemDownloadingInfo
                .distinctUntilChanged()
                .collect { downloadInfo ->
                    if (downloadInfo == null)
                        return@collect
                    Timber.tag(PlayerAdapter.TAG)
                        .v("Progress from Activity ${downloadInfo.currentProgress}, Id: ${downloadInfo.instagramId}")
                    adapter
                        .posts
                        .firstOrNull {
                            (it is PlayerUiModel.VideoMediaItem
                                    || it is PlayerUiModel.ImageMediaItem
                                    || it is PlayerUiModel.AudioMediaItem
                                    || it is PlayerUiModel.CarouselMediaItem)
                                    && it.mediaId == downloadInfo.instagramId
                        }?.let { playerUiModel ->

                            when (playerUiModel) {
                                is PlayerUiModel.VideoMediaItem -> {
                                    if (downloadInfo.currentProgress == -1.0) {
                                        if (playerUiModel.media is PlayerDataModel.DownloadedMediaItem) {
                                            onMediaDownloaded(
                                                playerUiModel.media,
                                                adapter.posts.indexOf(playerUiModel)
                                            )
                                        }
                                        adapter.notifyItemChanged(
                                            adapter.posts.indexOf(playerUiModel),
                                            PlayerAdapter.DownloadStatus(
                                                progress = downloadInfo.currentProgress,
                                                adapter.posts.indexOf(playerUiModel)
                                            )
                                        )
                                    } else {
                                        adapter.notifyItemChanged(
                                            adapter.posts.indexOf(playerUiModel),
                                            PlayerAdapter.DownloadStatus(
                                                progress = downloadInfo.currentProgress,
                                                adapter.posts.indexOf(playerUiModel)
                                            )
                                        )
                                    }
                                }

                                is PlayerUiModel.ImageMediaItem -> {
                                    if (playerUiModel.media is PlayerDataModel.DownloadedMediaItem) {
                                        onMediaDownloaded(
                                            playerUiModel.media,
                                            adapter.posts.indexOf(playerUiModel)
                                        )
                                    }
                                    adapter.notifyItemChanged(
                                        adapter.posts.indexOf(playerUiModel),
                                        PlayerAdapter.DownloadStatus(
                                            progress = downloadInfo.currentProgress,
                                            adapter.posts.indexOf(playerUiModel)
                                        )
                                    )
                                }

                                is PlayerUiModel.AudioMediaItem -> {
                                    if (downloadInfo.currentProgress == -1.0)           // audio update for download items
                                        onMediaDownloaded(
                                            playerUiModel.media as PlayerDataModel.DownloadedMediaItem,
                                            adapter.posts.indexOf(playerUiModel)
                                        )
                                    adapter.notifyItemChanged(
                                        adapter.posts.indexOf(playerUiModel),
                                        PlayerAdapter.DownloadStatus(
                                            progress = downloadInfo.currentProgress,
                                            adapter.posts.indexOf(playerUiModel)
                                        )
                                    )
                                }

                                is PlayerUiModel.CarouselMediaItem -> {
                                    if (downloadInfo.currentProgress == -1.0) {
                                        if (playerUiModel.media is PlayerDataModel.DownloadedMediaItem) {
                                            onMediaDownloaded(
                                                playerUiModel.media,
                                                adapter.posts.indexOf(playerUiModel)
                                            )
                                        }
                                        adapter.notifyItemChanged(
                                            adapter.posts.indexOf(playerUiModel),
                                            PlayerAdapter.DownloadStatus(
                                                progress = downloadInfo.currentProgress,
                                                adapter.posts.indexOf(playerUiModel)
                                            )
                                        )
                                    } else {
                                        adapter.notifyItemChanged(
                                            adapter.posts.indexOf(playerUiModel),
                                            PlayerAdapter.DownloadStatus(
                                                progress = downloadInfo.currentProgress,
                                                adapter.posts.indexOf(playerUiModel)
                                            )
                                        )
                                    }
                                }

                                is PlayerUiModel.AdItem -> {
                                    // NO-OP
                                }
                            }
                        }
                }
        }

        onBackPressedCallback = object : OnBackPressedCallback(enabled = true) {
            override fun handleOnBackPressed() {
                releaseMediaPlayer()
                if (admobInterstitialAd != null)
                    admobInterstitialAd?.show(this@PlayerActivity)
                else if (fbInterstitialAd != null && fbInterstitialAd?.isAdLoaded == true)
                    fbInterstitialAd?.show()
                else {
                    onBackPressedCallback.remove()
                    onBackPressedDispatcher.onBackPressed()
                }

                // TODO: show also the fb inter
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        binding.downloadedPostAction.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.Default)
            setContent {
                InstaDownloaderTheme {
                    DownloadedPostPlayerActions(
                        viewModel.selectedDownloadedPost,
                        onShare = {
                            viewModel.selectedDownloadedPost.value?.let { post ->

                                if (downloadManager.getCurrentDownloadDbId() == post.post.identifier) {
                                    showMessage(R.string.wait_until_download_finish)
                                    return@let
                                }

                                if (post.media.size > 1) {
                                    val files = ArrayList<File>()
                                    post.media.map { it.fileLocation }.forEach { filePath ->
                                        val file = File(filePath)
                                        if (file.exists()) {
                                            files.add(file)
                                        }
                                    }
                                    shareFiles(files, this@PlayerActivity)
                                } else if (post.media.size == 1) {
                                    val file = File(post.media.first().fileLocation)
                                    if (file.exists()) {
                                        shareFile(file, this@PlayerActivity)
                                    } else {
                                        showMessage(R.string.media_file_does_not_exist)
                                    }
                                }
                            }
                        },
                        onOpenInInstagram = {
                            openDownloadedPostInInstagram()
                        }
                    )

                    ExploreMediaPlayerActions(
                        selectedPost = viewModel.selectedExplorePost,
                        onShare = {
                            viewModel.selectedExplorePost.value?.let { post ->
                                val link = buildInstagramLink(post)
                                shareText(VideoDownloaderApp.application, link)
                            }
                        }) {
                        openInstagram(
                            buildInstagramLink(viewModel.selectedExplorePost.value!!),
                            VideoDownloaderApp.application
                        )
                    }

                    CollectionMediaPlayerActions(
                        selectedPost = viewModel.selectedCollectionMedia,
                        onShare = {
                            viewModel.selectedCollectionMedia.value?.let { post ->
                                val link = buildInstagramLink(post)
                                shareText(VideoDownloaderApp.application, link)
                            }
                        }) {
                        openInstagram(
                            buildInstagramLink(viewModel.selectedCollectionMedia.value!!),
                            VideoDownloaderApp.application
                        )
                    }
                }
            }
        }

        handler.postDelayed({
            viewModel.checkFakeDrag(posts?.first()!!)
        }, 1500)
    }


    /**
     *  Called after onResume(), and before onPause()
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        adapter.isWindowHasFocus.value = hasFocus
        if (adapter.posts.isEmpty())
            return
        if (hasFocus) {
            Timber.tag(TAG).v("Window has focus")
            when {
                adapter.posts[binding.viewPager.currentItem] is PlayerUiModel.VideoMediaItem -> {
                    val currentPlayer =
                        adapter.videoMediaPlayers.firstOrNull { it.adapterPosition == binding.viewPager.currentItem }
                    if (currentPlayer == null)
                        return
                    if (!currentPlayer.mediaPlayer.isReleased && !currentPlayer.mediaPlayer.isPlaying) {
                        currentPlayer.mediaPlayer.time = currentPlayer.mediaPlayer.time
                        currentPlayer.mediaPlayer.play()
                    }
                }

                adapter.posts[binding.viewPager.currentItem] is PlayerUiModel.AudioMediaItem -> {
                    val audioPlayer =
                        adapter.audioMediaPlayers.firstOrNull { it.adapterPosition == binding.viewPager.currentItem }
                    if (audioPlayer == null)
                        return
                    if (audioPlayer.isPrepared && audioPlayer.isPaused) {
                        audioPlayer.isPaused = false
                        audioPlayer.mediaPlayer.start()
                    }
                }

                adapter.posts[binding.viewPager.currentItem] is PlayerUiModel.CarouselMediaItem -> {
                    val carouselAdapter =
                        adapter.carouselAdapters.firstOrNull { it.position == binding.viewPager.currentItem }
                    if (carouselAdapter == null)
                        return

                    if (carouselAdapter.adapter.getItemViewType(carouselAdapter.adapter.currentSelectedItem) == 2) {
                        val currentPlayer =
                            carouselAdapter.adapter.videoMediaPlayers.firstOrNull { it.adapterPosition == carouselAdapter.adapter.currentSelectedItem }
                        if (currentPlayer == null)
                            return
                        if (!currentPlayer.mediaPlayer.isReleased && !currentPlayer.mediaPlayer.isPlaying) {
                            currentPlayer.mediaPlayer.time = currentPlayer.mediaPlayer.time
                            currentPlayer.mediaPlayer.play()
                        }
                    }
                }
            }
        } else {
            Timber.tag(TAG).v("Window does does not has focus")
            when {
                adapter.posts[binding.viewPager.currentItem] is PlayerUiModel.VideoMediaItem -> {
                    val currentPlayer =
                        adapter.videoMediaPlayers.firstOrNull { it.adapterPosition == binding.viewPager.currentItem }
                    if (currentPlayer == null)
                        return
                    if (!currentPlayer.mediaPlayer.isReleased && currentPlayer.mediaPlayer.isPlaying) {
                        currentPlayer.mediaPlayer.pause()
                    }
                }

                adapter.posts[binding.viewPager.currentItem] is PlayerUiModel.AudioMediaItem -> {
                    val audioPlayer =
                        adapter.audioMediaPlayers.firstOrNull { it.adapterPosition == binding.viewPager.currentItem }
                    if (audioPlayer == null)
                        return
                    if (audioPlayer.isPrepared && !audioPlayer.isPaused) {
                        audioPlayer.isPaused = true
                        audioPlayer.mediaPlayer.pause()
                    }
                }

                adapter.posts[binding.viewPager.currentItem] is PlayerUiModel.CarouselMediaItem -> {
                    val carouselAdapter =
                        adapter.carouselAdapters.firstOrNull { it.position == binding.viewPager.currentItem }
                    if (carouselAdapter == null)
                        return

                    if (carouselAdapter.adapter.getItemViewType(carouselAdapter.adapter.currentSelectedItem) == 2) {
                        val currentPlayer =
                            carouselAdapter.adapter.videoMediaPlayers.firstOrNull { it.adapterPosition == carouselAdapter.adapter.currentSelectedItem }
                        if (currentPlayer == null)
                            return
                        if (!currentPlayer.mediaPlayer.isReleased && currentPlayer.mediaPlayer.isPlaying) {
                            currentPlayer.mediaPlayer.pause()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        binding.downloadedPostAction.disposeComposition()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                downloadProgressReceiver, IntentFilter().apply {
                    addAction(DOWNLOAD_IN_PROGRESS)
                    addAction(DOWNLOAD_POST_COMPLETED)
                    addAction(DOWNLOAD_LIST_POSTS_COMPLETED)
                },
                RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(
                downloadProgressReceiver,
                IntentFilter().apply {
                    addAction(DOWNLOAD_IN_PROGRESS)
                    addAction(DOWNLOAD_POST_COMPLETED)
                    addAction(DOWNLOAD_LIST_POSTS_COMPLETED)
                },
            )
        }
        binding.viewPager.registerOnPageChangeCallback(pageChangeCallback)
    }


    override fun onPause() {
        super.onPause()
        unregisterReceiver(downloadProgressReceiver)
        binding.viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
    }

    /**
     *  The subscription check is not done in the viewModel
     */
    private fun initAds() {
        if (
            viewModel.currentRemoteUser != null
            && viewModel.currentRemoteUser?.hasValidSubscription() == false
        ) {
            lifecycleScope.launch {
                viewModel
                    .appConfig
                    .collectLatest { appConfig ->
                        appConfig?.advertisements?.forEachIndexed { _, ad ->
                            when {
                                ad.isEnabled
                                        && ad.type == "interstitial"
                                        && ad.name == "player_interstitial_1"
                                        && ad.provider == "facebook" -> {
                                    runOnUiThread {
                                        fbInterstitialAd =
                                            FbInterstitialAd(this@PlayerActivity, ad.adId)
                                        fbInterstitialAd?.buildLoadAdConfig()?.withAdListener(
                                            object : InterstitialAdListener {
                                                override fun onError(p0: Ad?, p1: AdError?) {
                                                    Timber.tag(TAG)
                                                        .i("Error loading fb inter ${p1?.errorMessage}")
                                                }

                                                override fun onAdLoaded(p0: Ad?) {
                                                    //
                                                }

                                                override fun onAdClicked(p0: Ad?) {
                                                    //
                                                }

                                                override fun onLoggingImpression(p0: Ad?) {
                                                    //
                                                }

                                                override fun onInterstitialDisplayed(p0: Ad?) {
                                                    //
                                                }

                                                override fun onInterstitialDismissed(p0: Ad?) {
                                                    runOnUiThread {
                                                        finish()
                                                    }
                                                }
                                            }
                                        )?.build()
                                    }
                                }

                                ad.isEnabled
                                        && ad.type == "interstitial"
                                        && ad.name == "player_interstitial_1"
                                        && ad.provider == "admob" -> {
                                    runOnUiThread {
                                        InterstitialAd.load(
                                            this@PlayerActivity,
                                            ad.adId,
                                            AdRequest.Builder().build(),
                                            object : InterstitialAdLoadCallback() {
                                                override fun onAdFailedToLoad(p0: LoadAdError) {
                                                    Timber.tag(TAG)
                                                        .v("Failed to load inter ${p0.message}")
                                                }

                                                override fun onAdLoaded(p0: InterstitialAd) {
                                                    admobInterstitialAd = p0
                                                }
                                            }
                                        )
                                    }
                                }

                                ad.isEnabled
                                        && ad.type == "rewarded"
                                        && ad.name == "player_rewarded"
                                        && ad.provider == "admob" -> {
                                    RewardedAd.load(
                                        this@PlayerActivity,
                                        ad.adId,
                                        AdRequest.Builder().build(),
                                        object : RewardedAdLoadCallback() {
                                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                                Timber.tag(HomeActivity.TAG).e(p0.toString())
                                                admobRewardedAd = null
                                            }

                                            override fun onAdLoaded(p0: RewardedAd) {
                                                admobRewardedAd = p0
                                            }
                                        })
                                }

                                ad.isEnabled
                                        && ad.type == "rewarded"
                                        && ad.name == "player_mass_downloads_rewarded_ad"
                                        && ad.provider == "admob" -> {
                                    RewardedAd.load(
                                        this@PlayerActivity,
                                        ad.adId,
                                        AdRequest.Builder().build(),
                                        object : RewardedAdLoadCallback() {
                                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                                Timber.tag(HomeActivity.TAG).e(p0.toString())
                                                massDownloadsRewardedAd = null
                                            }

                                            override fun onAdLoaded(p0: RewardedAd) {
                                                massDownloadsRewardedAd = p0
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
            }
        }
    }


    private fun showFakeDrag() {
        if (!binding.viewPager.isFakeDragging) {
            binding.viewPager.beginFakeDrag()
        }

        binding.viewPager.fakeDragBy(-binding.viewPager.height * 0.22f)
        handler.postDelayed({
            if (binding.viewPager.isFakeDragging)
                binding.viewPager.endFakeDrag()
        }, 500)
    }

    // region Audio Listener

    /**
     *  Update the downloaded item, this method is called from downloaded items only.
     *  This method is helpful when the user delete a profile, and re-download the media
     *  from other profile.
     */
    override suspend fun onMediaDownloaded(
        media: PlayerDataModel.DownloadedMediaItem,
        position: Int
    ) {
        // to update the downloaded item in the adapter list
        val postDownloadedId = media.media.post.mediaId
        val newPost = viewModel.getUpdatedItem(postDownloadedId)
        Timber.tag(TAG)
            .v("Post updated after download: ${newPost.media.media.first().fileLocation}")
        when (media.mediaType) {
            1 -> adapter.posts[position] = PlayerUiModel.ImageMediaItem(newPost)
            2 -> adapter.posts[position] = PlayerUiModel.VideoMediaItem(newPost)
            8 -> adapter.posts[position] = PlayerUiModel.CarouselMediaItem(newPost)
            -1 -> adapter.posts[position] = PlayerUiModel.AudioMediaItem(newPost)
        }
    }

    override fun onDownloadClick(media: PlayerDataModel) {
        when (media) {
            is PlayerDataModel.DownloadedMediaItem -> {
                when (media.mediaType) {                // which I set in the onCreate method
                    -1, 1, 2 -> {
                        val file = File(media.media.media.first().fileLocation)
                        if (file.exists()) {
                            showMessage(R.string.file_already_exist)
                            return
                        }
                        loadDownloadInterstitialAd()
                        viewModel.onStartDownload(media)
                    }

                    8 -> {
                        showReDownloadConfirmation(media)
                    }
                }
            }

            is PlayerDataModel.ExploreMediaItem -> {
                when (media.mediaType) {
                    1 -> {
                        val probableFileLocation = "${
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES
                            )
                        }/${viewModel.currentLoggedInUser.value.username}/${media.media.id}.${
                            getExtension(media.media.imageVersions2?.candidates?.first()?.url!!)
                        }"

                        val file = File(probableFileLocation)
                        if (file.exists()) {
                            showMessage(R.string.file_already_exist)
                            return
                        }
                        loadDownloadInterstitialAd()
                        viewModel.onStartDownload(media)
                    }

                    2 -> {
                        val probableFileLocation = "${
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_MOVIES
                            )
                        }/${viewModel.currentLoggedInUser.value.username}/${media.media.id}.${
                            getExtension(media.media.videoVersions?.first()?.url!!)
                        }"

                        val file = File(probableFileLocation)
                        if (file.exists()) {
                            showMessage(R.string.file_already_exist)
                            return
                        }
                        loadDownloadInterstitialAd()
                        viewModel.onStartDownload(media)
                    }

                    8 -> {
                        loadDownloadInterstitialAd()
                        viewModel.onStartDownload(media)
                    }
                }
            }

            is PlayerDataModel.CollectionMediaItem -> {
                when (media.mediaType) {
                    1 -> {
                        val probableFileLocation = "${
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES
                            )
                        }/${viewModel.currentLoggedInUser.value.username}/${media.media.id}.${
                            getExtension(
                                media.media.imageVersions2?.candidates?.first()?.url!!
                            )
                        }"

                        val file = File(probableFileLocation)
                        if (file.exists()) {
                            showMessage(R.string.file_already_exist)
                            return
                        }
                        loadDownloadInterstitialAd()
                        viewModel.onStartDownload(media)
                    }

                    2 -> {
                        val probableFileLocation = "${
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_MOVIES
                            )
                        }/${viewModel.currentLoggedInUser.value.username}/${media.media.id}.${
                            getExtension(media.media.videoVersions?.first()?.url!!)
                        }"

                        val file = File(probableFileLocation)
                        if (file.exists()) {
                            showMessage(R.string.file_already_exist)
                            return
                        }
                        loadDownloadInterstitialAd()
                        viewModel.onStartDownload(media)
                    }

                    8 -> {
                        loadDownloadInterstitialAd()
                        viewModel.onStartDownload(media)
                    }
                }
            }

            else -> {
                // NO-OP
            }
        }
    }

    override fun onMoreClick(media: PlayerDataModel) {
        when (media) {
            is PlayerDataModel.DownloadedMediaItem -> viewModel.selectedDownloadedPost.value =
                media.media

            is PlayerDataModel.ExploreMediaItem -> viewModel.selectedExplorePost.value = media.media
            is PlayerDataModel.CollectionMediaItem -> viewModel.selectedCollectionMedia.value =
                media.media

            is PlayerDataModel.AdMediaItem -> {
                // NO-OP
            }
        }
    }

    // endregion

    private fun showDownloadLimitReached() {
        val dialog = BaseAlertDialog(this)

        dialog.addButton(
            R.string.subscribe_for_unlimited,
            isPrimary = true,
            isSubscribeButton = true,
            autoDismiss = true,
        ) { _, _ ->
            PlansActivity.open(plansStartForResult, this@PlayerActivity)
        }

        dialog.setTitle(R.string.download_reached_limit)
        dialog.setMessage(R.string.download_reached_limit_message_without_ads_and_review)
        dialog.setExtraCloseButtonVisible(true)
        dialog.show()
    }

    private fun showRewardedAdOrReviewIsRequired(isReview: Boolean, media: PlayerDataModel) {
        val dialog = BaseAlertDialog(this)
        dialog.setTitle(R.string.download_reached_limit)
        dialog.addButton(
            R.string.subscribe_for_unlimited,
            isPrimary = true,
            isSubscribeButton = true,
            autoDismiss = false,
        ) { _, _ ->
            PlansActivity.open(plansStartForResult, this@PlayerActivity)
        }

        if (isReview) {
            dialog.setMessage(R.string.download_reached_limit_message)
            dialog.addButton(
                R.string.give_five_stars_review,
                isPrimary = true,
                isDestructive = false
            ) { _, _ ->
                val manager = ReviewManagerFactory.create(this)
                val request = manager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        val flow = manager.launchReviewFlow(this, reviewInfo)
                        flow.addOnCompleteListener { task2 ->
                            if (task2.isSuccessful) {
                                viewModel.onStartDownload(
                                    media,
                                    isAfterReview = true,
                                    isAfterRewardedAd = false
                                )
                            } else {
                                Timber.tag(TAG).v("User canceled the review")
                            }
                        }
                    } else {
                        showMessage(R.string.can_t_open_google_review_dialog_try_again)
                        Timber.tag(TAG).i((task.exception))
                        PlansActivity.open(plansStartForResult, this@PlayerActivity)
                    }
                }
            }
        } else
            dialog.setMessage(R.string.download_reached_limit_message_without_review)

        dialog.addButton(R.string.watch_an_ad, isPrimary = true) { _, _ ->
            if (admobRewardedAd != null) {
                admobRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        admobRewardedAd = null
                        reloadRewardedAd()
                    }
                }
                admobRewardedAd?.show(this) {
                    Timber.tag(TAG).v("User earned rewarded")
                    viewModel.onStartDownload(
                        media = media,
                        isAfterReview = false,
                        isAfterRewardedAd = true
                    )
                }
            } else {
                showMessage(R.string.ad_not_available_now)
                PlansActivity.open(plansStartForResult, this@PlayerActivity)
            }
        }
        dialog.setExtraCloseButtonVisible(false)
        dialog.show()
    }

    private fun reloadRewardedAd() {
        lifecycleScope.launch {
            viewModel.appConfig.collectLatest { config ->
                config?.advertisements?.firstOrNull {
                    it.isEnabled
                            && it.type == "rewarded"
                            && it.name == "player_rewarded"
                            && it.provider == "admob"
                }?.let { ad ->
                    RewardedAd.load(
                        this@PlayerActivity,
                        ad.adId,
                        AdRequest.Builder().build(),
                        object : RewardedAdLoadCallback() {
                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                Timber.tag(TAG)
                                    .e(p0.toString())
                                admobRewardedAd = null
                            }

                            override fun onAdLoaded(p0: RewardedAd) {
                                admobRewardedAd = p0
                            }
                        })
                }
            }
        }
    }

    private fun openDownloadedPostInInstagram() {
        openInstagram(
            buildInstagramLink(viewModel.selectedDownloadedPost.value!!),
            this
        )
    }

    private fun showTempInvalidSubscription() {
        val dialog = BaseAlertDialog(this)
        dialog.setTitle(R.string.invalid_subscription)
        dialog.setMessage(R.string.invalid_subscription_message)
        dialog.setExtraCloseButtonVisible(true)
        dialog.show()
    }

    private fun showReDownloadConfirmation(media: PlayerDataModel) {
        val dialog = BaseAlertDialog(this)
        dialog.setTitle(R.string.re_download_confirmation)
        dialog.setMessage(R.string.re_download_confirmation_text)
        dialog.addButton(R.string.yes, isPrimary = true, isDestructive = true) { _, _ ->
            loadDownloadInterstitialAd()
            viewModel.onStartDownload(media)
        }
        dialog.setExtraCloseButtonVisible(true)
        dialog.show()
    }

    private fun loadDownloadInterstitialAd() {
        if (
            viewModel.currentRemoteUser != null
            && viewModel.currentRemoteUser?.hasValidSubscription() == false
            && viewModel.appConfig.value != null
            && viewModel.appConfig.value?.isAdsEnabled == true
        ) {
            viewModel.appConfig.value?.advertisements?.forEach { ad ->
                when {
                    ad.isEnabled
                            && ad.type == "interstitial"
                            && ad.name == "player_interstitial_2"
                            && ad.provider == "admob" -> {
                        runOnUiThread {
                            InterstitialAd.load(
                                this@PlayerActivity,
                                ad.adId,
                                AdRequest.Builder().build(),
                                object : InterstitialAdLoadCallback() {
                                    override fun onAdFailedToLoad(p0: LoadAdError) {
                                        Timber.tag(TAG).v("Failed to load inter ${p0.message}")
                                    }

                                    override fun onAdLoaded(p0: InterstitialAd) {
                                        downloadInterstitialAd = p0
                                    }
                                }
                            )
                        }
                    }

                    ad.isEnabled
                            && ad.type == "interstitial"
                            && ad.name == "player_interstitial_2"
                            && ad.provider == "facebook" -> {
                        runOnUiThread {
                            fbDownloadInterstitialAd =
                                FbInterstitialAd(this@PlayerActivity, ad.adId)
                            fbDownloadInterstitialAd?.buildLoadAdConfig()?.withAdListener(
                                object : InterstitialAdListener {
                                    override fun onError(p0: Ad?, p1: AdError?) {
                                        Timber.tag(TAG)
                                            .i("Error loading fb inter ${p1?.errorMessage}")
                                    }

                                    override fun onAdLoaded(p0: Ad?) {
                                        //
                                    }

                                    override fun onAdClicked(p0: Ad?) {
                                        //
                                    }

                                    override fun onLoggingImpression(p0: Ad?) {
                                        //
                                    }

                                    override fun onInterstitialDisplayed(p0: Ad?) {
                                        //
                                    }

                                    override fun onInterstitialDismissed(p0: Ad?) {
                                        // NO-OP
                                    }
                                }
                            )?.build()
                        }
                    }

                }
            }
        }
    }

    private fun showDownloadFlowFinishedAd() {
        handler.postDelayed({
            runOnUiThread {
                if (downloadInterstitialAd != null) {
                    downloadInterstitialAd?.show(this)
                } else if (fbDownloadInterstitialAd != null && fbDownloadInterstitialAd?.isAdLoaded == true) {
                    fbDownloadInterstitialAd?.show()
                }
            }
        }, 20)
    }

    private fun showReachedMaxMassDownloadCount() {
        val dialog = BaseAlertDialog(this)
        dialog.setTitle(R.string.mass_download_limit_title)
        dialog.setMessage(R.string.mass_download_limit)
        dialog.addButton(
            R.string.subscribe_for_unlimited_mass_download,
            isPrimary = true,
            isSubscribeButton = true,
            autoDismiss = true,
        ) { _, _ ->
            PlansActivity.open(plansStartForResult, this@PlayerActivity)
        }

        dialog.addButton(
            getString(
                R.string.watch_an_ad_get_n_mass_download,
                viewModel.appConfig.value?.massDownloadsToGetAsReward.toString()
            ), isPrimary = true
        ) { _, _ ->
            if (massDownloadsRewardedAd != null) {
                massDownloadsRewardedAd?.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            massDownloadsRewardedAd = null
                            reloadMassRewardedAd()
                        }
                    }
                massDownloadsRewardedAd?.show(this) {
                    Timber.tag(TAG).v("User earned reward for mass downloads")
                    viewModel.setUserNewMassDownloadCount()
                }
            } else {
                showMessage(R.string.ad_not_available_now)
                PlansActivity.open(plansStartForResult, this@PlayerActivity)
            }
        }
        dialog.setExtraCloseButtonVisible(false)
        dialog.show()
    }


    /**
     *  This method is called after I am sure I get the app config from collection
     *  in the initAds Method.
     */
    private fun reloadMassRewardedAd() {
        if (viewModel.appConfig.value != null
            && viewModel.appConfig.value?.isAdsEnabled == true
            && viewModel.currentRemoteUser != null
            && viewModel.currentRemoteUser?.hasValidSubscription() == false
        ) {
            viewModel.appConfig.value?.advertisements?.firstOrNull {
                it.isEnabled
                        && it.type == "rewarded"
                        && it.name == "player_mass_downloads_rewarded_ad"
                        && it.provider == "admob"
            }?.let { ad ->
                RewardedAd.load(
                    this@PlayerActivity,
                    ad.adId,
                    AdRequest.Builder().build(),
                    object : RewardedAdLoadCallback() {
                        override fun onAdFailedToLoad(p0: LoadAdError) {
                            Timber.tag(HomeActivity.TAG).e(p0.toString())
                            massDownloadsRewardedAd = null
                        }

                        override fun onAdLoaded(p0: RewardedAd) {
                            massDownloadsRewardedAd = p0
                        }
                    })
            }
        }
    }

    private fun releaseMediaPlayer() {
        adapter.carouselAdapters.forEach {
            it.adapter.videoMediaPlayers.forEach { p ->
                p.mediaPlayer.media?.release()
                p.mediaPlayer.release()
            }
        }
        adapter.audioJobs.forEach {
            it.job.cancel()
        }
        adapter.audioMediaPlayers.forEach {
            it.isPrepared = false
            it.mediaPlayer.release()
        }
        adapter.videoMediaPlayers.forEach {
            if (!it.mediaPlayer.isReleased) {
                it.mediaPlayer.media?.release()
                it.mediaPlayer.release()
            }
        }
    }

    override fun onDestroy() {
        releaseMediaPlayer()
        super.onDestroy()
    }
}