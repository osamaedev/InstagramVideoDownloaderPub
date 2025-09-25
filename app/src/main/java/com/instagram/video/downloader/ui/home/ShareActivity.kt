package com.instagram.video.downloader.ui.home

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import com.instagram.video.downloader.R
import com.instagram.video.downloader.common.coroutines.launchOnState
import com.instagram.video.downloader.common.isAudioLink
import com.instagram.video.downloader.common.isHighlightShareLink
import com.instagram.video.downloader.common.isValidInstagramLink
import com.instagram.video.downloader.data.local.prefs.PrefsHelper
import com.instagram.video.downloader.ui.splash.SplashActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ShareActivity : AppCompatActivity() {

    @Inject
    lateinit var prefsHelper: PrefsHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val intentAction = intent.action
        if (intentAction == Intent.ACTION_SEND) {
            val receivedUri =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) intent.getParcelableExtra(
                    Intent.EXTRA_TEXT,
                    String::class.java
                ) else intent.getStringExtra(Intent.EXTRA_TEXT)
            if (receivedUri != null) {
                prefsHelper.setIntentLinkToDownload(receivedUri)
                startActivity(HomeActivity.getIntent(this, receivedUri))
                finish()
            } else {
                startActivity(SplashActivity.getIntent(this))
                finish()
            }
        }
    }
}