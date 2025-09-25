package com.instagram.video.downloader.ui.splash

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.easyandroidanimations.library.Animation
import com.easyandroidanimations.library.FadeInAnimation
import com.easyandroidanimations.library.SlideInAnimation
import com.instagram.video.downloader.R
import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.common.ui.compose.InstaDownloaderTheme
import com.instagram.video.downloader.common.ui.views.InstaDownloaderCircularProgressView
import com.instagram.video.downloader.data.remote.api.dto.AppConfig
import com.instagram.video.downloader.data.remote.api.dto.hasValidSubscription
import com.instagram.video.downloader.data.remote.api.dto.toSubscription
import com.instagram.video.downloader.databinding.ActivitySplashBinding
import com.instagram.video.downloader.ui.base.BaseActivity
import com.instagram.video.downloader.ui.base.showMessage
import com.instagram.video.downloader.ui.home.HomeActivity
import com.instagram.video.downloader.ui.mock.MockActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>(),
    VideoDownloaderApp.OnShowOpenAdCompleteListener {

    companion object {
        fun getIntent(context: Context) = Intent(context, SplashActivity::class.java)
    }


    private val viewModel: SplashViewModel by viewModels()

    override fun getLayoutId() = R.layout.activity_splash


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler.postDelayed({
            FadeInAnimation(binding.brandImage).setDuration(1600L).animate()
            SlideInAnimation(binding.logo).setDuration(800L)
                .setDirection(Animation.DIRECTION_DOWN).animate()
        }, 500)
        binding.progressView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.Default)
            setContent {
                InstaDownloaderTheme {
                    InstaDownloaderCircularProgressView()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.state
                .distinctUntilChanged(areEquivalent = { old, new ->
                    return@distinctUntilChanged old.isLoading == new.isLoading &&
                            old.isNetworkAvailable == new.isNetworkAvailable &&
                            old.error == new.error &&
                            old.appConfig == new.appConfig &&
                            old.isUserAndAppCheckDone == new.isUserAndAppCheckDone
                })
                .collect { splashState ->
                    binding.progressView.isVisible = splashState.isLoading

                    if (splashState.isUserAndAppCheckDone
                        && splashState.isNetworkAvailable.not()
                        && splashState.isLoading.not()
                        && splashState.appConfig == null
                    ) {
                        // just start the main activity
                        handler.postDelayed({
                            startActivity(HomeActivity.getIntent(this@SplashActivity))
                            this@SplashActivity.finish()
                        }, 3000L)
                    }

                    if (splashState.isUserAndAppCheckDone && splashState.isLoading.not() && splashState.appConfig != null) {

                        if (splashState.appConfig?.isTesting == true) {
                            startActivity(MockActivity.getIntent(this@SplashActivity))
                            this@SplashActivity.finish()
                            return@collect
                        }

                        if (splashState.appConfig?.isAdsEnabled == true
                            && splashState.user != null
                            && splashState.user?.hasValidSubscription() == false        // for users that does not has a valid subscription
                        ) {
                            initOpenAppAd(splashState.appConfig!!)
                        } else if (splashState.user?.hasValidSubscription() == true || splashState.appConfig?.isAdsEnabled == false) {
                            onShowAdComplete()
                        }

                        handler.postDelayed({
                            runOnUiThread {
                                VideoDownloaderApp.application.openAdaManager?.showAdIfAvailable(
                                    this@SplashActivity,
                                    this@SplashActivity
                                )
                            }
                        }, 2000L)
                    }

                    if (splashState.error != null) {
                        showMessage(R.string.something_went_wrong)
                    }
                }
        }
    }


    private fun initOpenAppAd(appConfig: AppConfig) {
        appConfig.advertisements.first { it.name == "open_ad_1" && it.provider == "admob" && it.type == "open_ad" }
            .let { ad ->
                if (ad.isEnabled) {
                    VideoDownloaderApp.application.initOpenAd(ad.adId)
                }
            }
    }

    override fun onResume() {
        super.onResume()
        binding.progressView.disposeComposition()
    }

    override fun onShowAdComplete() {
        startActivity(HomeActivity.getIntent(this@SplashActivity))
        handler.removeCallbacksAndMessages(null)
        this@SplashActivity.finish()
    }
}