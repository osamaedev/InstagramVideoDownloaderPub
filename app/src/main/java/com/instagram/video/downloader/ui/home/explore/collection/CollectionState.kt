package com.instagram.video.downloader.ui.home.explore.collection

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.instagram.video.downloader.cases.user.LoggedInUser
import com.instagram.video.downloader.data.local.room.entities.PostToDownload
import com.instagram.video.downloader.data.remote.api.dto.User

data class CollectionState(
    var loggedInUser: LoggedInUser? = null,
    @StringRes var error: Int? = null,
    var isLoading: Boolean = false,
    var isSwipeLoading: Boolean = false,
    var isCookiesExpired: Boolean = false,


    var isMassDownloading: Boolean = false,
    var currentUser: User? = null,
    var massMediaToDownload: List<PostToDownload>? = null,
)
