package com.instagram.video.downloader.ui.home

//import com.applovin.mediation.MaxAd
//import com.applovin.mediation.MaxError
//import com.applovin.mediation.nativeAds.MaxNativeAdListener
//import com.applovin.mediation.nativeAds.MaxNativeAdLoader
//import com.applovin.mediation.nativeAds.MaxNativeAdView
import android.annotation.SuppressLint
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.isEmpty
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.NativeAd
import com.facebook.ads.NativeAdBase
import com.facebook.ads.NativeAdListener
import com.facebook.ads.NativeAdView
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.instagram.video.downloader.R
import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.common.Constants.PLAYER_NATIVE_AD_INTERVAL
import com.instagram.video.downloader.common.Constants.SIZE_TO_INCLUDE_NATIVE_PLAYER_AFTER
import com.instagram.video.downloader.common.DataReference
import com.instagram.video.downloader.common.TempPlayerDataModel
import com.instagram.video.downloader.common.buildInstagramLink
import com.instagram.video.downloader.common.insertItemEveryNItems
import com.instagram.video.downloader.common.openInstagram
import com.instagram.video.downloader.common.shareFile
import com.instagram.video.downloader.common.shareFiles
import com.instagram.video.downloader.common.toPlayerDataModel
import com.instagram.video.downloader.common.ui.compose.InstaDownloaderTheme
import com.instagram.video.downloader.common.ui.compose.PostContextActions
import com.instagram.video.downloader.data.local.room.entities.PostWithMediaUserAndMedia
import com.instagram.video.downloader.data.remote.api.dto.hasValidSubscription
import com.instagram.video.downloader.databinding.FragmentHomeBinding
import com.instagram.video.downloader.ui.base.BaseAlertDialog
import com.instagram.video.downloader.ui.base.BaseFragment
import com.instagram.video.downloader.ui.home.downloads.DownloadedPostsAdapter
import com.instagram.video.downloader.ui.home.downloads.PostItemListener
import com.instagram.video.downloader.ui.player.PlayerActivity
import com.instagram.video.downloader.ui.player.PlayerDataModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.UUID
import com.instagram.video.downloader.common.isNetworkConnected


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(), PostItemListener {


    companion object {
        const val TAG = "HomeFragment"
    }


    private val viewModel: HomeViewModel by activityViewModels()

    private val adapter = DownloadedPostsAdapter()


    override fun getLayoutId() = R.layout.fragment_home

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isNetworkConnected(requireActivity()) && binding.nativeContainer.isEmpty()) {
            binding.nativeContainer.visibility = View.INVISIBLE
        } else if (binding.nativeContainer.isEmpty() && !isNetworkConnected(requireActivity())) {
            binding.nativeContainer.isVisible = false
        }


        binding.howToDownload.setOnClickListener { viewModel.onHomeAction(HomeAction.OnOpenHowToDownload) }
        binding.openInstagram.setOnClickListener { viewModel.onHomeAction(HomeAction.OnOpenInstagram) }
        binding.floatingActionButton.setOnClickListener { viewModel.onHomeAction(HomeAction.OnShowDownloadInputDialog) }

        initWaitingTextAnimation()

        binding.postAction.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.Default)
            setContent {
                InstaDownloaderTheme {
                    PostContextActions(
                        viewModel.selectedPostForOptions,
                        onShare = {
                            viewModel.selectedPostForOptions.value?.let { post ->
                                if (post.media.size > 1) {
                                    val files = ArrayList<File>()
                                    post.media.map { it.fileLocation }.forEach { filePath ->
                                        val file = File(filePath)
                                        if (file.exists()) {
                                            files.add(file)
                                        }
                                    }
                                    shareFiles(files, baseActivity()!!)
                                } else if (post.media.size == 1) {
                                    val file = File(post.media.first().fileLocation)
                                    if (file.exists()) {
                                        shareFile(file, baseActivity()!!)
                                    } else {
                                        showMessage(R.string.media_file_does_not_exist)
                                    }
                                }
                            }
                        },
                        onShowFileLocation = {
                            showPostFilesLocationDialog()
                        },
                        onRename = {
                            renameSinglePostMedia()
                        },
                        onOpenInInstagram = {
                            openPostInInstagram()
                        },
                        onDelete = {
                            deletePost(viewModel.selectedPostForOptions.value!!)
                        },
                    )
                }
            }
        }

        lifecycleScope.launch {
            viewModel.homeChannel.collect { event ->
                if (event == HomeAction.OnLoadNativeAds) {
                    initializeNativeAds()
                }
                if (event == HomeAction.HideAds) {
                    binding.nativeContainer.isVisible = false
                }
            }
        }

        lifecycleScope.launch {
            viewModel.postsDataFlow.collect { posts ->
                if (posts.isNotEmpty()) {
                    binding.posts.isVisible = true
                    binding.emptyDownloadsSection.gravity = Gravity.TOP
                    adapter.posts = posts
                    adapter.postItemListener = this@HomeFragment
                    binding.posts.adapter = adapter
                    adapter.notifyDataSetChanged()
                } else {
                    binding.posts.isVisible = false
                    binding.emptyDownloadsSection.gravity = Gravity.CENTER
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.postAction.disposeComposition()
    }

    private fun initializeNativeAds() {
        lifecycleScope.launch {
            viewModel.appConfig.collectLatest { appConfig ->
                if (appConfig == null || !appConfig.isAdsEnabled)
                    return@collectLatest
                appConfig.advertisements.filter { it.name == "native_main" && it.type == "native" }
                    .forEach { ad ->
                        baseActivity()?.runOnUiThread {
                            when {
                                ad.provider == "admob" && ad.isEnabled -> {
                                    Timber.tag(TAG).i("Ad load started...")
                                    AdLoader
                                        .Builder(baseActivity()!!, ad.adId)
                                        .forNativeAd { nativeAd ->
                                            binding.nativeContainer.visibility = View.VISIBLE
                                            val styles = NativeTemplateStyle
                                                .Builder()
                                                .build()
                                            baseActivity()?.let { activity ->
                                                val layoutInflater = activity.getSystemService(
                                                    LAYOUT_INFLATER_SERVICE
                                                ) as LayoutInflater
                                                val view = layoutInflater.inflate(
                                                    com.google.android.ads.nativetemplates.R.layout.medium_template_view,
                                                    binding.nativeContainer
                                                )
                                                val template =
                                                    view.findViewById<TemplateView>(com.google.android.ads.nativetemplates.R.id.my_template)
                                                template.setStyles(styles)
                                                template.setNativeAd(nativeAd)
                                            }
                                        }
                                        .withAdListener(object : AdListener() {
                                            override fun onAdFailedToLoad(p0: LoadAdError) {
                                                binding.nativeContainer.isVisible = false
                                                Timber.tag(TAG)
                                                    .e("Failed to load native: ${p0.message}")
                                            }
                                        })
                                        .build()
                                        .loadAd(AdRequest.Builder().build())
                                }

                                ad.provider == "facebook" && ad.isEnabled -> {
                                    Timber.tag(TAG).i("Ad load started...")
                                    val fbNativeAd = NativeAd(baseActivity()!!, ad.adId)
                                    fbNativeAd.loadAd(
                                        fbNativeAd.buildLoadAdConfig()
                                            .withAdListener(object : NativeAdListener {
                                                override fun onError(p0: Ad?, p1: AdError?) {
                                                    Timber.tag(TAG).e(p1?.errorMessage)
                                                }

                                                override fun onAdLoaded(p0: Ad?) {
                                                    baseActivity()?.runOnUiThread {
                                                        val adView = NativeAdView.render(
                                                            baseActivity()!!,
                                                            fbNativeAd
                                                        )
                                                        binding.nativeContainer.invalidate()
                                                        binding.nativeContainer.addView(adView)
                                                    }
                                                }

                                                override fun onAdClicked(p0: Ad?) {
                                                    // NO-OP
                                                }

                                                override fun onLoggingImpression(p0: Ad?) {
                                                    // NO-OP
                                                }

                                                override fun onMediaDownloaded(p0: Ad?) {
                                                    // NO-OP
                                                }
                                            })
                                            .withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL)
                                            .build()
                                    )
                                }

                                ad.provider == "applovin" && ad.isEnabled -> {
//                                    val applovinNative =
//                                        MaxNativeAdLoader(ad.adId, baseActivity()!!)
//                                    applovinNative.setNativeAdListener(object :
//                                        MaxNativeAdListener() {
//                                        override fun onNativeAdLoaded(
//                                            p0: MaxNativeAdView?,
//                                            p1: MaxAd
//                                        ) {
//                                            binding.nativeContainer.removeAllViews()
//                                            binding.nativeContainer.addView(p0)
//                                        }
//
//                                        override fun onNativeAdLoadFailed(
//                                            p0: String,
//                                            p1: MaxError
//                                        ) {
//                                            Timber.tag(TAG)
//                                                .i("Failed to load native applovin ${p1.message}")
//                                        }
//                                    })
                                }
                            }
                        }
                    }
            }
        }
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

    private fun showPostFilesLocationDialog() {
        val dialog = BaseAlertDialog(baseActivity()!!)
        dialog.setTitle(R.string.post_files_location)
        var dialogContent = "All Posts Media locations:\n"
        viewModel.selectedPostForOptions.value?.let {
            if (it.media.size == 1) {
                dialogContent += it.media.first().fileLocation
            } else {
                it.media.forEach { m ->
                    dialogContent += if (it.media.last() == m) {
                        m.fileLocation
                    } else
                        m.fileLocation + "\n"
                }
            }
        }
        dialog.setMessage(dialogContent)
        dialog.setExtraCloseButtonVisible(true)
        dialog.show()
    }

    /**
     *  Called only for the posts having a single file media
     */
    private fun renameSinglePostMedia() {

    }

    private fun openPostInInstagram() {
        openInstagram(
            buildInstagramLink(viewModel.selectedPostForOptions.value!!),
            baseActivity()!!
        )
    }

    private fun deletePost(post: PostWithMediaUserAndMedia) {
        val deleteDialog = BaseAlertDialog(baseActivity()!!)
        deleteDialog.setTitle(R.string.delete_post_title)
        deleteDialog.setMessage(getString(R.string.delete_post_message))
        deleteDialog.addButton(R.string.yes, isPrimary = true, isDestructive = true) { _, _ ->
            viewModel.deletePost(post)
        }
        deleteDialog.setExtraCloseButtonVisible(true)
        deleteDialog.show()
    }

    // region [PostItemListener]

    override fun onPostItemClick(post: PostWithMediaUserAndMedia) {
        var posts = viewModel.postsDataFlow.value.map { it.toPlayerDataModel() }

        if (
            posts.size > SIZE_TO_INCLUDE_NATIVE_PLAYER_AFTER
            && viewModel.currentRemoteUser != null
            && viewModel.currentRemoteUser?.hasValidSubscription() == false
        ) {
            viewModel.appConfig.value?.advertisements?.forEach {
                when {
                    it.isEnabled && it.name == "player_native_1" && it.type == "native" && it.provider == "admob" -> {
                        posts = insertItemEveryNItems(
                            posts.toMutableList(),
                            PlayerDataModel.AdMediaItem(it.provider, it.adId),
                            PLAYER_NATIVE_AD_INTERVAL
                        )
                    }
                }
            }
        }
        val scrollTo = posts.indexOf(post.toPlayerDataModel())

        val tempData = TempPlayerDataModel(ArrayList(posts))
        val sessionId = UUID.randomUUID().toString()
        DataReference<TempPlayerDataModel>(sessionId = sessionId).save(
            VideoDownloaderApp.application,
            tempData
        )

        startActivity(PlayerActivity.getIntent(baseActivity()!!, sessionId, scrollTo))
    }

    override fun onPostMoreClick(post: PostWithMediaUserAndMedia) {
        viewModel.selectedPostForOptions.value = post
    }

    // endregion
}