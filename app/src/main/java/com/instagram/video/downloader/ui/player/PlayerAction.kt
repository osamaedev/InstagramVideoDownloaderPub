package com.instagram.video.downloader.ui.player

import androidx.annotation.StringRes

interface PlayerAction {

    data class OnShowMessage(@StringRes val message: Int) : PlayerAction

    data object OnShowShouldSubscribeDialog : PlayerAction

    data class OnShowReachedLimitWithoutLogin(val media: PlayerDataModel) : PlayerAction
    data class OnShowAdOrReviewRequiredDialog(val media: PlayerDataModel) : PlayerAction
    data class OnShowRewardedAdRequiredDialog(val media: PlayerDataModel) : PlayerAction
    data object OnShowTempInvalidSubscription : PlayerAction

    data object OnShowHighlightsToDownloadDialog : PlayerAction

    data object OnShowLoginRequiredForPrivatePost : PlayerAction
    data object OnShowReachedMaxMassDownloadDialog : PlayerAction
    data object OnShowFakeDrag: PlayerAction
    data object OnHideAds : PlayerAction

}