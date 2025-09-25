package com.instagram.video.downloader.ui.home.explore.collection

sealed interface CollectionAction {
    data object OnSwipeRefresh : CollectionAction
    data object OnShowInstagramLoginDialog : CollectionAction
    data object OnReloadClick : CollectionAction
    data object HideAds: CollectionAction
}