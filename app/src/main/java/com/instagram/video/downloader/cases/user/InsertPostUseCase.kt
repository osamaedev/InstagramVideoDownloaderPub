package com.instagram.video.downloader.cases.user

import android.os.Environment
import android.webkit.MimeTypeMap
import com.instagram.video.downloader.common.getExtension
import com.instagram.video.downloader.data.DataManager
import com.instagram.video.downloader.data.local.room.entities.Caption
import com.instagram.video.downloader.data.local.room.entities.Media
import com.instagram.video.downloader.data.local.room.entities.MediaUser
import com.instagram.video.downloader.data.local.room.entities.Post
import com.instagram.video.downloader.data.local.room.entities.User
import com.instagram.video.downloader.data.remote.api.dto.AudioResponse
import com.instagram.video.downloader.data.remote.api.dto.HighlightData
import com.instagram.video.downloader.data.remote.api.dto.HighlightMedia
import com.instagram.video.downloader.data.remote.api.dto.PostResponse
import com.instagram.video.downloader.data.remote.instaApi.dto.collections.CollectionMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import com.instagram.video.downloader.data.remote.instaApi.dto.explore.Media as PostMassDownloadResponse

class InsertPostUseCase @Inject constructor(private val dataManager: DataManager) {

    suspend fun insertPostAndMedia(postResponse: PostResponse, currentLoggedInUser: LoggedInUser) =
        withContext(Dispatchers.IO) {
            try {
                if (currentLoggedInUser.id == -1L && dataManager.getDefaultUser() == null) {
                    dataManager.insertUser(
                        User(
                            identifier = -1L,
                            fullName = "default",
                            biography = "",
                            username = "default",
                            profilePicUrl = "",
                            instagramId = "",
                            cookies = "",
                            loggedInAt = System.currentTimeMillis(),
                            cookiesExpiringAt = System.currentTimeMillis(),
                            createAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                        )
                    )
                }

                val mediaUser = dataManager.getMediaUserByIdentifier(
                    postResponse.user?.pk?.toString() ?: postResponse.user?.id!!
                )

                val mediaUserId = mediaUser?.identifier
                    ?: dataManager.insertMediaUser(
                        MediaUser(
                            fullName = postResponse.user?.fullName!!,
                            isPrivate = postResponse.user.isPrivate == true,
                            pkId = postResponse.user.pk?.toString() ?: postResponse.user.id!!,
                            fbIdV2 = postResponse.user.fbidV2?.toString()!!,
                            accountType = postResponse.user.accountType ?: 2,
                            username = postResponse.user.username!!,
                            profilePicUrl = postResponse.user.profilePicUrl!!,
                            profilePicId = postResponse.user.profilePicId!!,
                            createAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                            userId = currentLoggedInUser.id,
                        )
                    )

                val existingPost = dataManager.getPostByInstagramId(postResponse.id!!)

                val postId = existingPost?.identifier
                    ?: dataManager.insertPost(
                        Post(
                            mediaId = postResponse.id,
                            productType = postResponse.productType!!,
                            mediaType = postResponse.mediaType!!,
                            code = postResponse.code!!,
                            takenAt = postResponse.takenAt!!,
                            commentCount = postResponse.metrics?.commentCount ?: 0L,
                            likeCount = if (postResponse.metrics?.likeCount != null) postResponse.metrics.likeCount else postResponse.likeCount
                                ?: 0L,
                            thumbnailUrl = postResponse.thumbnailUrl
                                ?: postResponse.imageVersions?.items?.first()?.url!!,
                            userId = currentLoggedInUser.id,
                            mediaUserId = mediaUserId,
                            createAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                        )
                    )

                when (postResponse.mediaType) {
                    8 -> {
                        postResponse.carouselMedia?.forEachIndexed { _, carouselMediaItem ->
                            val mediaUrl =
                                if (carouselMediaItem?.mediaType == 1) carouselMediaItem.imageVersions?.items?.first()?.url!! else carouselMediaItem?.videoVersions?.first()?.url!!


                            val fileLocation =
                                if (carouselMediaItem.mediaType == 1) Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES
                                ) else Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)

                            dataManager.insertMedia(
                                Media(
                                    instagramId = carouselMediaItem.id!!,
                                    type = if (carouselMediaItem.mediaType == 1) "image" else "video",
                                    displayUrl = mediaUrl,
                                    thumbnailUrl = carouselMediaItem.imageVersions?.items?.first()?.url!!,
                                    fileLocation = "${fileLocation}/${currentLoggedInUser.username}/${carouselMediaItem.id}.${
                                        getExtension(
                                            mediaUrl
                                        )
                                    }",
                                    postId = postId,
                                    createAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis(),
                                )
                            )
                        }
                    }

                    1 -> {
                        val mediaUrl = postResponse.imageVersions?.items?.first()?.url!!
                        dataManager.insertMedia(
                            Media(
                                instagramId = postResponse.id,
                                type = "image",
                                displayUrl = mediaUrl,
                                thumbnailUrl = postResponse.thumbnailUrl
                                    ?: postResponse.imageVersions.items.first()?.url!!,
                                fileLocation = "${
                                    Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_PICTURES
                                    )
                                }/${currentLoggedInUser.username}/${postResponse.id}.${
                                    getExtension(
                                        mediaUrl
                                    )
                                }",
                                postId = postId,
                                createAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                            )
                        )
                    }

                    2 -> {
                        val mediaUrl = postResponse.videoVersions?.first()?.url!!
                        dataManager.insertMedia(
                            Media(
                                instagramId = postResponse.id,
                                type = "video",
                                displayUrl = mediaUrl,
                                thumbnailUrl = postResponse.thumbnailUrl
                                    ?: postResponse.imageVersions?.items?.first()?.url!!,
                                fileLocation = "${
                                    Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_MOVIES
                                    )
                                }/${currentLoggedInUser.username}/${postResponse.id}.${
                                    getExtension(
                                        mediaUrl
                                    )
                                }",
                                postId = postId,
                                createAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                            )
                        )
                    }
                }

                if (postResponse.caption != null) {
                    dataManager.insertCaption(
                        Caption(
                            pk = postResponse.caption.pk!!,
                            postId = postId,
                            text = postResponse.caption.text ?: "",
                            createAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                        )
                    )
                }
                return@withContext
            } catch (e: Exception) {
                Timber.tag("InsertPostUseCase").e(e)
                return@withContext
            }
        }

    suspend fun insertHighlight(
        highlightData: HighlightData,
        downloadedMedia: List<HighlightMedia>,
        currentLoggedInUser: LoggedInUser
    ) = withContext(Dispatchers.IO) {
        try {
            if (currentLoggedInUser.id == -1L && dataManager.getDefaultUser() == null) {
                dataManager.insertUser(
                    User(
                        identifier = -1L,
                        fullName = "default",
                        biography = "",
                        username = "default",
                        profilePicUrl = "",
                        instagramId = "",
                        cookies = "",
                        loggedInAt = System.currentTimeMillis(),
                        cookiesExpiringAt = System.currentTimeMillis(),
                        createAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    )
                )
            }

            val mediaUser = dataManager.getMediaUserByIdentifier(
                highlightData.user?.pk?.toString() ?: highlightData.user?.id!!
            )

            val mediaUserId = mediaUser?.identifier
                ?: dataManager.insertMediaUser(
                    MediaUser(
                        fullName = highlightData.user?.fullName!!,
                        isPrivate = highlightData.user.isPrivate == true,
                        pkId = highlightData.user.pk?.toString()
                            ?: highlightData.user.id!!,
                        fbIdV2 = highlightData.user.id!!,
                        accountType = highlightData.user.accountType ?: 2,
                        username = highlightData.user.username!!,
                        profilePicUrl = highlightData.user.profilePicUrl!!,
                        profilePicId = highlightData.user.profilePicId!!,
                        createAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        userId = currentLoggedInUser.id,
                    )
                )


            val postId = dataManager.insertPost(
                Post(
                    mediaId = highlightData.id!!,
                    productType = highlightData.reelType!!,                 // post type is: highlight_reel
                    mediaType = downloadedMedia.first().mediaType!!,        // it's an highlight so the type is not required here
                    code = highlightData.id,
                    takenAt = highlightData.createdAt!!,
                    commentCount = -1,
                    likeCount = -1,
                    thumbnailUrl = highlightData.coverMedia?.croppedImageVersion?.url!!,
                    userId = currentLoggedInUser.id,
                    mediaUserId = mediaUserId,
                    createAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                )
            )


            downloadedMedia.forEach { media ->
                when (media.mediaType) {
                    1 -> {
                        val mediaUrl = media.imageVersions?.items?.first()?.url!!
                        dataManager.insertMedia(
                            Media(
                                instagramId = media.id!!,
                                type = "image",
                                displayUrl = mediaUrl,
                                thumbnailUrl = media.thumbnailUrl!!,
                                fileLocation = "${
                                    Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_PICTURES
                                    )
                                }/${currentLoggedInUser.username}/${media.id}.${
                                    getExtension(
                                        mediaUrl
                                    )
                                }",
                                postId = postId,
                                createAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                            )
                        )
                    }

                    2 -> {
                        val mediaUrl = media.videoVersions?.first()?.url!!
                        dataManager.insertMedia(
                            Media(
                                instagramId = media.id!!,
                                type = "video",
                                displayUrl = mediaUrl,
                                thumbnailUrl = media.thumbnailUrl!!,
                                fileLocation = "${
                                    Environment.getExternalStoragePublicDirectory(
                                        Environment.DIRECTORY_MOVIES
                                    )
                                }/${currentLoggedInUser.username}/${media.id}.${
                                    getExtension(
                                        mediaUrl
                                    )
                                }",
                                postId = postId,
                                createAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                            )
                        )
                    }
                }
            }

            return@withContext
        } catch (e: Exception) {
            Timber.tag("insertHighlight").e(e)
            return@withContext
        }
    }

    suspend fun insertAudio(audioData: AudioResponse, currentLoggedInUser: LoggedInUser) =
        withContext(Dispatchers.IO) {
            try {
                if (currentLoggedInUser.id == -1L && dataManager.getDefaultUser() == null) {
                    dataManager.insertUser(
                        User(
                            identifier = -1L,
                            fullName = "default",
                            biography = "",
                            username = "default",
                            profilePicUrl = "",
                            instagramId = "",
                            cookies = "",
                            loggedInAt = System.currentTimeMillis(),
                            cookiesExpiringAt = System.currentTimeMillis(),
                            createAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                        )
                    )
                }


                val mediaUser = if (audioData.artist?.id == null) {
                    null
                } else dataManager.getMediaUserByIdentifier(
                    audioData.artist.id
                )

                val mediaUserId = mediaUser?.identifier ?: dataManager.insertMediaUser(
                    MediaUser(
                        fullName = if (audioData.artist != null) {
                            audioData.artist.fullName!!
                        } else "unknown",
                        isPrivate = if (audioData.artist == null) false else audioData.artist.isPrivate!!,
                        pkId = if (audioData.artist != null) audioData.artist.id.toString() else "",
                        fbIdV2 = if (audioData.artist != null) audioData.artist.id.toString() else "",
                        accountType = -1,
                        username = if (audioData.artist != null) audioData.artist.username!! else "unknown",
                        profilePicUrl = if (audioData.artist != null) audioData.artist.profilePicUrl!! else "",
                        profilePicId = if (audioData.artist != null) audioData.artist.profilePicId!! else "",
                        createAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        userId = currentLoggedInUser.id,
                    )
                )

                val postId = dataManager.insertPost(
                    Post(
                        mediaId = audioData.audioId,
                        productType = "audio",
                        mediaType = -1,
                        code = audioData.audioId,                   // no code for the audios
                        takenAt = audioData.timeCreated ?: 0L,
                        commentCount = -1,
                        likeCount = -1,
                        thumbnailUrl = if (audioData.artist != null) audioData.artist.profilePicUrl!! else "",
                        userId = currentLoggedInUser.id,
                        mediaUserId = mediaUserId,
                        createAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    )
                )

                val mediaUrl = audioData.downloadUrl!!
                dataManager.insertMedia(
                    Media(
                        instagramId = audioData.audioId,
                        type = "music",
                        displayUrl = mediaUrl,
                        thumbnailUrl = if (audioData.artist != null) audioData.artist.profilePicUrl!! else "",
                        fileLocation = "${
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_MUSIC
                            )
                        }/${currentLoggedInUser.username}/${audioData.audioId}.${
                            getExtension(
                                mediaUrl
                            )
                        }",
                        postId = postId,
                        createAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    )
                )
                return@withContext
            } catch (e: Exception) {
                Timber.tag("insertAudio").e(e)
            }
        }

    suspend fun insertExtractedExplorePostAndMedia(
        postResponse: PostMassDownloadResponse,
        currentLoggedInUser: LoggedInUser
    ) = withContext(
        Dispatchers.IO
    ) {
        try {
            if (currentLoggedInUser.id == -1L && dataManager.getDefaultUser() == null) {
                dataManager.insertUser(
                    User(
                        identifier = -1L,
                        fullName = "default",
                        biography = "",
                        username = "default",
                        profilePicUrl = "",
                        instagramId = "",
                        cookies = "",
                        loggedInAt = System.currentTimeMillis(),
                        cookiesExpiringAt = System.currentTimeMillis(),
                        createAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    )
                )
            }

            val mediaUser = dataManager.getMediaUserByIdentifier(
                postResponse.user?.pk ?: postResponse.user?.id!!
            )

            val mediaUserId = mediaUser?.identifier
                ?: dataManager.insertMediaUser(
                    MediaUser(
                        fullName = postResponse.user?.fullName!!,
                        isPrivate = postResponse.user.isPrivate == true,
                        pkId = postResponse.user.pk ?: postResponse.user.id!!,
                        fbIdV2 = postResponse.user.fbidV2!!,
                        accountType = postResponse.user.accountType ?: 2,
                        username = postResponse.user.username!!,
                        profilePicUrl = postResponse.user.profilePicUrl!!,
                        profilePicId = postResponse.user.profilePicId!!,
                        createAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        userId = currentLoggedInUser.id,
                    )
                )

            val existingPost = dataManager.getPostByInstagramId(postResponse.id!!)

            val postId = existingPost?.identifier
                ?: dataManager.insertPost(
                    Post(
                        mediaId = postResponse.id,
                        productType = postResponse.productType!!,
                        mediaType = postResponse.mediaType!!,
                        code = postResponse.code!!,
                        takenAt = postResponse.takenAt!!,
                        commentCount = postResponse.commentCount ?: 0L,
                        likeCount = postResponse.likeCount ?: 0L,
                        thumbnailUrl = postResponse.imageVersions2?.candidates?.first()?.url!!,
                        userId = currentLoggedInUser.id,
                        mediaUserId = mediaUserId,
                        createAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    )
                )

            when (postResponse.mediaType) {
                8 -> {
                    postResponse.carouselMedia?.forEachIndexed { _, carouselMediaItem ->
                        val mediaUrl =
                            if (carouselMediaItem?.mediaType == 1) carouselMediaItem.imageVersions2?.candidates?.first()?.url!!
                            else carouselMediaItem?.videoVersions?.first()?.url!!


                        val fileLocation =
                            if (carouselMediaItem.mediaType == 1) Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES
                            ) else Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)

                        dataManager.insertMedia(
                            Media(
                                instagramId = carouselMediaItem.id!!,
                                type = if (carouselMediaItem.mediaType == 1) "image" else "video",
                                displayUrl = mediaUrl,
                                thumbnailUrl = carouselMediaItem.imageVersions2?.candidates?.first()?.url!!,
                                fileLocation = "${fileLocation}/${currentLoggedInUser.username}/${carouselMediaItem.id}.${
                                    getExtension(
                                        mediaUrl
                                    )
                                }",
                                postId = postId,
                                createAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                            )
                        )
                    }
                }

                1 -> {
                    val mediaUrl = postResponse.imageVersions2?.candidates?.first()?.url!!
                    dataManager.insertMedia(
                        Media(
                            instagramId = postResponse.id,
                            type = "image",
                            displayUrl = mediaUrl,
                            thumbnailUrl = postResponse.imageVersions2.candidates.first()?.url!!,
                            fileLocation = "${
                                Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES
                                )
                            }/${currentLoggedInUser.username}/${postResponse.id}.${
                                getExtension(
                                    mediaUrl
                                )
                            }",
                            postId = postId,
                            createAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                        )
                    )
                }

                2 -> {
                    val mediaUrl = postResponse.videoVersions?.first()?.url!!
                    dataManager.insertMedia(
                        Media(
                            instagramId = postResponse.id,
                            type = "video",
                            displayUrl = mediaUrl,
                            thumbnailUrl = postResponse.imageVersions2?.candidates?.first()?.url!!,
                            fileLocation = "${
                                Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_MOVIES
                                )
                            }/${currentLoggedInUser.username}/${postResponse.id}.${
                                getExtension(
                                    mediaUrl
                                )
                            }",
                            postId = postId,
                            createAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                        )
                    )
                }
            }

            if (postResponse.caption != null) {
                dataManager.insertCaption(
                    Caption(
                        pk = postResponse.caption.pk!!,
                        postId = postId,
                        text = postResponse.caption.text ?: "",
                        createAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    )
                )
            }
            return@withContext

        } catch (e: Exception) {

            // TODO: Important here to record the exception in firebase

            Timber.tag("InsertMassPostUseCase").e(e)
            return@withContext
        }
    }


    suspend fun insertExtractedCollectionPostAndMedia(
        postResponse: CollectionMedia,
        currentLoggedInUser: LoggedInUser,
    ) = withContext(Dispatchers.IO) {
        try {
            if (currentLoggedInUser.id == -1L && dataManager.getDefaultUser() == null) {
                dataManager.insertUser(
                    User(
                        identifier = -1L,
                        fullName = "default",
                        biography = "",
                        username = "default",
                        profilePicUrl = "",
                        instagramId = "",
                        cookies = "",
                        loggedInAt = System.currentTimeMillis(),
                        cookiesExpiringAt = System.currentTimeMillis(),
                        createAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    )
                )
            }

            val mediaUser = dataManager.getMediaUserByIdentifier(
                postResponse.user?.pk ?: postResponse.user?.id!!
            )

            val mediaUserId = mediaUser?.identifier
                ?: dataManager.insertMediaUser(
                    MediaUser(
                        fullName = postResponse.user?.fullName!!,
                        isPrivate = postResponse.user.isPrivate == true,
                        pkId = postResponse.user.pk ?: postResponse.user.id!!,
                        fbIdV2 = postResponse.user.fbidV2!!,
                        accountType = postResponse.user.accountType ?: 2,
                        username = postResponse.user.username!!,
                        profilePicUrl = postResponse.user.profilePicUrl!!,
                        profilePicId = postResponse.user.profilePicId!!,
                        createAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                        userId = currentLoggedInUser.id,
                    )
                )

            val existingPost = dataManager.getPostByInstagramId(postResponse.id!!)

            val postId = existingPost?.identifier
                ?: dataManager.insertPost(
                    Post(
                        mediaId = postResponse.id,
                        productType = postResponse.productType!!,
                        mediaType = postResponse.mediaType!!,
                        code = postResponse.code!!,
                        takenAt = postResponse.takenAt!!,
                        commentCount = 0L,          // saved items does not have comment count
                        likeCount = postResponse.likeCount ?: 0L,
                        thumbnailUrl = postResponse.imageVersions2?.candidates?.first()?.url!!,
                        userId = currentLoggedInUser.id,
                        mediaUserId = mediaUserId,
                        createAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    )
                )

            when (postResponse.mediaType) {
                8 -> {
                    postResponse.carouselMedias?.forEachIndexed { _, carouselMediaItem ->
                        val mediaUrl =
                            if (carouselMediaItem.mediaType == 1) carouselMediaItem.imageVersions2?.candidates?.first()?.url!!
                            else carouselMediaItem.videoVersions?.first()?.url!!


                        val fileLocation =
                            if (carouselMediaItem.mediaType == 1) Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES
                            ) else Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)

                        dataManager.insertMedia(
                            Media(
                                instagramId = carouselMediaItem.id!!,
                                type = if (carouselMediaItem.mediaType == 1) "image" else "video",
                                displayUrl = mediaUrl,
                                thumbnailUrl = carouselMediaItem.imageVersions2?.candidates?.first()?.url!!,
                                fileLocation = "${fileLocation}/${currentLoggedInUser.username}/${carouselMediaItem.id}.${
                                    getExtension(
                                        mediaUrl
                                    )
                                }",
                                postId = postId,
                                createAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                            )
                        )
                    }
                }

                1 -> {
                    val mediaUrl = postResponse.imageVersions2?.candidates?.first()?.url!!
                    dataManager.insertMedia(
                        Media(
                            instagramId = postResponse.id,
                            type = "image",
                            displayUrl = mediaUrl,
                            thumbnailUrl = postResponse.imageVersions2.candidates.first()?.url!!,
                            fileLocation = "${
                                Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_PICTURES
                                )
                            }/${currentLoggedInUser.username}/${postResponse.id}.${
                                getExtension(
                                    mediaUrl
                                )
                            }",
                            postId = postId,
                            createAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                        )
                    )
                }

                2 -> {
                    val mediaUrl = postResponse.videoVersions?.first()?.url!!
                    dataManager.insertMedia(
                        Media(
                            instagramId = postResponse.id,
                            type = "video",
                            displayUrl = mediaUrl,
                            thumbnailUrl = postResponse.imageVersions2?.candidates?.first()?.url!!,
                            fileLocation = "${
                                Environment.getExternalStoragePublicDirectory(
                                    Environment.DIRECTORY_MOVIES
                                )
                            }/${currentLoggedInUser.username}/${postResponse.id}.${
                                getExtension(
                                    mediaUrl
                                )
                            }",
                            postId = postId,
                            createAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis(),
                        )
                    )
                }
            }

            if (postResponse.caption != null) {
                dataManager.insertCaption(
                    Caption(
                        pk = postResponse.caption.pk!!,
                        postId = postId,
                        text = postResponse.caption.text ?: "",
                        createAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis(),
                    )
                )
            }

            return@withContext

        } catch (e: Exception) {
            Timber.tag("insertCollectionMass").e(e)
        }
    }
}