package com.instagram.video.downloader.ui.player

data class PlayerItemDownloadingInfo(
    val instagramId: String,
    val currentProgress: Double,
    val downloadManagerId: Long,
)
