package com.instagram.video.downloader

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.UiModeManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.facebook.ads.AdSettings
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.instagram.video.downloader.common.ui.utils.MarkdownParser
import com.instagram.video.downloader.data.local.prefs.PrefsHelper
import com.instagram.video.downloader.ui.home.HomeActivity
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import javax.inject.Inject


@HiltAndroidApp
class VideoDownloaderApp : Application(), Configuration.Provider,
    Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {

    private external fun get1(): String
    private external fun get2(): String
    private external fun get3(): String
    private external fun get4(): String


    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var prefsHelper: PrefsHelper

    var openAdaManager: AppOpenAdManager? = null
    private var currentActivity: Activity? = null


    companion object {

        const val TAG = "VideoDownloaderApp"

        init {
            System.loadLibrary("Main")
        }

        @SuppressLint("StaticFieldLeak")
        lateinit var application: VideoDownloaderApp

        const val grantType = "client_credentials"

        lateinit var clientSecret: String
            private set
        lateinit var clientId: String
            private set
        lateinit var baseUrl: String
            private set

        lateinit var key: String
            private set

        lateinit var instaBaseUrl: String
    }

    init {
        application = this
        clientId = get1()
        baseUrl = get3()
        clientSecret = get2()
        key = get4()

        instaBaseUrl = "https://www.instagram.com/"
    }

    override fun onCreate() {
        super<Application>.onCreate()
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        CoroutineScope(Dispatchers.IO).launch {
            MobileAds.initialize(this@VideoDownloaderApp)
        }

        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e)
        }
        MarkdownParser.setup(this)
        if (BuildConfig.DEBUG) {

            val testDeviceIds = listOf("4EC2E836DA262ADE33AD7E9DBBC655FC")
            val configuration =
                RequestConfiguration
                    .Builder()
                    .setTestDeviceIds(testDeviceIds)
                    .build()
            MobileAds.setRequestConfiguration(configuration)


            AdSettings.setTestMode(true)
            AdSettings.addTestDevice("1a213b00-9352-4e1e-84e1-8a6b1583f56a")

            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
            Timber.plant(Timber.DebugTree())
        }

        if (!prefsHelper.getIsIntroductionDone()) {          // means this is the first time the app open
            val uiManager = getSystemService(UI_MODE_SERVICE) as UiModeManager
            val mode = uiManager.nightMode
            when (mode) {
                UiModeManager.MODE_NIGHT_YES -> {
                    prefsHelper.setIsDarkModeEnabled(true)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }

                UiModeManager.MODE_NIGHT_NO -> {
                    prefsHelper.setIsDarkModeEnabled(false)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }

                else -> prefsHelper.setIsDarkModeEnabled(false)
            }
        }
    }


    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        if (openAdaManager != null && currentActivity != null) {
            // openAdManager is null means the ad is disabled or ad unit is disabled
            openAdaManager?.showAdIfAvailable(
                currentActivity!!,
                object : OnShowOpenAdCompleteListener {
                    override fun onShowAdComplete() {
                        Timber.tag(TAG).d("Open Ad closed from the HomeActivity")
                    }
                }
            )
        }
    }


    // region [ActivityLifeCycleCallback]

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // NO-OP
    }

    override fun onActivityStarted(activity: Activity) {

    }

    override fun onActivityResumed(activity: Activity) {
        if (openAdaManager != null && openAdaManager?.isShowingAd == false && activity is HomeActivity) {
            currentActivity = activity
        }
    }

    override fun onActivityPaused(activity: Activity) {
        // NO-OP
    }

    override fun onActivityStopped(activity: Activity) {
        // NO-OP
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // NO-OP
    }

    override fun onActivityDestroyed(activity: Activity) {
        // NO-OP
    }

    // endregion


    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()
    }


    fun initOpenAd(adUnitId: String) {
        if (openAdaManager == null) {
            openAdaManager = AppOpenAdManager(adUnitId)
            openAdaManager?.loadAd(application)
        }
    }

    interface OnShowOpenAdCompleteListener {
        fun onShowAdComplete()
    }

    inner class AppOpenAdManager(private var adUnitId: String) {

        private var appOpenAd: AppOpenAd? = null
        private var isLoadingAd = false
        var isShowingAd = false
        var loadTime: Long = 0


        private fun isAdAvailable(): Boolean {
            return appOpenAd != null && wasLoadTimeLessThanNHoursAgo()
        }

        private fun wasLoadTimeLessThanNHoursAgo(numHours: Long = 4): Boolean {
            val dateDifference: Long = Date().time - loadTime
            val numMilliSecondsPerHour: Long = 3600000
            return dateDifference < numMilliSecondsPerHour * numHours
        }

        fun loadAd(context: Context) {
            // Do not load ad if there is an unused ad or one is already loading.
            if (isLoadingAd || isAdAvailable()) {
                return
            }

            isLoadingAd = true
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                context,
                this.adUnitId,
                request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {

                    override fun onAdLoaded(ad: AppOpenAd) {
                        // Called when an app open ad has loaded.
                        Timber.tag(TAG).i("Ad was loaded.")
                        appOpenAd = ad
                        isLoadingAd = false
                        loadTime = Date().time
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Called when an app open ad has failed to load.
                        Timber.tag(TAG).e(loadAdError.message)
                        isLoadingAd = false;
                    }
                })
        }

        fun showAdIfAvailable(
            activity: Activity,
            onShowOpenAdCompleteListener: OnShowOpenAdCompleteListener
        ) {

            if (isShowingAd) {
                Timber.tag(TAG).i("The app open ad is already showing.")
                return
            }

            if (!isAdAvailable()) {
                Timber.tag(TAG).i("The app open ad is not ready yet.")
                onShowOpenAdCompleteListener.onShowAdComplete()
                loadAd(activity)
                return
            }

            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                override fun onAdDismissedFullScreenContent() {
                    // Called when full screen content is dismissed.
                    // Set the reference to null so isAdAvailable() returns false.
                    Timber.tag(TAG).i("Ad dismissed fullscreen content.")
                    appOpenAd = null
                    isShowingAd = false

                    onShowOpenAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    // Called when fullscreen content failed to show.
                    // Set the reference to null so isAdAvailable() returns false.
                    Timber.tag(TAG).e(adError.message)
                    appOpenAd = null
                    isShowingAd = false

                    onShowOpenAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    // Called when fullscreen content is shown.
                    Timber.tag(TAG).e("Ad showed fullscreen content.")
                }
            }

            isShowingAd = true
            appOpenAd?.show(activity)
        }
    }

}