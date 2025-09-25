package com.instagram.video.downloader.ui.home.settings

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import com.instagram.video.downloader.BuildConfig
import com.instagram.video.downloader.R
import com.instagram.video.downloader.common.openWeblink
import com.instagram.video.downloader.common.shareApp
import com.instagram.video.downloader.databinding.FragmentSettingsBinding
import com.instagram.video.downloader.ui.base.BaseFragment
import com.instagram.video.downloader.ui.faq.FaqActivity
import com.instagram.video.downloader.ui.howTo.HowToDownloadActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun getLayoutId() = R.layout.fragment_settings


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.shareWith.text =
            resources.getString(R.string.settings_share, resources.getString(R.string.app_name))

        binding.nightMode.setOnClickListener {
            binding.darkModeSwitch.isChecked = binding.darkModeSwitch.isChecked.not()
        }

        "Build Version: ${BuildConfig.VERSION_NAME}".let {
            binding.buildVersion.text = it
        }


        binding.darkModeSwitch.setOnCheckedChangeListener { button, isChecked ->
            when (isChecked) {
                true -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }

                false -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }

        binding.howToDownload.setOnClickListener {
            baseActivity()?.startActivity(
                HowToDownloadActivity.getIntent(baseActivity()!!)
            )
        }
        binding.faq.setOnClickListener { startActivity(FaqActivity.getIntent(baseActivity()!!)) }
        binding.share.setOnClickListener { shareApp(baseActivity()!!) }
        binding.privacy.setOnClickListener {
            openWeblink(
                resources.getString(R.string.privacy_link),
                baseActivity()!!
            )
        }

    }

    override fun onResume() {
        super.onResume()
        val nighModeFlag = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        binding.darkModeSwitch.isChecked =
            (nighModeFlag == Configuration.UI_MODE_NIGHT_YES || nighModeFlag == Configuration.UI_MODE_NIGHT_UNDEFINED)
        viewModel.setIsDarkModeEnabled((nighModeFlag == Configuration.UI_MODE_NIGHT_YES || nighModeFlag == Configuration.UI_MODE_NIGHT_UNDEFINED))
    }
}