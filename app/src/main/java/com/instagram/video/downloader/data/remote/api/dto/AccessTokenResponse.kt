package com.instagram.video.downloader.data.remote.api.dto

import com.google.gson.annotations.SerializedName

data class AccessTokenResponse(
    @SerializedName("token_type")
    val tokenType: String,
    @SerializedName("expires_in")
    val expiresIn: Long,
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("refresh_token")
    val refreshToken: String
)
