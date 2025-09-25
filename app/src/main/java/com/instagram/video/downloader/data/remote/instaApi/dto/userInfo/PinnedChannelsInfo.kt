package com.instagram.video.downloader.data.remote.instaApi.dto.userInfo

import com.google.gson.annotations.SerializedName

data class PinnedChannelsInfo(

	@field:SerializedName("has_public_channels")
	val hasPublicChannels: Boolean? = null,

	@field:SerializedName("pinned_channels_list")
	val pinnedChannelsList: List<Any?>? = null
)