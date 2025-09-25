package com.instagram.video.downloader.data.remote.instaApi.dto.userInfo

import com.google.gson.annotations.SerializedName

data class UserInfo(

	@field:SerializedName("user")
	val user: User? = null,

	@field:SerializedName("status")
	val status: String? = null
)