package com.instagram.video.downloader.cases.user

import com.google.gson.Gson
import com.instagram.video.downloader.BuildConfig
import com.instagram.video.downloader.data.DataManager
import com.instagram.video.downloader.data.remote.utils.GeneralErrorResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class SingUpOrGetRemoteUser @Inject constructor(private val dataManager: DataManager) {
    suspend operator fun invoke(androidId: String) =
        withContext(Dispatchers.IO) {
            try {
                val user = dataManager.findUser(
                    dataManager.getApplicationAccessToken()?.accessToken!!,
                    HashMap<String, Any>().apply {
                        set("android_id", androidId)
                        set("version_code", BuildConfig.VERSION_CODE)
                    }
                )
                if (user.subscription != null) {
                    dataManager.setCurrentUserPlanTag(user.subscription!!.plan.tagName)
                    dataManager.setSubscriptionStartedAt(user.subscription!!.startTime)
                    dataManager.setSubscriptionExpireAt(user.subscription!!.expiryTime)
                }
                return@withContext user
            } catch (e: HttpException) {
                val response = e.response()?.errorBody()?.string()
                if (e.code() == 404) {
                    val generalError = Gson().fromJson(response, GeneralErrorResponse::class.java)
                    if (generalError.error == "User not found") {
                        val user = dataManager.signUp(
                            accessToken = dataManager.getApplicationAccessToken()?.accessToken!!,
                            data = HashMap<String, Any>().apply {
                                set("android_id", androidId)
                                set("version_code", BuildConfig.VERSION_CODE)
                            }
                        )
                        Timber.tag("RegisterRemoteUser").i("Remote User inserted successfully")
                        return@withContext user
                    }
                }
                return@withContext null
            } catch (e: Exception) {
                Timber.tag("RegisterRemoteUser").e(e)
                return@withContext null
            }
        }
}