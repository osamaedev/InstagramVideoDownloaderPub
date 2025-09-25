package com.instagram.video.downloader.ui.base

import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.instagram.video.downloader.R
import com.instagram.video.downloader.common.isNetworkConnected
import com.instagram.video.downloader.data.local.prefs.PrefsHelper
import com.instagram.video.downloader.databinding.ToastMessageLayoutBinding
import com.instagram.video.downloader.di.qualifiers.MainThreadHandler
import javax.inject.Inject


abstract class BaseActivity<T : ViewDataBinding> : AppCompatActivity() {

    private var loadingDialog: LoadingDialog? = null

    protected lateinit var binding: T

    @MainThreadHandler
    @Inject
    lateinit var handler: Handler

    @LayoutRes
    protected abstract fun getLayoutId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        performDataBinding()
    }

    private fun performDataBinding() {
        binding = DataBindingUtil.setContentView(this, getLayoutId())
    }

    fun showLoading() {
        runOnUiThread {
            if (loadingDialog != null)
                hideLoading()
            loadingDialog = LoadingDialog()
            loadingDialog?.setCancelable(false)
            if (!isFinishing)
                loadingDialog?.show(supportFragmentManager, LoadingDialog.TAG)
        }
    }

    fun hideLoading() {
        runOnUiThread {
            if (loadingDialog != null) {
                loadingDialog?.dismiss()
            }
        }
    }

    fun isNetworkConnected(): Boolean {
        return isNetworkConnected(this)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}

fun AppCompatActivity.showMessage(message: String) {
    runOnUiThread {
        val binding = ToastMessageLayoutBinding.inflate(LayoutInflater.from(this))
        binding.message.text = message
        val toast = Toast(application)
        toast.view = binding.root
        toast.setGravity(Gravity.TOP, 0, 180)
        toast.duration = Toast.LENGTH_SHORT
        toast.show()
    }
}

fun AppCompatActivity.showMessage(messageId: Int) {
    runOnUiThread {
        showMessage(getString(messageId))
    }
}

fun AppCompatActivity.hideKeyboard() {
    val view = this.currentFocus
    if (view != null) {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?)?.hideSoftInputFromWindow(
            view.windowToken,
            0
        )
    }
}

fun AppCompatActivity.hideKeyboard(view: View) {
    val inputManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
    inputManager?.hideSoftInputFromWindow(view.windowToken, 0)
}

fun AppCompatActivity.showKeyboard() {
    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.toggleSoftInput(
        InputMethodManager.SHOW_IMPLICIT,
        InputMethodManager.HIDE_IMPLICIT_ONLY
    )
}