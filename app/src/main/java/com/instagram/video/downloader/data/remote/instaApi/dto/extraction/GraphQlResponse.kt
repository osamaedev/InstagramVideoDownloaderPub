package com.instagram.video.downloader.data.remote.instaApi.dto.extraction

import com.google.gson.annotations.SerializedName

data class GraphQlResponse(

	@field:SerializedName("graphql")
	val graphql: Graphql? = null,

	@field:SerializedName("showQRModal")
	val showQRModal: Boolean? = null
)

data class Dimensions2(

	@field:SerializedName("width")
	val width: Int? = null,

	@field:SerializedName("height")
	val height: Int? = null
)

data class Graphql(

	@field:SerializedName("shortcode_media")
	val shortcodeMedia: ShortcodeMedia? = null
)

data class ShortcodeMedia(

	@field:SerializedName("__typename")
	val typename: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("shortcode")
	val shortcode: String? = null,

	@field:SerializedName("dimensions")
	val dimensions: Dimensions2? = null
)
