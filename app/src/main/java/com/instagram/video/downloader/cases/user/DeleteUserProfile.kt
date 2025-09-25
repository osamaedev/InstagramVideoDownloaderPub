package com.instagram.video.downloader.cases.user

import com.instagram.video.downloader.data.DataManager
import com.instagram.video.downloader.data.local.room.entities.User
import timber.log.Timber
import javax.inject.Inject

class DeleteUserProfile @Inject constructor(private val dataManager: DataManager) {
    suspend operator fun invoke(user: User) {
        try {
//            dataManager.setMediaUsersToDefaultUser(user.identifier)
//            dataManager.setPostsToDefaultUser(user.identifier)
            dataManager.deleteUser(user = user)
        } catch (e: Exception) {
            Timber.tag("DeleteUserProfile").e(e)
        }
    }
}