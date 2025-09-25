package com.instagram.video.downloader.ui.splash

import com.instagram.video.downloader.data.remote.api.dto.AppConfig
import com.instagram.video.downloader.data.remote.api.dto.UserDto

data class SplashState(
    var isUserAndAppCheckDone: Boolean = false,
    var error: Int? = null,
    var isLoading: Boolean = false,
    var appConfig: AppConfig? = null,
    var isNetworkAvailable: Boolean = false,
    var user: UserDto? = null,
)
