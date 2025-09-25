package com.instagram.video.downloader.cases.media

import com.google.gson.Gson
import com.instagram.video.downloader.R
import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.cases.user.LoggedInUser
import com.instagram.video.downloader.common.Response
import com.instagram.video.downloader.common.extractCsrfFromCookies
import com.instagram.video.downloader.common.getId
import com.instagram.video.downloader.data.DataManager
import com.instagram.video.downloader.data.remote.utils.GeneralErrorResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class DownloadPostByIdUseCase @Inject constructor(private val dataManager: DataManager) {


    /***
     *  This is for logged in user
     */
    suspend operator fun invoke(
        mediaId: String,
        currentLoggedInUser: LoggedInUser,
    ) = flow {
        try {
            emit(Response.Loading())
            val headers = HashMap<String, Any>().apply {
                set(
                    "User-Agent",
                    VideoDownloaderApp.application.resources.getString(R.string.web_agent)
                )
                set("Cookie", currentLoggedInUser.cookies)
                set("Accept", "*/*")
                set("Accept-Encoding", "*/*")
                set("Content-Type", "application/json")
                set("Accept-Language", "en-US,en;q=0.9,fr-FR;q=0.8,fr;q=0.7")
                set("DNT", "1")
                set("Referer", "https://www.instagram.com/")
                set("authority", "www.instagram.com/")
                set("x-csrftoken", extractCsrfFromCookies(currentLoggedInUser.cookies))

                set("x-ig-app-id", "1217981644879628")
            }

            val response = dataManager.extractMediaById(
                mediaId,
                HashMap(),
                headers
            )

            emit(Response.Success(response.items.first()))

        } catch (e: HttpException) {
            Timber.tag("DownloadPostByIdUseCase").e(e)
            when {
                e.code() == 401 || e.code() == 403 -> {
                    // here send the user that he made too much request so re-login
                    emit(Response.Error("Wait some minutes or re-Login is required"))
                }

                e.code() == 400 -> {
                    emit(Response.Error("Could not retrieve the media info"))
                }

                else -> emit(Response.Error("Something went wrong"))
            }
        } catch (e: Exception) {
            Timber.tag("DownloadPostByIdUseCase").e(e)
            emit(Response.Error(e.message))
        }
    }.flowOn(Dispatchers.IO)


    suspend fun getPostInfoById(
        userUniqueId: String,
        isAfterReview: Boolean,
        isAfterRewarded: Boolean,
        postId: String
    ) = flow {
        try {
            emit(Response.Loading())

            val mediaInfo = dataManager.getMediaInfo(
                userUniqueId,
                accessToken = dataManager.getApplicationAccessToken()?.accessToken!!,
                data = HashMap<String, Any>().apply {
                    set("android_id", getId(VideoDownloaderApp.application))
                    set("is_after_review", if (isAfterReview) "1" else "0")
                    set("is_after_rewarded_ad", if (isAfterRewarded) "1" else "0")
                    set("url_id_code", postId)
                }
            )
            emit(Response.Success(data = mediaInfo))

        } catch (e: HttpException) {
            if (e.code() == 403 || e.code() == 404) {
                val response = e.response()?.errorBody()?.string()
                Timber.tag("DownloadPostById")
                    .d("Error code .. %s", e.code())
                val generalErrorResponse =
                    Gson().fromJson(response, GeneralErrorResponse::class.java)
                if (generalErrorResponse != null)
                    emit(Response.Error(response, generalErrorResponse))
            } else {
                emit(Response.Error(e.message))
            }
        } catch (e: Exception) {
            Timber.tag("DownloadPostByIdCase").e(e)
        }
    }.flowOn(Dispatchers.IO)

}