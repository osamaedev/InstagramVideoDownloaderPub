package com.instagram.video.downloader.cases.plans

import com.instagram.video.downloader.data.DataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class GetPlansUseCase @Inject constructor(private val dataManager: DataManager) {

    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        try {
            return@withContext dataManager.getPlans(
                dataManager.getApplicationAccessToken()?.accessToken!!
            )
        } catch (e: Exception) {
            Timber.tag("GetPlansUseCase").e(e)
            return@withContext arrayListOf()
        }
    }


}