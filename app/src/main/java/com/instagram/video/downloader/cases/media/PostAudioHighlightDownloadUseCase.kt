package com.instagram.video.downloader.cases.media

import com.google.gson.Gson
import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.common.Response
import com.instagram.video.downloader.common.getId
import com.instagram.video.downloader.data.DataManager
import com.instagram.video.downloader.data.remote.utils.GeneralErrorResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class PostAudioHighlightDownloadUseCase @Inject constructor(private val dataManager: DataManager) {

    fun invoke(
        userUniqueId: String,
        mediaUrl: String,
        isHighlight: Boolean,
        isAfterReview: Boolean,
        isAfterRewarded: Boolean,
        isAudio: Boolean = false,
    ) = flow {
        try {
            emit(Response.Loading())
            when {
                isHighlight -> {
                    val highlightMedia = dataManager.getHighlightInfo(
                        userUniqueId,
                        accessToken = dataManager.getApplicationAccessToken()?.accessToken!!,
                        data = HashMap<String, Any>().apply {
                            set("android_id", getId(VideoDownloaderApp.application))
                            set("is_after_review", if (isAfterReview) "1" else "0")
                            set("is_after_rewarded_ad", if (isAfterRewarded) "1" else "0")
                            set("url_id_code", mediaUrl)
                        }
                    )

                    emit(Response.Success(data = highlightMedia))
                }

                isAudio -> {
                    val audioData = dataManager.getAudioInfo(
                        userUniqueId,
                        accessToken = dataManager.getApplicationAccessToken()?.accessToken!!,
                        data = HashMap<String, Any>().apply {
                            set("android_id", getId(VideoDownloaderApp.application))
                            set("is_after_review", if (isAfterReview) "1" else "0")
                            set("is_after_rewarded_ad", if (isAfterRewarded) "1" else "0")
                            set("url_id_code", mediaUrl)
                        }
                    )
                    emit(Response.Success(data = audioData))
                }

                else -> {
                    val mediaInfo = dataManager.getMediaInfo(
                        userUniqueId,
                        accessToken = dataManager.getApplicationAccessToken()?.accessToken!!,
                        data = HashMap<String, Any>().apply {
                            set("android_id", getId(VideoDownloaderApp.application))
                            set("is_after_review", if (isAfterReview) "1" else "0")
                            set("is_after_rewarded_ad", if (isAfterRewarded) "1" else "0")
                            set("url_id_code", mediaUrl)
                        }
                    )
                    emit(Response.Success(data = mediaInfo))
                }
            }

        } catch (e: HttpException) {
            if (e.code() == 403 || e.code() == 404) {
                val response = e.response()?.errorBody()?.string()
                Timber.tag(PostAudioHighlightDownloadUseCase::class.java.simpleName)
                    .d("Error code .. %s", e.code())
                val generalErrorResponse =
                    Gson().fromJson(response, GeneralErrorResponse::class.java)
                if (generalErrorResponse != null)
                    emit(Response.Error(response, generalErrorResponse))
            } else {
                emit(Response.Error(e.message))
            }
        } catch (e: Exception) {
            emit(Response.Error(e.message))
        }
    }.flowOn(Dispatchers.IO)
}