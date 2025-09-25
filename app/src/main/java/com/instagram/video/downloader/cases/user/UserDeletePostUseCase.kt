package com.instagram.video.downloader.cases.user

import com.instagram.video.downloader.data.DataManager
import com.instagram.video.downloader.data.local.room.entities.PostWithMediaUserAndMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserDeletePostUseCase @Inject constructor(private val dataManager: DataManager) {

    suspend operator fun invoke(postWithMediaUserAndMedia: PostWithMediaUserAndMedia) =
        withContext(Dispatchers.IO) {
            // delete all media the post will be deleted
            postWithMediaUserAndMedia.media.forEach {
                dataManager.deleteMedia(media = it)
            }
            dataManager.deletePost(post = postWithMediaUserAndMedia.post)
            return@withContext
        }

}