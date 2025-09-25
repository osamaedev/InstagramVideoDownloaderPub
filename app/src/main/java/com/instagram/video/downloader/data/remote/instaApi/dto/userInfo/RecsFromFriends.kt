package com.instagram.video.downloader.data.remote.instaApi.dto.userInfo

import com.google.gson.annotations.SerializedName

data class RecsFromFriends(

	@field:SerializedName("enable_recs_from_friends")
	val enableRecsFromFriends: Boolean? = null,

	@field:SerializedName("recs_from_friends_entry_point_type")
	val recsFromFriendsEntryPointType: String? = null
)