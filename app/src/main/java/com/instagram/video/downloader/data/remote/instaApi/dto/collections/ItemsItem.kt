package com.instagram.video.downloader.data.remote.instaApi.dto.collections

import com.google.gson.annotations.SerializedName

data class ItemsItem(

	@field:SerializedName("media")
	val media: CollectionMedia
)