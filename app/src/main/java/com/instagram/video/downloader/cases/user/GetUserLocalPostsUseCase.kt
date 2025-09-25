package com.instagram.video.downloader.cases.user

import com.instagram.video.downloader.data.DataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class GetUserLocalPostsUseCase @Inject constructor(private val dataManager: DataManager) {

    suspend operator fun invoke(currentUser: LoggedInUser) = withContext(Dispatchers.IO) {
        try {
            val posts = dataManager.getUserPosts()
            return@withContext posts
        } catch (e: Exception) {
            Timber.tag("GetUserLocalPostUseCase").e(e)
            return@withContext emptyList()
        }
    }

}