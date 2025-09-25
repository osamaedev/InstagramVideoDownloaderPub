package com.instagram.video.downloader.cases.general

import com.instagram.video.downloader.data.DataManager
import javax.inject.Inject

class CheckIntentDownloadLink @Inject constructor(private val dataManager: DataManager) {
    fun getLinkToDownload() = dataManager.getIntentLinkToDownload()
    fun clearLinkToDownload() = dataManager.setIntentLinkToDownload("")
    fun setDownloadLink(link: String) = dataManager.setIntentLinkToDownload(link)
}