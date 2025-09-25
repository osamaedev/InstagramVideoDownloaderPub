package com.instagram.video.downloader.data.remote.instaApi.dto.userInfo

import com.google.gson.annotations.SerializedName

data class CreatorShoppingInfo(

	@field:SerializedName("linked_merchant_accounts")
	val linkedMerchantAccounts: List<Any?>? = null
)