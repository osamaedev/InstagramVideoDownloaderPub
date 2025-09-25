package com.instagram.video.downloader.ui.plans

import androidx.annotation.StringRes
import com.instagram.video.downloader.data.remote.api.dto.AppConfig
import com.instagram.video.downloader.data.remote.api.dto.User

data class PlansState(
    var isPlansLoading: Boolean = false,
    @StringRes var error: Int? = null,
    var isLoading: Boolean = false,
    var isNetworkAvailable: Boolean? = null,
    var isGoogleBillingSetupDone: Boolean = false,
    var currentUser: User? = null,
    var appConfig: AppConfig? = null,
)