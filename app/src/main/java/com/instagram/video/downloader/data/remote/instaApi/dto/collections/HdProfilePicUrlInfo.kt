package com.instagram.video.downloader.data.remote.instaApi.dto.collections

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class HdProfilePicUrlInfo(

	@field:SerializedName("width")
	val width: Int? = null,

	@field:SerializedName("url")
	val url: String? = null,

	@field:SerializedName("height")
	val height: Int? = null
) : Serializable