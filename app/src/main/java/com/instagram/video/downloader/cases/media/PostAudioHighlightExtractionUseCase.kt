package com.instagram.video.downloader.cases.media

import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.cases.user.LoggedInUser
import com.instagram.video.downloader.common.Response
import com.instagram.video.downloader.common.extractCsrfFromCookies
import com.instagram.video.downloader.common.extractPostCode
import com.instagram.video.downloader.common.extractReelCode
import com.instagram.video.downloader.common.extractStoryId
import com.instagram.video.downloader.common.getId
import com.instagram.video.downloader.common.isPostLink
import com.instagram.video.downloader.common.isReelLink
import com.instagram.video.downloader.common.isStoryLink
import com.instagram.video.downloader.data.DataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.HttpException
import com.instagram.video.downloader.R
import com.instagram.video.downloader.common.extractHighlightId
import com.instagram.video.downloader.common.extractHighlightIdPhoneShareLink
import com.instagram.video.downloader.common.isHighlightShareLink
import timber.log.Timber
import javax.inject.Inject

class PostAudioHighlightExtractionUseCase @Inject constructor(private val dataManager: DataManager) {

    operator fun invoke(
        userUniqueId: String,
        mediaUrl: String,
        isHighlight: Boolean,
        isAudio: Boolean = false,
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

            val query = HashMap<String, Any>().apply {
                set("__a", "1")
                set("__d", "dis")
            }
            when {
                isHighlight -> {
                    val highlightId =
                        if (isHighlightShareLink(mediaUrl)) extractHighlightIdPhoneShareLink(url = mediaUrl)
                        else extractHighlightId(
                            mediaUrl
                        )
                    val highlightMedia = dataManager.extractHighlight(
                        hashCode = "de8017ee0a7c9c45ec4260733d81ea31",
                        headers = headers,
                        data = HashMap<String, Any>().apply {
                            set("reel_ids", arrayOf<Any>())
                            set("tag_names", arrayOf<Any>())
                            set("location_ids", arrayOf<Any>())
                            set(
                                "highlight_reel_ids",
                                arrayOf(highlightId)
                            )
                            set("precomposed_overlay", false)
                            set("show_story_viewer_list", true)
                            set("story_viewer_fetch_count", 50)
                            set("story_viewer_cursor", "")
                        }
                    )
                    emit(Response.Success(highlightMedia))
                }

                isAudio -> {
                    val mediaData = dataManager.getLoggedInUserAudioInfo(
                        userUniqueId = userUniqueId,
                        accessToken = dataManager.getApplicationAccessToken()?.accessToken!!,
                        data = HashMap<String, Any>().apply {
                            set("url", mediaUrl)
                            set("android_id", getId(VideoDownloaderApp.application))
                        }
                    )
                    emit(Response.Success(mediaData))
                }

                else -> {
                    when {
                        isReelLink(mediaUrl) -> {
                            val reelCode = extractReelCode(mediaUrl)
                            if (reelCode.isNotBlank()) {
                                val mediaData = dataManager.extractReel(
                                    postCode = reelCode,
                                    query = query,
                                    headers = headers,
                                )

                                if (mediaData.graphql != null) {
                                    kotlinx.coroutines.delay(1500)
                                    val newMediaInfo = dataManager.extractMediaById(
                                        storyId = mediaData.graphql.shortcodeMedia?.id!!,
                                        query = HashMap(),
                                        headers = headers,
                                    )
                                    emit(Response.Success(newMediaInfo.items.first()))
                                    return@flow
                                }

                                emit(Response.Success(mediaData.items.first()))
                            } else {
                                Timber.tag("PostExtraction").e("The postCode (reel) is blank")
                            }
                        }

                        isPostLink(mediaUrl) -> {
                            val postCode = extractPostCode(mediaUrl)
                            if (postCode.isNotBlank()) {
                                val mediaData = dataManager.extractPost(
                                    postCode = postCode,
                                    query = query,
                                    headers = headers,
                                )

                                if (mediaData.graphql != null) {
                                    kotlinx.coroutines.delay(1500)
                                    val newMediaInfo = dataManager.extractMediaById(
                                        storyId = mediaData.graphql.shortcodeMedia?.id!!,
                                        query = HashMap(),
                                        headers = headers,
                                    )
                                    emit(Response.Success(newMediaInfo.items.first()))
                                    return@flow
                                }
                                emit(Response.Success(mediaData.items.first()))
                            } else {
                                Timber.tag("PostExtraction").e("The postCode is blank")
                            }
                        }

                        isStoryLink(mediaUrl) -> {
                            extractStoryId(mediaUrl).let { storyId ->
                                if (storyId.isNotBlank()) {
                                    val mediaData = dataManager.extractMediaById(
                                        storyId = storyId,
                                        query = HashMap(),
                                        headers = headers,
                                    )
                                    emit(Response.Success(mediaData.items.first()))
                                } else {
                                    emit(Response.Error("Something went wrong"))
                                }
                            }
                        }

                        else -> {
                            emit(Response.Error("Unknown media type"))
                        }
                    }
                }

            }
        } catch (e: HttpException) {
            Timber.tag("PostExtractionUseCase").e(e)
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
            Timber.tag("PostExtractionUseCase").e(e)
            emit(Response.Error(e.message))
        }
    }.flowOn(Dispatchers.IO)

}