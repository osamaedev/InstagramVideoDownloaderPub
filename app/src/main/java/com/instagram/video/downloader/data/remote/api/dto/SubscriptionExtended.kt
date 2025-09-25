package com.instagram.video.downloader.data.remote.api.dto

data class SubscriptionExtended(
    val isReturned: Boolean,
    val uniqueId: String,
    val priceAmount: Float,
    val isAutoRenewing: Boolean,
    val priceCurrencyCode: String,
    val startTime: Long,
    val expiryTime: Long,
    val autoResumeTime: Long?,
    val status: String,
    val orderId: String,
    val countryCode: String,
    val profileName: String?,
    val emailAddress: String?,
    val profileId: String?,
    val firstName: String?,
    val familyName: String?,
    val googlePlayToken: String?,
    val plan: Plan,
)
