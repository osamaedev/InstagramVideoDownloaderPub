package com.instagram.video.downloader.cases.user

import com.instagram.video.downloader.data.DataManager
import com.instagram.video.downloader.data.remote.api.dto.SubscriptionDto
import com.instagram.video.downloader.data.remote.api.dto.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateRemoteUser @Inject constructor(private val dataManager: DataManager) {

    suspend fun updateRemainingMassDownloadCount(newMassDownloadCount: Int, userDto: UserDto) =
        withContext(Dispatchers.IO) {
            return@withContext dataManager.updateUser(
                userUniqueId = userDto.uniqueId,
                accessToken = dataManager.getApplicationAccessToken()?.accessToken!!,
                data = HashMap<String, Any>().apply {
                    set("remaining_mass_download_count", newMassDownloadCount)
                }
            )
        }


    suspend fun updateUserSubscription(subscriptionDto: SubscriptionDto, userId: String) =
        withContext(Dispatchers.IO) {
            return@withContext dataManager.updateUserSubscription(
                accessToken = dataManager.getApplicationAccessToken()?.accessToken!!,
                userUniqueId = userId,
                data = HashMap<String, Any>().apply {
                    set("google_play_token", subscriptionDto.googlePlayToken)
                }
            )
        }
}