package com.instagram.video.downloader.data.remote.api.dto

data class User(
    val uniqueId: String,
    val androidId: String?,
    val isTester: Boolean,
    val remainingMassDownloadCount: Int,
    val subscriptionDto: SubscriptionDto?,
)
