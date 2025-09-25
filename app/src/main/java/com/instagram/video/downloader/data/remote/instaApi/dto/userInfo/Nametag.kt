package com.instagram.video.downloader.data.remote.instaApi.dto.userInfo

import com.google.gson.annotations.SerializedName

data class Nametag(

	@field:SerializedName("emoji_color")
	val emojiColor: Int? = null,

	@field:SerializedName("mode")
	val mode: Int? = null,

	@field:SerializedName("background_image_url")
	val backgroundImageUrl: String? = null,

	@field:SerializedName("emoji")
	val emoji: String? = null,

	@field:SerializedName("selected_theme_color")
	val selectedThemeColor: Int? = null,

	@field:SerializedName("available_theme_colors")
	val availableThemeColors: List<Int?>? = null,

	@field:SerializedName("gradient")
	val gradient: Int? = null,

	@field:SerializedName("selfie_url")
	val selfieUrl: String? = null,

	@field:SerializedName("is_background_image_blurred")
	val isBackgroundImageBlurred: Boolean? = null,

	@field:SerializedName("selfie_sticker")
	val selfieSticker: Int? = null
)