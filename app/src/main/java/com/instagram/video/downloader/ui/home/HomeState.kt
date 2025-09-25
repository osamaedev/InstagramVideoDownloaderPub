package com.instagram.video.downloader.ui.home

import androidx.annotation.StringRes
import com.instagram.video.downloader.data.local.room.entities.PostToDownload
import com.instagram.video.downloader.data.remote.api.dto.User

data class HomeState(
    @StringRes var error: Int? = null,
    var isLoading: Boolean = false,
    var isExtractingUserInfoFailed: Boolean = false,
    var isNetworkAvailable: Boolean = false,
    var isFileDownloading: Boolean = false,
    var currentUser: User? = null,
    var postWithMediaToDownload: PostToDownload? = null,
)
