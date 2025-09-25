package com.instagram.video.downloader.ui.howTo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.instagram.video.downloader.R
import com.instagram.video.downloader.common.Constants
import com.instagram.video.downloader.databinding.ActivityHowToDownloadBinding
import com.instagram.video.downloader.ui.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HowToDownloadActivity : BaseActivity<ActivityHowToDownloadBinding>() {

    companion object {
        const val TAG = "HowToDownloadActivity"
        fun getIntent(context: Context) = Intent(context, HowToDownloadActivity::class.java)
    }

    private lateinit var adapter: HowToPagerAdapter

    override fun getLayoutId() = R.layout.activity_how_to_download


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setDisplayShowTitleEnabled(false)
        }
        initiateViewPager()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun initiateViewPager() {
        adapter = HowToPagerAdapter(Constants.howToSteps)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
        binding.viewPager.adapter = adapter
    }
}