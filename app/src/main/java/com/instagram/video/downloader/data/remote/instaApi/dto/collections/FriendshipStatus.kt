package com.instagram.video.downloader.data.remote.instaApi.dto.collections

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize


@Parcelize
data class FriendshipStatus(

	@field:SerializedName("is_restricted")
	val isRestricted: Boolean? = null,

	@field:SerializedName("following")
	val following: Boolean? = null,

	@field:SerializedName("is_feed_favorite")
	val isFeedFavorite: Boolean? = null,

	@field:SerializedName("is_bestie")
	val isBestie: Boolean? = null
) : Parcelable