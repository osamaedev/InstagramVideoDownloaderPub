package com.instagram.video.downloader.common.download

import com.instagram.video.downloader.data.local.room.entities.MediaToDownload
import com.instagram.video.downloader.data.local.room.entities.PostToDownload
import com.instagram.video.downloader.data.local.room.entities.PostToDownloadWithMediaToDownload

interface IDownloaderManager {
    suspend fun addToDownloadItems(postAndMediaItems: PostToDownload)
    suspend fun addToDownloadItems(postsAndMediaItems: List<PostToDownload>)
    suspend fun buildNextRequest(postAndMediaItems: PostToDownloadWithMediaToDownload): MediaToDownload?
}

enum class MediaType {
    IMAGE, VIDEO, MUSIC
}

fun String.toMediaType(): MediaType {
    return when (this) {
        "IMAGE" -> MediaType.IMAGE
        "VIDEO" -> MediaType.VIDEO
        "MUSIC" -> MediaType.MUSIC
        else -> {
            throw Exception("Unknown MediaType Value")
        }
    }
}