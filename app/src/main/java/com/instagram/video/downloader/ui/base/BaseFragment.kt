package com.instagram.video.downloader.ui.base

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.instagram.video.downloader.di.qualifiers.MainThreadHandler
import javax.inject.Inject

abstract class BaseFragment<T : ViewDataBinding> : Fragment() {

    private var baseActivity: BaseActivity<*>? = null

    protected lateinit var binding: T

    @MainThreadHandler
    @Inject
    lateinit var handler: Handler

    @LayoutRes
    protected abstract fun getLayoutId(): Int


    fun baseActivity() = baseActivity


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (this::binding.isInitialized.not()) {
            binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // NO-OP
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is BaseActivity<*>) this.baseActivity = context
    }

    override fun onDetach() {
        super.onDetach()
        handler.removeCallbacksAndMessages(null);
        baseActivity = null
    }

    fun showMessage(message: String) {
        baseActivity?.showMessage(message)
    }

    fun showMessage(messageId: Int) {
        baseActivity?.showMessage(messageId)
    }

    fun hideKeyboard() {
        baseActivity?.hideKeyboard()
    }

    fun hideKeyboard(view: View) {
        baseActivity?.hideKeyboard(view)
    }

    fun showKeyboard() {
        baseActivity?.showKeyboard()
    }

    fun showLoading() {
        baseActivity?.showLoading()
    }

    fun hideLoading() {
        baseActivity?.hideLoading()
    }

    fun isNetworkConnected(): Boolean {
        baseActivity?.isNetworkConnected()?.let {
            return it
        }
        return false
    }
}