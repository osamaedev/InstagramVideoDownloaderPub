package com.instagram.video.downloader.cases.user

import com.instagram.video.downloader.data.DataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetLocalPostByInstaId @Inject constructor(private val dataManager: DataManager) {

    suspend fun invoke(instagramId: String) = withContext(Dispatchers.IO) {
        return@withContext dataManager.getPostWithAllByInstagramId(instagramId)
    }
}