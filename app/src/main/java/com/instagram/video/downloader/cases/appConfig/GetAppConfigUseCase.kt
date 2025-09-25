package com.instagram.video.downloader.cases.appConfig

import com.instagram.video.downloader.BuildConfig
import com.instagram.video.downloader.data.DataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class GetAppConfigUseCase @Inject constructor(
    private val dataManager: DataManager
) {

    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        return@withContext dataManager.getAppConfig(
            accessToken = dataManager.getApplicationAccessToken()?.accessToken!!,
            HashMap<String, Any>().apply {
                set("version_code", BuildConfig.VERSION_CODE)
            }
        )
    }

}