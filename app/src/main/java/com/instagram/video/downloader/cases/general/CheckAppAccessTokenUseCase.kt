package com.instagram.video.downloader.cases.general

import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.common.Response
import com.instagram.video.downloader.data.DataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CheckAppAccessTokenUseCase @Inject constructor(
    private val dataManager: DataManager
) {

    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        if (dataManager.getApplicationAccessToken() == null
            || dataManager.getApplicationAccessToken()?.accessToken?.isEmpty() == true
            || !isTokenValid()
        ) {
            val accessToken = dataManager.getAppAccessToken(
                credentials = HashMap<String, Any>().apply {
                    set("grant_type", VideoDownloaderApp.grantType)
                    set("client_secret", VideoDownloaderApp.clientSecret)
                    set("client_id", VideoDownloaderApp.clientId)
                }
            )
            dataManager.setApplicationAccessToken(accessToken)
            dataManager.setApplicationAccessTokenIssuedAt(System.currentTimeMillis())
        }
        return@withContext true
    }

    private suspend fun getApplicationAccessToken() = withContext(Dispatchers.IO) {
        return@withContext dataManager.getAppAccessToken(
            credentials = HashMap<String, Any>().apply {
                set("grant_type", VideoDownloaderApp.grantType)
                set("client_secret", VideoDownloaderApp.clientSecret)
                set("client_id", VideoDownloaderApp.clientId)
            }
        )
    }


    private fun isTokenValid() =
        (System.currentTimeMillis() - dataManager.getApplicationAccessTokenIssuedAt()) <= (dataManager.getApplicationAccessToken()!!.expiresIn * 1000)


}