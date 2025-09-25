package com.instagram.video.downloader.data.local.room

import com.instagram.video.downloader.data.local.room.dao.PostToDownloadDao
import com.instagram.video.downloader.data.local.room.entities.Caption
import com.instagram.video.downloader.data.local.room.entities.Media
import com.instagram.video.downloader.data.local.room.entities.MediaToDownload
import com.instagram.video.downloader.data.local.room.entities.MediaUser
import com.instagram.video.downloader.data.local.room.entities.Post
import com.instagram.video.downloader.data.local.room.entities.PostToDownload
import com.instagram.video.downloader.data.local.room.entities.PostToDownloadWithMediaToDownload
import com.instagram.video.downloader.data.local.room.entities.PostWithMediaUserAndMedia
import com.instagram.video.downloader.data.local.room.entities.User
import javax.inject.Inject

class VideoDownloaderRoomImp @Inject constructor(
    private val videoDownloaderDatabase: VideoDownloaderDatabase,
) : IVideoDownloaderRoom {

    override suspend fun getPostWithAllByInstagramId(instagramId: String) =
        videoDownloaderDatabase.postDao().getPostWithAllByInstagramId(instagramId)

    override suspend fun setPostsToDefaultUser(loggedInUserId: Long) =
        videoDownloaderDatabase.postDao().setPostsToDefaultUser(loggedInUserId)

    override suspend fun setMediaUsersToDefaultUser(loggedInUserId: Long) =
        videoDownloaderDatabase.mediaUserDao().setMediaUsersToDefaultUser(loggedInUserId)

    override suspend fun getPostsToDownload() =
        videoDownloaderDatabase.postToDownloadDao().getPostsToDownload()

    override suspend fun getPostToDownloadById(postToDownloadId: Long) =
        videoDownloaderDatabase.postToDownloadDao().getPostToDownloadById(postToDownloadId)

    override suspend fun deletePostToDownload(postToDownloadId: Long) =
        videoDownloaderDatabase.postToDownloadDao().deletePostToDownload(postToDownloadId)

    override suspend fun insertPostToDownload(postToDownload: PostToDownload) =
        videoDownloaderDatabase.postToDownloadDao().insertPostToDownload(postToDownload)

    override suspend fun deletePost(post: Post) = videoDownloaderDatabase.postDao().deletePost(post)

    override suspend fun deleteMedia(media: Media) =
        videoDownloaderDatabase.mediaDao().deleteMedia(media)

    override suspend fun insertMediaToDownload(mediaToDownload: MediaToDownload) =
        videoDownloaderDatabase.mediaToDownloadDao().insertMediaToDownload(mediaToDownload)

    override suspend fun insertMediaToDownload(mediaToDownloads: List<MediaToDownload>) =
        videoDownloaderDatabase.mediaToDownloadDao().insertMediaToDownload(mediaToDownloads)

    override suspend fun deleteMediaToDownload(identifier: Long) =
        videoDownloaderDatabase.mediaToDownloadDao().deleteMediaToDownload(identifier)

    override suspend fun getPostByInstagramId(instagramId: String) =
        videoDownloaderDatabase.postDao().getPostByInstagramId(instagramId)

    override suspend fun getMediaUserByIdentifier(pkIdOrInstagramId: String): MediaUser? =
        videoDownloaderDatabase.mediaUserDao().getMediaUserByIdentifier(pkIdOrInstagramId)

    override suspend fun getDefaultUser() = videoDownloaderDatabase.userDao().getDefaultUser()

    override suspend fun insertCaption(caption: Caption) =
        videoDownloaderDatabase.captionDao().insertCaption(caption)

    override suspend fun insertMediaUser(mediaUser: MediaUser) =
        videoDownloaderDatabase.mediaUserDao().insertMediaUser(mediaUser)

    override suspend fun insertMedia(media: Media) =
        videoDownloaderDatabase.mediaDao().insertMedia(media)

    override suspend fun insertPost(post: Post) = videoDownloaderDatabase.postDao().insertPost(post)

    override suspend fun getUserPosts() =
        videoDownloaderDatabase.userDao().getUserPosts()

    override suspend fun getAllUsers() = videoDownloaderDatabase.userDao().getAllUsers()

    override suspend fun insertUser(user: User) = videoDownloaderDatabase.userDao().insertUser(user)

    override suspend fun updateUser(user: User) = videoDownloaderDatabase.userDao().updateUser(user)

    override suspend fun getUserByIdentifier(identifier: Long) =
        videoDownloaderDatabase.userDao().getUserByIdentifier(identifier)

    override suspend fun deleteUser(user: User) = videoDownloaderDatabase.userDao().deleteUser(user)
}