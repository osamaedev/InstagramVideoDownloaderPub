package com.instagram.video.downloader.data.remote.instaApi.dto.collections

data class CollectionPage(
    val mediaList: List<CollectionMedia>?,
    val pageAfter: String?,
)
