package com.instagram.video.downloader.data.remote.instaApi.dto.userInfo

import com.google.gson.annotations.SerializedName

data class BiographyWithEntities(

	@field:SerializedName("raw_text")
	val rawText: String? = null,

	@field:SerializedName("entities")
	val entities: List<Any?>? = null
)