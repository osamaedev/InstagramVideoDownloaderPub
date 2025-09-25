package com.instagram.video.downloader.data.remote.utils

import com.google.gson.annotations.SerializedName

data class GeneralErrorResponse(
    @SerializedName("error")
    val error: String,

    @SerializedName("code")
    val code: Int,
)
