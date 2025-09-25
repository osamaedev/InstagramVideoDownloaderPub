package com.instagram.video.downloader.ui.home.explore

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowMetrics
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingData
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.instagram.video.downloader.R
import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.data.remote.api.dto.Advertisement
import com.instagram.video.downloader.data.remote.api.dto.hasValidSubscription
import com.instagram.video.downloader.databinding.FragmentExploreContainerBinding
import com.instagram.video.downloader.ui.base.BaseFragment
import com.instagram.video.downloader.ui.home.HomeActivity
import com.instagram.video.downloader.ui.home.explore.collection.CollectionFragment
import com.instagram.video.downloader.ui.home.explore.explore.ExploreFragment
import com.instagram.video.downloader.ui.home.explore.like.LikeFragment
import com.instagram.video.downloader.ui.home.HomeViewModel
import com.instagram.video.downloader.ui.home.explore.collection.adapter.CollectionUiModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import timber.log.Timber


@AndroidEntryPoint
class ExploreContainerFragment : BaseFragment<FragmentExploreContainerBinding>() {

    companion object {
        const val TAG = "ExploreContainer"
    }

    private val viewModel: HomeViewModel by activityViewModels()

    private var statePagerAdapter: FragmentStateAdapter? = null

    private var exploreFragment: ExploreFragment = ExploreFragment()
    private var collectionFragment: CollectionFragment = CollectionFragment()
    private var likeFragment: LikeFragment = LikeFragment()

    override fun getLayoutId() = R.layout.fragment_explore_container


    private val pageChangeCallback = object : OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.resetSelection.tryEmit(true)
            }
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.exploreTabLayout.tabMode = TabLayout.MODE_FIXED
        statePagerAdapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {
            override fun getItemCount(): Int {
                return 2
            }

            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> {
                        exploreFragment
                    }

//                    1 -> {
//                        likeFragment
//                    }

                    1 -> {
                        collectionFragment
                    }

                    else -> Fragment()
                }
            }
        }
        binding.viewPager.adapter = statePagerAdapter
        binding.exploreTabLayout.let {
            binding.viewPager.let { page ->
                TabLayoutMediator(it, page) { tab, position ->
                    tab.text = when (position) {
                        0 -> getString(R.string.tab_explore_explore)
//                        1 -> getString(R.string.tab_explore_like)
                        else -> getString(R.string.tab_explore_collection)
                    }
                }.attach()
            }
        }
        statePagerAdapter?.notifyDataSetChanged()
        initBanner()
    }


    private fun initBanner() {
        if (
            viewModel.currentRemoteUser != null
            && viewModel.currentRemoteUser?.hasValidSubscription() == false
        ) {
            viewModel.appConfig.value?.advertisements?.firstOrNull {
                it.name == "explore_banner" && it.type == "banner"
            }?.let { ad ->
                if (ad.provider == "admob") {
                    lifecycleScope.launch {
                        merge(viewModel.exploreDataFlow, viewModel.collectionDataFlow)
                            .catch {
                                Timber.tag(TAG).e(it)
                            }
                            .collect {
                                if ((baseActivity() as HomeActivity).bannerAdView == null
                                    && viewModel.currentLoggedInUser.value.id != -1L
                                ) {
                                    loadAdmobAd(ad)
                                }
                            }
                    }
                }
            }
        }
    }

    private fun loadAdmobAd(ad: Advertisement) {
        val adView = AdView(VideoDownloaderApp.application)
        adView.adUnitId = ad.adId
        adView.setAdSize(adSize)
        (baseActivity() as HomeActivity).bannerAdView = adView
        binding.bannerContainer.removeAllViews()
        binding.bannerContainer.addView(adView)
        adView.loadAd(AdRequest.Builder().build())
    }

    private val adSize: AdSize
        get() {
            val displayMetrics = resources.displayMetrics
            val adWidthPixels =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val windowMetrics: WindowMetrics =
                        activity?.windowManager?.currentWindowMetrics!!
                    windowMetrics.bounds.width()
                } else {
                    displayMetrics.widthPixels
                }
            val density = displayMetrics.density
            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                requireActivity(),
                adWidth
            )
        }


    override fun onResume() {
        if ((baseActivity() as HomeActivity).bannerAdView != null && binding.bannerContainer.isEmpty()) {
            binding.bannerContainer.addView((baseActivity() as HomeActivity).bannerAdView)
        }
        binding.viewPager.registerOnPageChangeCallback(pageChangeCallback)
        super.onResume()
    }

    override fun onPause() {
        binding.bannerContainer.removeAllViews()
        binding.viewPager.unregisterOnPageChangeCallback(pageChangeCallback)
        super.onPause()
    }
}