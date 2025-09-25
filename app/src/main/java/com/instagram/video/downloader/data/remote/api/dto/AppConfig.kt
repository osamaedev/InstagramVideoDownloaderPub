package com.instagram.video.downloader.data.remote.api.dto

import com.google.gson.annotations.SerializedName

data class AppConfig(

    @field:SerializedName("advertisements")
    val advertisements: List<Advertisement>,

    @field:SerializedName("unique_id")
    val uniqueId: String,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("is_ads_enabled")
    val isAdsEnabled: Boolean,

    @field:SerializedName("max_downloads")
    val maxDownloads: Int,

    @field:SerializedName("mass_downloads_to_get_as_reward")
    val massDownloadsToGetAsReward: Int,

    @field:SerializedName("is_testing")
    val isTesting: Boolean,

    @field:SerializedName("login_page_url")
    val loginPageUrl: String,

    @field:SerializedName("current_yearly_google_id_discount")
    val googleIdDiscount: String?,
)