package com.instagram.video.downloader.data.remote.api.dto

data class Plan(
    val uniqueId: String,
    val duration: String,
    val tagName: String,
    val googlePlayId: String,
    val basePlanGoogleId: String,
    val price: Float,
    val name: String,
    val discount: Float,
    val isTrial: Boolean,
)
