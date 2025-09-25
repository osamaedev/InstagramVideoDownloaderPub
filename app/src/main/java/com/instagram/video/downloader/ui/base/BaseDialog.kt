package com.instagram.video.downloader.ui.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.annotation.LayoutRes
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import com.instagram.video.downloader.R

abstract class BaseDialog<T : ViewDataBinding> :
    DialogFragment() {

    protected lateinit var baseActivity: BaseActivity<*>
    protected lateinit var viewDataBinding: T


    @LayoutRes
    protected abstract fun getLayoutId(): Int

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity<out ViewDataBinding>) {
            baseActivity = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewDataBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        return viewDataBinding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root = RelativeLayout(activity)
        val params = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        root.layoutParams = params
        val dialog = Dialog(baseActivity)
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

    fun showMessage(message: String) = baseActivity.showMessage(message)

    fun showMessage(messageId: Int) = baseActivity.showMessage(messageId)

    fun hideKeyboard() = baseActivity.hideKeyboard()

    fun hideKeyboard(view: View) = baseActivity.hideKeyboard(view)

    fun showKeyboard() = baseActivity.showKeyboard()

    fun showLoading() = baseActivity.showLoading()

    fun hideLoading() = baseActivity.hideLoading()

    fun isNetworkConnected() = com.instagram.video.downloader.common.isNetworkConnected(baseActivity)

}