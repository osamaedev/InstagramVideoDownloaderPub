package com.instagram.video.downloader.ui.plans

import androidx.annotation.StringRes

sealed interface PlansAction {
    data class OnShowMessage(@StringRes val message: Int) : PlansAction
    data object OnShowSubscribedDialog : PlansAction
//    data object OnStartGoogleBillingService: PlansAction
}