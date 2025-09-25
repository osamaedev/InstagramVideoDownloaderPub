package com.instagram.video.downloader.ui.faq

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import com.instagram.video.downloader.BR
import com.instagram.video.downloader.R
import com.instagram.video.downloader.databinding.ActivityFaqBinding
import com.instagram.video.downloader.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FaqActivity : BaseActivity<ActivityFaqBinding>() {

    companion object {
        const val TAG = "FaqActivity"
        fun getIntent(context: Context) = Intent(context, FaqActivity::class.java)
    }

    override fun getLayoutId() = R.layout.activity_faq


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayShowTitleEnabled(true)
            setTitle(R.string.faq)
        }

        binding.contactSupport.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}