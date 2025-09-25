package com.instagram.video.downloader.ui.home.explore.like

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.fragment.app.activityViewModels
import com.instagram.video.downloader.R
import com.instagram.video.downloader.databinding.FragmentLikeBinding
import com.instagram.video.downloader.ui.base.BaseFragment
import com.instagram.video.downloader.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LikeFragment : BaseFragment<FragmentLikeBinding>() {


    private val viewModel: HomeViewModel by activityViewModels()

    override fun getLayoutId() = R.layout.fragment_like

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cookiePolicy.movementMethod = LinkMovementMethod.getInstance()

        binding.refreshLayout.setOnRefreshListener { }

        binding.instagramLogin.setOnClickListener { }

    }
}