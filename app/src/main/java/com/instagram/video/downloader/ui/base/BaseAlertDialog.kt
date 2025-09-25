package com.instagram.video.downloader.ui.base

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.button.MaterialButton
import com.instagram.video.downloader.R
import com.instagram.video.downloader.common.Presets
import com.instagram.video.downloader.common.ui.extensions.dpToPx
import com.instagram.video.downloader.common.ui.extensions.inflate
import com.instagram.video.downloader.common.ui.extensions.layoutInflater
import com.instagram.video.downloader.databinding.DialogAlertBaseBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class BaseAlertDialog(context: Context) : AlertDialog(context, R.style.BaseAlertDialog) {

    private val binding = DialogAlertBaseBinding.inflate(LayoutInflater.from(context))
    internal var additionalContentView: View? = null

    var isPremiumCelebration = false

    init {
        setView(binding.root)
        binding.closeButton.setOnClickListener { dismiss() }
        binding.dialogContainer.clipChildren = true
        binding.dialogContainer.clipToOutline = true
//        window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
    }


    override fun onStart() {
        super.onStart()

        if (isPremiumCelebration) {
            binding.confettiContainer.isVisible = true
            var num = 10
            CoroutineScope(Dispatchers.Main).launch {
                while (num > 1) {
                    binding.konfettiView.start(Presets.explode())
                    delay(2000)
                    num -= 1
                }
            }
        }
    }

    override fun setTitle(title: CharSequence?) {
        binding.titleTextView.isVisible = (title?.length ?: 0) > 0
        binding.titleTextView.text = title
    }

    override fun setTitle(titleId: Int) {
        this.setTitle(context.getString(titleId))
    }

    override fun setMessage(message: CharSequence?) {
        binding.messageTextView.isVisible = (message?.length ?: 0) > 0
        binding.messageTextView.text = message
    }

    fun setMessage(textResId: Int) {
        this.setMessage(context.getString(textResId))
    }

    fun setNotice(notice: CharSequence?) {
        if ((notice?.length ?: 0) > 0) {
            binding.noticeTextView.visibility = View.VISIBLE
        } else {
            binding.noticeTextView.visibility = View.GONE
        }
        binding.noticeTextView.text = notice
    }

    fun setNotice(noticeResId: Int) {
        setNotice(context.getString(noticeResId))
    }

    fun setCustomHeaderView(customHeader: View) {
        binding.dialogContainer.addView(customHeader, 0)
        binding.dialogContainer.setPadding(0, 0, 0, binding.dialogContainer.paddingBottom)
    }

    fun setAdditionalContentView(layoutResID: Int) {
        setAdditionalContentView(
            context.layoutInflater.inflate(
                layoutResID,
                binding.root as ViewGroup,
                false
            )
        )
    }

    fun setAdditionalContentView(view: View?) {
        (binding.root as ViewGroup).removeView(additionalContentView)
        additionalContentView = view
        binding.contentView.addView(view)
        val layoutParams = view?.layoutParams
        layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams?.height = WRAP_CONTENT
        view?.layoutParams = layoutParams
        binding.contentView.forceLayout()
    }

    fun setAdditionalContentSidePadding(padding: Int) {
        binding.contentView.setPadding(padding, 0, padding, binding.contentView.paddingBottom)
        binding.messageTextView.setPadding(
            padding,
            binding.messageTextView.paddingTop,
            padding,
            binding.messageTextView.paddingBottom
        )
    }

    fun setExtraCloseButtonVisible(isVisible: Boolean) {
        binding.closeButton.isVisible = isVisible
    }

    fun getContentView(): View? = additionalContentView

    fun addButton(
        stringRes: Int,
        isPrimary: Boolean,
        isDestructive: Boolean = false,
        autoDismiss: Boolean = true,
        isSubscribeButton: Boolean = false,
        isLoginButton: Boolean = false,
        function: ((BaseAlertDialog, Int) -> Unit)? = null,
    ): Button {
        return addButton(
            context.getString(stringRes),
            isPrimary,
            isDestructive,
            autoDismiss,
            isSubscribeButton,
            isLoginButton,
            function
        )
    }

    fun addButton(
        string: String,
        isPrimary: Boolean,
        isDestructive: Boolean = false,
        autoDismiss: Boolean = true,
        isSubscribeButton: Boolean = false,
        isLoginButton: Boolean = false,
        function: ((BaseAlertDialog, Int) -> Unit)? = null,
    ): Button {
        val button: Button =
            if (isPrimary) {
                when {
                    isDestructive -> {
                        binding.buttonsWrapper.inflate(R.layout.dialog_primary_destructive_button) as? Button
                    }

                    isSubscribeButton -> {
                        binding.buttonsWrapper.inflate(R.layout.dialog_subscribe_button) as? Button
                    }

                    isLoginButton -> {
                        binding.buttonsWrapper.inflate(R.layout.dialog_login_button) as? Button
                    }

                    else -> {
                        binding.buttonsWrapper.inflate(R.layout.dialog_primary_button) as? Button
                    }
                }
            } else {
                val button =
                    binding.buttonsWrapper.inflate(R.layout.dialog_secondary_button) as? Button
                if (isDestructive) {
                    button?.setTextColor(ContextCompat.getColor(context, R.color.maroon_100))
                }
                button
            } ?: Button(context)
        button.text = string
        return addButton(button, autoDismiss, function) as Button
    }

    fun addButton(
        buttonView: View,
        autoDismiss: Boolean = true,
        function: ((BaseAlertDialog, Int) -> Unit)? = null,
    ): View {
        val weakThis = WeakReference(this)
        val buttonIndex = binding.buttonsWrapper.childCount
        buttonView.setOnClickListener {
            weakThis.get()?.let { it1 ->
                if (function != null) {
                    function(it1, buttonIndex)
                }
                if (autoDismiss) {
                    dismiss()
                }
            }
        }
        configureButtonLayoutParams(buttonView)
        binding.buttonsWrapper.addView(buttonView)
        // for some reason the padding gets lost somewhere.
//        buttonView.setPadding(24.dpToPx(context), 0, 5.dpToPx(context), 0)
        return buttonView
    }

    private fun configureButtonLayoutParams(buttonView: View) {
        val layoutParams =
//            if (isScrollingLayout) {
//                val params = LinearLayout.LayoutParams(0, 48.dpToPx(context))
//                params.weight = 1f
//                params
//            } else {
            LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
//            }
        buttonView.layoutParams = layoutParams
        (buttonView as MaterialButton).cornerRadius = 8.dpToPx(context)
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }
}