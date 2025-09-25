package com.instagram.video.downloader.data.remote.api.dto

import com.google.gson.annotations.SerializedName

data class Advertisement(

	@field:SerializedName("app_config_id")
	val appConfigId: Int,

	@field:SerializedName("is_enabled")
	val isEnabled: Boolean,

	@field:SerializedName("ad_id")
	val adId: String,

	@field:SerializedName("unique_id")
	val uniqueId: String,

	@field:SerializedName("provider")
	val provider: String,

	@field:SerializedName("name")
	val name: String,

	@field:SerializedName("type")
	val type: String
)