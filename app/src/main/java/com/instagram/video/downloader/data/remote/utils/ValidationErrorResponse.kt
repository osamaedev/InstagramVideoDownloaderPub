package com.instagram.video.downloader.data.remote.utils

import com.google.gson.annotations.SerializedName

data class ValidationErrorResponse(
    @SerializedName("error")
    val error: Map<String, List<String>>,

    @SerializedName("code")
    val code: Int,
)
