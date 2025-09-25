package com.instagram.video.downloader.data.remote.api.dto

import com.google.gson.annotations.SerializedName

data class UserDto(

    @field:SerializedName("unique_id")
    val uniqueId: String,

    @field:SerializedName("android_id")
    val androidId: String?,

    @field:SerializedName("is_tester")
    val isTester: Boolean,

    @field:SerializedName("remaining_mass_download_count")
    val remainingMassDownloadCount: Int,

    @field:SerializedName("subscription")
    var subscription: SubscriptionDto? = null,
)

fun UserDto.toUser() = User(
    uniqueId,
    androidId,
    isTester,
    remainingMassDownloadCount,
    subscription,
)

fun UserDto.hasValidSubscription() =
    subscription != null && subscription?.toSubscription()?.isValidSubscription == true
