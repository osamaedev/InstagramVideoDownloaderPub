package com.instagram.video.downloader.cases.general

import com.instagram.video.downloader.data.DataManager
import javax.inject.Inject

class FakeDragCases @Inject constructor(private val dataManager: DataManager) {

    fun getExploreFakeDrag() = dataManager.getExploreFakeDragShown()
    fun setExploreFakeDrag(isShown: Boolean) = dataManager.setExploreFakeDragShown(isShown)

    fun getCollectionFakeDrag() = dataManager.getCollectionFakeDragShown()
    fun setCollectionFakeDrag(isShown: Boolean) = dataManager.setCollectionFakeDragShown(isShown)

    fun getDownloadedFakeDrag() = dataManager.getDownloadFakeDragShown()
    fun setDownloadedFakeDrag(isShown: Boolean) = dataManager.setDownloadedFakeDragShown(isShown)

}