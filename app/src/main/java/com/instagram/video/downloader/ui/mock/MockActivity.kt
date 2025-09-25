package com.instagram.video.downloader.ui.mock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.core.view.isVisible
import com.instagram.video.downloader.R
import com.instagram.video.downloader.common.openInstagram
import com.instagram.video.downloader.databinding.ActivityMockBinding
import com.instagram.video.downloader.ui.base.BaseActivity
import com.instagram.video.downloader.ui.howTo.HowToDownloadActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MockActivity : BaseActivity<ActivityMockBinding>() {

    companion object {
        fun getIntent(context: Context) = Intent(context, MockActivity::class.java)
    }

    override fun getLayoutId() = R.layout.activity_mock

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.progress.isVisible = true
        handler.postDelayed({
            binding.progress.isVisible = false
        }, 2000L)

        binding.howToDownload.setOnClickListener {
            startActivity(
                HowToDownloadActivity.getIntent(
                    this@MockActivity
                )
            )
        }
        binding.openInstagram.setOnClickListener {
            openInstagram(
                "https://www.instagram.com",
                this@MockActivity
            )
        }
        initWaitingTextAnimation()
    }


    private fun initWaitingTextAnimation() {
        CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                AlphaAnimation(0.0f, 1.0f).let {
                    it.duration = 2500
                    it.startOffset = 20
                    it.repeatMode = Animation.REVERSE
                    it.repeatCount = Animation.INFINITE
                    binding.waitingForLink.startAnimation(it)
                }
                delay(6000)
                binding.waitingForLink.text =
                    resources.getStringArray(R.array.hints).random()
            }
        }
    }
}