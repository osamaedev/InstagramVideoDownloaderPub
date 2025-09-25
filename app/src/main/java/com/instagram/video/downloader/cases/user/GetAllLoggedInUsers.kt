package com.instagram.video.downloader.cases.user

import com.instagram.video.downloader.data.DataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetAllLoggedInUsers @Inject constructor(private val dataManager: DataManager) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        dataManager.getAllUsers()
    }
}