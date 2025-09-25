package com.instagram.video.downloader.cases.version

import com.instagram.video.downloader.BuildConfig
import com.instagram.video.downloader.data.DataManager
import com.instagram.video.downloader.data.remote.api.dto.Version
import javax.inject.Inject

class CheckCurrentVersion @Inject constructor(private val dataManager: DataManager) {
    suspend operator fun invoke(): Version {
        return dataManager.checkVersion(
            dataManager.getApplicationAccessToken()?.accessToken!!,
            HashMap<String, Any>().apply {
                set("version_code", BuildConfig.VERSION_CODE)
            })
    }
}