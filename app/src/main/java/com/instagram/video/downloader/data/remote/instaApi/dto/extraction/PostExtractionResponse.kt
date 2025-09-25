package com.instagram.video.downloader.data.remote.instaApi.dto.extraction

import com.google.gson.annotations.SerializedName
import com.instagram.video.downloader.data.remote.api.dto.PostResponse

data class PostExtractionResponse(

	@field:SerializedName("graphql")
	val graphql: Graphql? = null,

	@field:SerializedName("more_available")
	val moreAvailable: Boolean,

	@field:SerializedName("auto_load_more_enabled")
	val autoLoadMoreEnabled: Boolean,

	@field:SerializedName("items")
	val items: List<PostResponse>,

	@field:SerializedName("num_results")
	val numResults: Int,

	@field:SerializedName("showQRModal")
	val showQRModal: Boolean
)
