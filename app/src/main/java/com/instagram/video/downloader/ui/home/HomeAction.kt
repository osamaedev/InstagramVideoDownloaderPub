package com.instagram.video.downloader.ui.home

import androidx.annotation.StringRes

sealed interface HomeAction {

    data class OnShowMessage(@StringRes val message: Int) : HomeAction

    data object OnShowFaq : HomeAction
    data object OnShowInstagramLogin : HomeAction
    data object OnOpenInstagram : HomeAction
    data object OnOpenHowToDownload : HomeAction
    data object OnShowDownloadInputDialog : HomeAction

    data object OnAddNewUser : HomeAction
    data object OnShowVersionNoLongerSupported : HomeAction
    data object OnLoadNativeAds : HomeAction
    data object OnLoadInterAds : HomeAction

    data object OnShowShouldSubscribeDialog : HomeAction

    data class OnShowReachedLimitWithoutLogin(val mediaUrl: String = "") : HomeAction
    data class OnShowAdOrReviewRequiredDialog(val mediaUrl: String = "") : HomeAction
    data class OnShowRewardedAdRequiredDialog(val mediaUrl: String = "") : HomeAction

    data object OnShowTempInvalidSubscription : HomeAction

    data object OnShowLoginRequiredForPrivatePost : HomeAction

    data object OnShowHighlightsToDownloadDialog : HomeAction

    data object OnCheckIntent: HomeAction
    data object OnJustOpenSplash: HomeAction

    data object HideAds: HomeAction
}