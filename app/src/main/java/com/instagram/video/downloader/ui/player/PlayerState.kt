package com.instagram.video.downloader.ui.player

import androidx.annotation.StringRes
import com.instagram.video.downloader.data.local.room.entities.PostToDownload
import com.instagram.video.downloader.data.remote.api.dto.AppConfig
import com.instagram.video.downloader.data.remote.api.dto.User

data class PlayerState(
    @StringRes var error: Int? = null,
    var isLoading: Boolean = false,
    var isNetworkAvailable: Boolean = false,
    var currentUser: User? = null,
    var appConfig: AppConfig? = null,
    var isFileDownloading: Boolean = false,
    var postWithMediaToDownload: PostToDownload? = null,
)
