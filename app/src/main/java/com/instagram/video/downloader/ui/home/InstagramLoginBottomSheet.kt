package com.instagram.video.downloader.ui.home

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings.LOAD_NO_CACHE
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.instagram.video.downloader.R
import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.databinding.DialogInstagramLoginBinding
import com.instagram.video.downloader.ui.base.BaseBottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class InstagramLoginBottomSheet : BaseBottomSheetDialog<DialogInstagramLoginBinding>() {

    companion object {
        const val TAG = "InstagramLoginBottomShe"

        fun getInstance(bundle: Bundle) = InstagramLoginBottomSheet().apply {
            arguments = bundle
        }
    }

    override fun getLayoutId() = R.layout.dialog_instagram_login

    private val viewModel: HomeViewModel by activityViewModels()

    @Suppress("DEPRECATION")
    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.icClose.setOnClickListener {
            dismiss()
        }

        binding.faqButton.setOnClickListener {
            viewModel.onHomeAction(HomeAction.OnShowFaq)
        }
        binding.loginWebView.settings.javaScriptEnabled = true
        binding.loginWebView.settings.userAgentString =
            resources.getString(R.string.web_agent)
        binding.loginWebView.settings.allowUniversalAccessFromFileURLs = true
        binding.loginWebView.settings.domStorageEnabled = true
        binding.loginWebView.settings.loadWithOverviewMode = true
        binding.loginWebView.settings.useWideViewPort = true
        binding.loginWebView.settings.allowFileAccess = true
        binding.loginWebView.settings.allowFileAccess = true
        binding.loginWebView.settings.allowFileAccessFromFileURLs = true
        binding.loginWebView.settings.loadsImagesAutomatically = true


        handler.post {
            if (viewModel.appConfig.value != null) {
                binding.loginWebView.loadUrl(viewModel.appConfig.value?.loginPageUrl!!)
            } else
                binding.loginWebView.loadUrl(VideoDownloaderApp.instaBaseUrl)

            binding.loginWebView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    Timber.tag(TAG).i("Loading instagram page progress $newProgress")
                }
            }

            binding.loginWebView.webViewClient = object : WebViewClient() {

                @Deprecated("Deprecated in Java")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    val u = url
                    if (url == "https://www.instagram.com/"
                        || url == "https://www.instagram.com/accounts/onetap/?next=%2F"
                        || url == "https://www.instagram.com/?deoia=1"
                        || url?.startsWith("https://www.instagram.com/") == true
                    ) {
                        val cookieManager = CookieManager.getInstance()
                        Timber.tag(TAG).i(
                            "All Cookies: %s",
                            cookieManager.getCookie("https://www.instagram.com/")
                        )
                        dismiss()
                        viewModel.handleUserInstagramLogin(
                            CookieManager.getInstance().getCookie("https://www.instagram.com/"),
                            arguments?.getBoolean("is_from_profiles") == true
                        )
                    }
                    return false
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                }
            }
        }
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener { dialogInterface ->
            val d = dialogInterface as BottomSheetDialog
            val bottomSheet =
                d.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.setBackgroundResource(android.R.color.transparent)
            val behavior = BottomSheetBehavior.from(bottomSheet!!)
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
            bottomSheet.layoutParams = layoutParams
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }

        dialog.window?.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        val windowInsetsController =
            WindowCompat.getInsetsController(dialog.window!!, dialog.window!!.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())

        return dialog
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val windowInsetsController =
            WindowCompat.getInsetsController(baseActivity.window, baseActivity.window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
    }

}