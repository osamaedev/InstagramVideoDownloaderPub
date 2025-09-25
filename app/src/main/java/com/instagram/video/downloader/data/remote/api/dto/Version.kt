package com.instagram.video.downloader.data.remote.api.dto

import com.google.gson.annotations.SerializedName

data class Version(
	@field:SerializedName("is_supported")
	val isSupported: Boolean,

	@field:SerializedName("unique_id")
	val uniqueId: String,

	@field:SerializedName("code")
	val code: Int,

	@field:SerializedName("app_config")
	val appConfig: AppConfig,

	@field:SerializedName("name")
	val name: String,
)