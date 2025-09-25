package com.instagram.video.downloader.data.remote.api.dto

data class Subscription(
    val isReturned: Boolean,
    val uniqueId: String,
    val priceAmount: Float,
    val isAutoRenewing: Boolean,
    val priceCurrencyCode: String,
    val startTime: Long,
    val countryCode: String,
    val profileName: String?,
    val emailAddress: String?,
    val profileId: String?,
    val expiryTime: Long,
    val orderId: String,
    val autoResumeTime: Long?,
    val firstName: String?,
    val familyName: String?,
    val status: String,
    val googleToken: String,
    val isValidSubscription: Boolean,
)