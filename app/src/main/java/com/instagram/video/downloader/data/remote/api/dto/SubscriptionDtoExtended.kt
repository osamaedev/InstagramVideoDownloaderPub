package com.instagram.video.downloader.data.remote.api.dto

import com.google.gson.annotations.SerializedName

data class SubscriptionDtoExtended(

    @field:SerializedName("is_returned")
    val isReturned: Boolean,

    @field:SerializedName("unique_id")
    val uniqueId: String,

    @field:SerializedName("price_amount")
    val priceAmount: Float,

    @field:SerializedName("is_auto_renewing")
    val isAutoRenewing: Boolean,

    @field:SerializedName("price_currency_code")
    val priceCurrencyCode: String,

    @field:SerializedName("start_time")
    val startTime: Long,

    @field:SerializedName("expiry_time")
    val expiryTime: Long,

    @field:SerializedName("auto_resume_time")
    val autoResumeTime: Long? = null,


    @field:SerializedName("status")
    val status: String,


    @field:SerializedName("order_id")
    val orderId: String,


    @field:SerializedName("country_code")
    val countryCode: String,

    @field:SerializedName("profile_name")
    val profileName: String? = null,

    @field:SerializedName("email_address")
    val emailAddress: String? = null,

    @field:SerializedName("profile_id")
    val profileId: String? = null,


    @field:SerializedName("first_name")
    val firstName: String? = null,

    @field:SerializedName("family_name")
    val familyName: String? = null,


    @field:SerializedName("google_play_token")
    val googlePlayToken: String? = null,


    @field:SerializedName("plan")
    val plan: PlanDto,
)

fun SubscriptionDtoExtended.toSubscriptionExtended() = SubscriptionExtended(
    isReturned,
    uniqueId,
    priceAmount,
    isAutoRenewing,
    priceCurrencyCode,
    startTime,
    expiryTime,
    autoResumeTime,
    status,
    orderId,
    countryCode,
    profileName,
    emailAddress,
    profileId,
    firstName,
    familyName,
    googlePlayToken,
    plan.toPlan(),
)
