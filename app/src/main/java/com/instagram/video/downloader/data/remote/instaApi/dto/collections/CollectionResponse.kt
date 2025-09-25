package com.instagram.video.downloader.data.remote.instaApi.dto.collections

import com.google.gson.annotations.SerializedName

data class CollectionResponse(

    @field:SerializedName("more_available")
    val moreAvailable: Boolean? = null,

    @field:SerializedName("auto_load_more_enabled")
    val autoLoadMoreEnabled: Boolean? = null,

    @field:SerializedName("items")
    val items: List<ItemsItem>,

    @field:SerializedName("num_results")
    val numResults: Int? = null,

    @field:SerializedName("status")
    val status: String? = null,

    @field:SerializedName("next_max_id")
    val nextMaxId: String? = null,
)

fun CollectionResponse.toCollectionPage(): CollectionPage {
    val list = this.items.map { it.media }
    return CollectionPage(
        mediaList = list,
        pageAfter = nextMaxId,
    )
}