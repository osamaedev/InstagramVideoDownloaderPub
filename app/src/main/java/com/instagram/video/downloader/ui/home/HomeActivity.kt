package com.instagram.video.downloader.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.onNavDestinationSelected
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.play.core.review.ReviewManagerFactory
import com.instagram.video.downloader.R
import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.common.cleanShareLink
import com.instagram.video.downloader.common.download.VDDownloadManager
import com.instagram.video.downloader.common.clearCookies
import com.instagram.video.downloader.common.download.DownloadProgressReceiver
import com.instagram.video.downloader.common.download.DownloadProgressReceiver.Companion.DOWNLOAD_IN_PROGRESS
import com.instagram.video.downloader.common.download.DownloadProgressReceiver.Companion.DOWNLOAD_LIST_POSTS_COMPLETED
import com.instagram.video.downloader.common.download.DownloadProgressReceiver.Companion.DOWNLOAD_POST_COMPLETED
import com.instagram.video.downloader.common.isAudioLink
import com.instagram.video.downloader.common.isHighlightShareLink
import com.instagram.video.downloader.common.isValidInstagramLink
import com.instagram.video.downloader.common.openAppInPlayStore
import com.instagram.video.downloader.common.openInstagram
import com.instagram.video.downloader.databinding.ActivityHomeBinding
import com.instagram.video.downloader.ui.base.BaseActivity
import com.instagram.video.downloader.ui.base.BaseAlertDialog
import com.instagram.video.downloader.ui.base.showMessage
import com.instagram.video.downloader.ui.faq.FaqActivity
import com.instagram.video.downloader.ui.home.explore.MassItemDownloadingInfo
import com.instagram.video.downloader.ui.home.highlightToDownload.HighlightToDownloadBottomSheet
import com.instagram.video.downloader.ui.howTo.HowToDownloadActivity
import com.instagram.video.downloader.ui.plans.PlansActivity
import com.instagram.video.downloader.ui.splash.SplashActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import com.facebook.ads.InterstitialAd as FbInterstitialAd


@AndroidEntryPoint
class HomeActivity : BaseActivity<ActivityHomeBinding>() {


    companion object {
        const val TAG = "HomeActivity"
        fun getIntent(context: Context) = Intent(context, HomeActivity::class.java)
        fun getIntent(context: Context, extraLink: String): Intent {
            val intent =
                Intent(context, HomeActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("EXTRA_TEXT", extraLink)
            return intent
        }

    }

    private val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var downloadManager: VDDownloadManager

    private val plansStartForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                viewModel.updateRemoteUser()
            }
        }

    private var admobInterstitialAd: InterstitialAd? = null
        set(value) {
            field = value
            if (value != null) {
                value.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        reloadAdmobInterstitial()
                    }
                }
            }
        }


    private var fbInterstitialAd: FbInterstitialAd? = null

    private var admobRewardedAd: RewardedAd? = null
    private var massDownloadsRewardedAd: RewardedAd? = null
    private var loginDialog: InstagramLoginBottomSheet? = null

    var bannerAdView: AdView? = null

    private val downloadProgressReceiver = object : DownloadProgressReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when {
                intent != null && intent.action == DOWNLOAD_IN_PROGRESS -> {
                    val progress = intent.getDoubleExtra(DOWNLOAD_PROGRESS_VALUE_KEY, 0.0)
                    val downloadId = intent.getLongExtra(DOWNLOAD_MANAGER_ID_KEY, -1L)
                    val instagramId = intent.getStringExtra(DOWNLOAD_POST_INSTAGRAM_ID_KEY)
                    val isMassDownloadPost =
                        intent.getBooleanExtra(DOWNLOAD_POST_IS_MASS_DOWNLOAD, false)

                    Timber.tag(TAG)
                        .v("Progress: $progress, Current Download Id: ${downloadManager.getCurrentDownloadDbId()}")

                    if (progress > 0.0) {
                        CoroutineScope(Dispatchers.Main).launch {
                            viewModel.massDownloadItemDownloadingInfo.emit(
                                MassItemDownloadingInfo(
                                    instagramId = instagramId.toString(),
                                    currentProgress = progress,
                                    downloadManagerId = downloadId,
                                )
                            )
                        }
                    }

                    if (isMassDownloadPost)
                        return

                    if (progress > 0.0) {
                        binding.progress.scaleY = 1.0f
                        binding.progress.isVisible = true
                        binding.progress.isIndeterminate = false
                        binding.progress.progress = progress.toInt()
                    }
                    if (downloadId == -1L || progress >= 100.0) {    // downloadId = -1L means list completed
                        handler.postDelayed({ binding.progress.isVisible = false }, 800)
                    }

                }

                // this is for a single download, or user set download media by media
                intent != null && intent.action == DOWNLOAD_POST_COMPLETED -> {
                    val instagramId = intent.getStringExtra(DOWNLOAD_POST_INSTAGRAM_ID_KEY)
                    val isMassDownloadPost =
                        intent.getBooleanExtra(DOWNLOAD_POST_IS_MASS_DOWNLOAD, false)

                    // for the mass just send the downloadManagerId set to -1L
                    CoroutineScope(Dispatchers.Main).launch {
                        viewModel.massDownloadItemDownloadingInfo.emit(
                            MassItemDownloadingInfo(
                                instagramId = instagramId.toString(),
                                currentProgress = 100.0,
                                downloadManagerId = -1L,
                            )
                        )
                    }

                    viewModel.reloadDownloadedPosts()

                    // this is only when not a mass download post
                    if (!isMassDownloadPost) {
                        handler.postDelayed({ binding.progress.isVisible = false }, 800)
                        hideFilesAreDownloadingDialog()
                    }
                }

                // this is for mass download items and also triggered when a single media is downloaded
                intent != null && intent.action == DOWNLOAD_LIST_POSTS_COMPLETED -> {
                    showDownloadFlowFinishedAd()   // always show ad at the end of download list (including mass download)
                    showHomeBadge()
                }
            }
        }
    }


    override fun getLayoutId() = R.layout.activity_home

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder(StrictMode.getVmPolicy()).detectLeakedClosableObjects()
                .build()
        )

        setSupportActionBar(findViewById(R.id.toolbar))
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        val topLevelIds = setOf(
            R.id.navigation_home,
            R.id.navigation_explore,
            R.id.navigation_profiles,
            R.id.navigation_settings,
        )

        supportActionBar?.setDisplayShowTitleEnabled(false)

        val appBarConfiguration = AppBarConfiguration(topLevelIds)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navView.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.navigation_home)
                clearHomeBadge()
            onNavDestinationSelected(
                item,
                navController
            )
        }

        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_toolbar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {

                    R.id.remove_ads_menu_item -> {
                        PlansActivity.open(plansStartForResult, this@HomeActivity)
                    }

                    R.id.instagram_menu_item -> {
                        openInstagram(
                            "https://www.instagram.com/",
                            this@HomeActivity
                        )
                    }
                }
                return false
            }
        }, this, Lifecycle.State.CREATED)

        lifecycleScope.launch {
            viewModel
                .homeState
                .distinctUntilChanged(areEquivalent = { old, new ->
                    return@distinctUntilChanged old.isLoading != new.isLoading &&
                            old.isNetworkAvailable != new.isNetworkAvailable &&
                            old.isExtractingUserInfoFailed != new.isNetworkAvailable &&
                            old.isFileDownloading != new.isFileDownloading &&
                            old.currentUser != new.currentUser &&
                            old.error != new.error &&
                            old.postWithMediaToDownload != new.postWithMediaToDownload
                })
                .collect { state ->
                    if (state.isLoading) showLoading() else hideLoading()

                    if (state.error != null
                        && state.isFileDownloading.not()
                        && state.isLoading.not()
                    ) showMessage(
                        state.error!!
                    )

                    if (state.isExtractingUserInfoFailed) {
                        clearCookies()
                    }

                    if (state.isNetworkAvailable && state.currentUser == null) {
                        viewModel.getUserAndCheckCurrentVersion()
                    }

                    if (state.postWithMediaToDownload != null && state.isFileDownloading) {
                        showIndeterminateProgressbar()
                        CoroutineScope(Dispatchers.IO).launch {
                            downloadManager.addToDownloadItems(state.postWithMediaToDownload!!)
                        }
                    }

                    if (state.isFileDownloading) showFilesAreDownloadingDialog()
                }
        }

        lifecycleScope.launch {
            viewModel
                .homeChannel
                .collect { event ->
                    when (event) {
                        HomeAction.OnJustOpenSplash -> {
                            startActivity(SplashActivity.getIntent(this@HomeActivity))
                            finish()
                        }

                        HomeAction.OnCheckIntent -> onNewIntent(intent)
                        HomeAction.OnShowFaq -> openFaqActivity()
                        HomeAction.OnShowInstagramLogin -> showInstagramLoginWebView()
                        HomeAction.OnOpenHowToDownload -> {
                            startActivity(
                                HowToDownloadActivity.getIntent(
                                    this@HomeActivity
                                )
                            )
                        }

                        HomeAction.OnOpenInstagram -> openInstagram(
                            "https://www.instagram.com",
                            this@HomeActivity
                        )

                        HomeAction.OnShowDownloadInputDialog -> showDownloadBoxBottomSheet()
                        HomeAction.OnAddNewUser -> {
                            clearCookies()
                            showInstagramLoginWebView(true)
                        }

                        HomeAction.OnShowVersionNoLongerSupported -> showUpdateVersionDialog()
                        HomeAction.OnLoadInterAds -> initAds()
                        HomeAction.OnLoadNativeAds -> { /* NO-OP */
                        }

                        HomeAction.OnShowShouldSubscribeDialog -> PlansActivity.open(
                            plansStartForResult,
                            this@HomeActivity
                        )

                        is HomeAction.OnShowReachedLimitWithoutLogin -> showDownloadLimitReached()
                        HomeAction.OnShowTempInvalidSubscription -> showTempInvalidSubscription()
                        is HomeAction.OnShowRewardedAdRequiredDialog -> showRewardedAdOrReviewIsRequired(
                            false,
                            event.mediaUrl
                        )

                        is HomeAction.OnShowAdOrReviewRequiredDialog -> showRewardedAdOrReviewIsRequired(
                            true,
                            event.mediaUrl
                        )

                        HomeAction.OnShowLoginRequiredForPrivatePost -> showLoginRequiredForPrivatePosts()
                        HomeAction.OnShowHighlightsToDownloadDialog -> showHighlightsToDownloadDialog()
                        is HomeAction.OnShowMessage -> showMessage(event.message)

                        HomeAction.HideAds -> {
                            fbInterstitialAd = null
                            admobInterstitialAd = null
                            admobRewardedAd = null
                        }
                    }
                }
        }
    }

    override fun onSupportNavigateUp() = findNavController(R.id.nav_host_fragment).navigateUp()


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
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
        viewModel.reloadDownloadedPosts()
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val receivedUri = intent.extras?.getString("EXTRA_TEXT")
        if (receivedUri != null && viewModel.currentRemoteUser != null) {
            if (!isValidInstagramLink(receivedUri)
                && !isHighlightShareLink(receivedUri)
                && !isAudioLink(receivedUri)
            ) {
                Timber.tag(TAG).v("Invalid instagram link")
            } else {
                if (isNetworkConnected()) {
                    viewModel.onStartDownload(cleanShareLink(receivedUri))
                } else {
                    hideLoading()
                    showMessage(R.string.network_not_available)
                }
            }
        } else if (receivedUri != null && viewModel.currentRemoteUser == null) {
            viewModel.setIntentDownloadLink(receivedUri)
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(downloadProgressReceiver)
    }

    private fun showInstagramLoginWebView(isFromProfiles: Boolean = false) {
        loginDialog = InstagramLoginBottomSheet.getInstance(Bundle().apply {
            this.putBoolean("is_from_profiles", isFromProfiles)
        })
        loginDialog?.show(supportFragmentManager, InstagramLoginBottomSheet.TAG)
    }

    private fun openFaqActivity() {
        startActivity(FaqActivity.getIntent(this))
    }

    private fun showDownloadBoxBottomSheet() {
        LinkToDownloadBottomSheet.getInstance(Bundle()).apply {
            show(supportFragmentManager, LinkToDownloadBottomSheet.TAG)
        }
    }


    /**
     *  This method is called always after we set the user from remote,
     *  and checked user subscription validity in the viewModel
     */
    private fun initAds() {
        if (viewModel.currentRemoteUser != null) {
            lifecycleScope.launch {
                viewModel.appConfig.collectLatest { appConfig ->
                    appConfig?.advertisements?.forEachIndexed { _, ad ->
                        when {
                            ad.isEnabled && ad.type == "interstitial" && ad.name == "interstitial_1" && ad.provider == "admob" -> {
                                runOnUiThread {
                                    InterstitialAd.load(
                                        this@HomeActivity,
                                        ad.adId,
                                        AdRequest.Builder().build(),
                                        object : InterstitialAdLoadCallback() {
                                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                                Timber.tag(TAG)
                                                    .i("Failed to load inter 1 ${p0.message}")
                                            }

                                            override fun onAdLoaded(p0: InterstitialAd) {
                                                admobInterstitialAd = p0
                                            }
                                        })
                                }
                            }

                            ad.isEnabled && ad.type == "interstitial" && ad.name == "interstitial_1" && ad.provider == "facebook" -> {
                                fbInterstitialAd = FbInterstitialAd(this@HomeActivity, ad.adId)
                                fbInterstitialAd?.loadAd(
                                    fbInterstitialAd?.buildLoadAdConfig()
                                        ?.withAdListener(object : InterstitialAdListener {
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
                                                handler.postDelayed({
                                                    reloadFacebookInterstitial()
                                                }, 20)
                                            }
                                        })?.build()
                                )
                            }

                            ad.isEnabled && ad.type == "open_ad" && ad.name == "open_ad_1" && ad.provider == "admob" -> {
                                VideoDownloaderApp.application.initOpenAd(ad.adId)
                            }

                            ad.isEnabled && ad.type == "rewarded" && ad.name == "rewarded_1" && ad.provider == "admob" -> {
                                RewardedAd.load(
                                    this@HomeActivity,
                                    ad.adId,
                                    AdRequest.Builder().build(),
                                    object : RewardedAdLoadCallback() {
                                        override fun onAdFailedToLoad(p0: LoadAdError) {
                                            Timber.tag(TAG).e(p0.toString())
                                            admobRewardedAd = null
                                        }

                                        override fun onAdLoaded(p0: RewardedAd) {
                                            admobRewardedAd = p0
                                        }
                                    })
                            }

                            ad.isEnabled && ad.type == "rewarded" && ad.name == "mass_downloads_rewarded_ad" && ad.provider == "admob" -> {
                                RewardedAd.load(
                                    this@HomeActivity,
                                    ad.adId,
                                    AdRequest.Builder().build(),
                                    object : RewardedAdLoadCallback() {
                                        override fun onAdFailedToLoad(p0: LoadAdError) {
                                            Timber.tag(TAG).e(p0.toString())
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

    private fun reloadMassRewardedAd() {
        lifecycleScope.launch {
            viewModel.appConfig.collectLatest { config ->
                config?.advertisements?.firstOrNull {
                    it.isEnabled
                            && it.type == "rewarded"
                            && it.name == "mass_downloads_rewarded_ad"
                            && it.provider == "admob"
                }?.let { ad ->
                    RewardedAd.load(
                        this@HomeActivity,
                        ad.adId,
                        AdRequest.Builder().build(),
                        object : RewardedAdLoadCallback() {
                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                Timber.tag(TAG).e(p0.toString())
                                massDownloadsRewardedAd = null
                            }

                            override fun onAdLoaded(p0: RewardedAd) {
                                massDownloadsRewardedAd = p0
                            }
                        })
                }
            }
        }
    }

    private fun reloadRewardedAd() {
        lifecycleScope.launch {
            viewModel.appConfig.collectLatest { config ->
                config?.advertisements?.firstOrNull {
                    it.isEnabled
                            && it.type == "rewarded"
                            && it.name == "rewarded_1"
                            && it.provider == "admob"
                }?.let { ad ->
                    RewardedAd.load(
                        this@HomeActivity,
                        ad.adId,
                        AdRequest.Builder().build(),
                        object : RewardedAdLoadCallback() {
                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                Timber.tag(TAG).e(p0.toString())
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

    private fun reloadAdmobInterstitial() {
        if (viewModel.currentRemoteUser?.subscription == null) {
            lifecycleScope.launch {
                viewModel.appConfig.collectLatest { appConfig ->
                    appConfig?.advertisements?.forEachIndexed { _, ad ->
                        when {
                            ad.isEnabled && ad.type == "interstitial" && ad.name == "interstitial_1" && ad.provider == "admob" -> {
                                runOnUiThread {
                                    InterstitialAd.load(
                                        this@HomeActivity,
                                        ad.adId,
                                        AdRequest.Builder().build(),
                                        object : InterstitialAdLoadCallback() {
                                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                                Timber.tag(TAG)
                                                    .i("Failed to load inter 1 ${p0.message}")
                                            }

                                            override fun onAdLoaded(p0: InterstitialAd) {
                                                admobInterstitialAd = p0
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
    }

    private fun reloadFacebookInterstitial() {
        if (viewModel.currentRemoteUser?.subscription == null) {
            lifecycleScope.launch {
                viewModel.appConfig.collectLatest { appConfig ->
                    appConfig?.advertisements?.forEachIndexed { _, ad ->
                        when {
                            ad.isEnabled && ad.type == "interstitial" && ad.name == "interstitial_1" && ad.provider == "facebook" -> {
                                fbInterstitialAd = FbInterstitialAd(this@HomeActivity, ad.adId)
                                fbInterstitialAd?.loadAd(
                                    fbInterstitialAd?.buildLoadAdConfig()
                                        ?.withAdListener(object : InterstitialAdListener {
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
                                                // TODO : Can load other here
                                            }
                                        })?.build()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showRewardedAdOrReviewIsRequired(isReview: Boolean, mediaLink: String) {
        val dialog = BaseAlertDialog(this)
        dialog.setTitle(R.string.download_reached_limit)
        dialog.addButton(
            R.string.subscribe_for_unlimited,
            isPrimary = true,
            isSubscribeButton = true,
            autoDismiss = false,
        ) { _, _ ->
            PlansActivity.open(plansStartForResult, this@HomeActivity)
        }

        if (isReview) {
            dialog.setMessage(R.string.download_reached_limit_message)
            dialog.addButton(
                R.string.give_five_stars_review,
                isPrimary = true,
                isDestructive = false
            ) { _, _ ->
                runOnUiThread {
                    val manager = ReviewManagerFactory.create(this)
                    val request = manager.requestReviewFlow()
                    request.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val reviewInfo = task.result
                            val flow = manager.launchReviewFlow(this, reviewInfo)
                            flow.addOnCompleteListener { task2 ->
                                if (task2.isSuccessful) {
                                    viewModel.onStartDownload(
                                        mediaLink,
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
                            PlansActivity.open(plansStartForResult, this@HomeActivity)
                        }
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
                        link = mediaLink,
                        isAfterReview = false,
                        isAfterRewardedAd = true
                    )
                }
            } else {
                showMessage(R.string.ad_not_available_now)
                PlansActivity.open(plansStartForResult, this@HomeActivity)
            }
        }
        dialog.setExtraCloseButtonVisible(false)
        dialog.show()
    }

    private fun showTempInvalidSubscription() {
        val dialog = BaseAlertDialog(this)
        dialog.setTitle(R.string.invalid_subscription)
        dialog.setMessage(R.string.invalid_subscription_message)
        dialog.setExtraCloseButtonVisible(true)
        dialog.show()
    }

    private fun showUpdateVersionDialog() {
        val dialog = BaseAlertDialog(this)
        dialog.setTitle(R.string.version_not_supported)
        dialog.setMessage(R.string.version_not_supported_message)
        dialog.addButton(
            R.string.update,
            isPrimary = true,
            isDestructive = false,
            autoDismiss = false
        ) { _, _ ->
            openAppInPlayStore(this.packageName, this)
        }
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun showDownloadLimitReached() {
        val dialog = BaseAlertDialog(this)

        dialog.addButton(
            R.string.subscribe_for_unlimited,
            isPrimary = true,
            isSubscribeButton = true,
            autoDismiss = true,
        ) { _, _ ->
            PlansActivity.open(plansStartForResult, this@HomeActivity)
        }

        dialog.addButton(
            R.string.login_to_instagram_for_unlimited,
            isPrimary = true,
            isLoginButton = true,
            autoDismiss = true,
        ) { _, _ ->
            showInstagramLoginWebView()
        }

        dialog.setTitle(R.string.download_reached_limit)
        dialog.setMessage(R.string.download_reached_limit_message_without_ads_and_review)
        dialog.setExtraCloseButtonVisible(true)
        dialog.show()
    }

    private fun showLoginRequiredForPrivatePosts() {
        val dialog = BaseAlertDialog(this)
        dialog.addButton(
            R.string.login_to_instagram_private_posts,
            isPrimary = true,
            isLoginButton = true,
            autoDismiss = true,
        ) { _, _ ->
            showInstagramLoginWebView()
        }
        dialog.setTitle(R.string.can_t_download_private_posts)
        dialog.setMessage(R.string.private_post_message)
        dialog.setExtraCloseButtonVisible(true)
        dialog.show()
    }

    /**
     *  This is for a custom dialog containing a custom progress
     *  bar, for now it's a loading dialog only.
     */
    private fun showFilesAreDownloadingDialog() {
        showLoading()
    }

    private fun hideFilesAreDownloadingDialog() {
        hideLoading()
        showMessage(R.string.media_downloaded_successfully)
    }

    private fun showDownloadFlowFinishedAd() {
        handler.postDelayed({
            runOnUiThread {
                if (admobInterstitialAd != null)
                    admobInterstitialAd?.show(this)
                else if (fbInterstitialAd != null && fbInterstitialAd?.isAdLoaded == true)
                    fbInterstitialAd?.show()
            }
        }, 20)
    }

    private fun showIndeterminateProgressbar() {
        runOnUiThread {
            binding.progress.scaleY = 4.0f
            binding.progress.isVisible = true
            binding.progress.isIndeterminate = true
        }
    }

    private fun showHighlightsToDownloadDialog() {
        val dialog = HighlightToDownloadBottomSheet.getInstance(Bundle())
        dialog.show(supportFragmentManager, HighlightToDownloadBottomSheet.TAG)
    }

    private fun showHomeBadge(itemsCount: Int = 0) {
        val navController = findNavController(R.id.nav_host_fragment)
        if (navController.currentDestination?.id != R.id.navigation_home) {
            var badge = binding.navView.getOrCreateBadge(R.id.navigation_home)
            badge.isVisible = true
        }
    }

    private fun clearHomeBadge() {
        handler.postDelayed({
            runOnUiThread {
                var badge = binding.navView.getOrCreateBadge(R.id.navigation_home)
                badge.isVisible = false
            }
        }, 1200)
    }

    // region [Search Menu]

    fun showDefaultMenu() {
        binding.toolbar.menu.clear()
        binding.mainLogo.isVisible = true
        binding.nSelected.isVisible = false
        binding.closeSelection.isVisible = false
        binding.toolbar.inflateMenu(R.menu.home_toolbar_menu)
    }

    fun showExploreMassDownloadMenu() {
        binding.toolbar.menu.clear()
        binding.mainLogo.isVisible = false
        binding.nSelected.isVisible = true
        binding.closeSelection.isVisible = true
        binding.toolbar.inflateMenu(R.menu.explore_mass_download_menu)

        binding.closeSelection.setOnClickListener {
            binding.toolbar.menu.performIdentifierAction(R.id.batch_download_close, 0)
            showDefaultMenu()
        }
    }

    fun showCollectionMassDownloadMenu() {
        binding.toolbar.menu.clear()
        binding.mainLogo.isVisible = false
        binding.nSelected.isVisible = true
        binding.closeSelection.isVisible = true
        binding.toolbar.inflateMenu(R.menu.collection_mass_download_menu)

        binding.closeSelection.setOnClickListener {
            binding.toolbar.menu.performIdentifierAction(R.id.batch_download_close, 0)
            showDefaultMenu()
        }
    }

    fun updateSelectedItemsCount(itemsCount: Int) {
        if (viewModel.currentRemoteUser == null) {
            Timber.tag(TAG).i("Remote user is null")
            return
        }
        var headerText = getString(R.string.n_selected_value, itemsCount.toString())
        if (viewModel.currentRemoteUser?.subscription == null) {
            headerText += getString(
                R.string.n_remaining,
                (viewModel.currentRemoteUser?.remainingMassDownloadCount!! - itemsCount).toString()
            )
        }
        binding.nSelected.text = headerText
    }

    fun showReachedMaxMassDownloadCount() {
        val dialog = BaseAlertDialog(this)
        dialog.setTitle(R.string.mass_download_limit_title)
        dialog.setMessage(R.string.mass_download_limit)
        dialog.addButton(
            R.string.subscribe_for_unlimited_mass_download,
            isPrimary = true,
            isSubscribeButton = true,
            autoDismiss = true,
        ) { _, _ ->
            PlansActivity.open(plansStartForResult, this@HomeActivity)
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
                PlansActivity.open(plansStartForResult, this@HomeActivity)
            }
        }
        dialog.setExtraCloseButtonVisible(false)
        dialog.show()
    }


    // endregion
}