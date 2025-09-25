package com.instagram.video.downloader.data.remote.utils

import com.google.gson.annotations.SerializedName

data class LoginErrorResponse(
    @SerializedName("error")
    val error: String,

    @SerializedName("error_description")
    val description: String,

    @SerializedName("message")
    val message: String,
)