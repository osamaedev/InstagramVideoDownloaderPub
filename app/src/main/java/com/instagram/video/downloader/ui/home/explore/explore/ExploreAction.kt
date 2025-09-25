package com.instagram.video.downloader.ui.home.explore.explore

sealed interface ExploreAction {
    data object OnSwipeRefresh: ExploreAction
    data object OnShowInstagramLoginDialog: ExploreAction

    data object HideAds: ExploreAction
}