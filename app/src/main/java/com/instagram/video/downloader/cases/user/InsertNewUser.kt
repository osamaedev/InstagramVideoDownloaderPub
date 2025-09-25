package com.instagram.video.downloader.cases.user

import com.instagram.video.downloader.common.getCookiesExpiringAt
import com.instagram.video.downloader.data.DataManager
import com.instagram.video.downloader.data.local.room.entities.User
import com.instagram.video.downloader.data.remote.instaApi.dto.userInfo.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InsertNewUser @Inject constructor(private val dataManager: DataManager) {
    suspend operator fun invoke(userInfo: UserInfo, cookies: String) = withContext(Dispatchers.IO) {
        val insertedUserId = dataManager.insertUser(
            User(
                instagramId = userInfo.user?.id!!,
                fullName = userInfo.user.fullName!!,
                biography = userInfo.user.biography!!,
                username = userInfo.user.username!!,
                profilePicUrl = userInfo.user.profilePicUrl!!,
                cookies = cookies,
                loggedInAt = System.currentTimeMillis(),
                cookiesExpiringAt = getCookiesExpiringAt(cookies),
                updatedAt = System.currentTimeMillis(),
                createAt = System.currentTimeMillis(),
            )
        )
        return@withContext dataManager.getUserByIdentifier(insertedUserId)
    }
}