package com.instagram.video.downloader.ui.base

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import com.instagram.video.downloader.R
import com.instagram.video.downloader.common.ui.views.InstaDownloaderCircularProgressView

class LoadingDialog : DialogFragment() {

    companion object {
        const val TAG = "LoadingDialog"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_loading, container)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val progressView = view.findViewById<ComposeView>(R.id.progress_view)
        progressView.setContent {
            InstaDownloaderCircularProgressView()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root = RelativeLayout(activity)
        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        root.layoutParams = params
        val dialog = Dialog(root.context)
        dialog.setContentView(root)
        if (dialog.window != null) {
            dialog.window!!.setBackgroundDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.transparent_dialog_bg,
                    null
                )
            )
        }
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }


}