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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.instagram.video.downloader.di.qualifiers.MainThreadHandler
import javax.inject.Inject

abstract class BaseBottomSheetDialog<T : ViewDataBinding> : BottomSheetDialogFragment() {

    protected lateinit var baseActivity: BaseActivity<*>

    protected lateinit var binding: T

    @MainThreadHandler
    @Inject
    lateinit var handler: Handler

    @LayoutRes
    protected abstract fun getLayoutId(): Int

//    abstract fun setupViewModel(viewModel: BaseViewModel<out IBaseNavigator>)


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
        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        return binding.root
    }

    fun showMessage(message: String) = baseActivity.showMessage(message)

    fun showMessage(messageId: Int) = baseActivity.showMessage(messageId)

    fun hideKeyboard() = baseActivity.hideKeyboard()

    fun hideKeyboard(view: View) = baseActivity.hideKeyboard(view)

    fun showKeyboard() = baseActivity.showKeyboard()

    fun showLoading() = baseActivity.showLoading()

    fun hideLoading() = baseActivity.hideLoading()

    fun isNetworkConnected() =
        com.instagram.video.downloader.common.isNetworkConnected(baseActivity)
}