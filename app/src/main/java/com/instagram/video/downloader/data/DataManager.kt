package com.instagram.video.downloader.data

import com.instagram.video.downloader.cases.user.LoggedInUser
import com.instagram.video.downloader.data.local.prefs.IPrefsHelper
import com.instagram.video.downloader.data.local.room.IVideoDownloaderRoom
import com.instagram.video.downloader.data.local.room.entities.Caption
import com.instagram.video.downloader.data.local.room.entities.Media
import com.instagram.video.downloader.data.local.room.entities.MediaToDownload
import com.instagram.video.downloader.data.local.room.entities.MediaUser
import com.instagram.video.downloader.data.local.room.entities.Post
import com.instagram.video.downloader.data.local.room.entities.PostToDownload
import com.instagram.video.downloader.data.local.room.entities.PostWithMediaUserAndMedia
import com.instagram.video.downloader.data.local.room.entities.User
import com.instagram.video.downloader.data.remote.api.IVideoDownloaderApi
import com.instagram.video.downloader.data.remote.api.dto.AccessTokenResponse
import com.instagram.video.downloader.data.remote.api.dto.SubscriptionDtoExtended
import com.instagram.video.downloader.data.remote.instaApi.IInstaApi
import javax.inject.Inject

class DataManager @Inject constructor(
    private val prefsHelper: IPrefsHelper,
    private val videoDownloaderRoomImp: IVideoDownloaderRoom,
    private val videoDownloaderApi: IVideoDownloaderApi,
    private val instaApi: IInstaApi
) : IDataManager {

    // region DataManger

    override fun signOut() {
        this.signOutUser()
        // Add other clears here
    }

    // endregion

    // region PrefsHelper

    override fun setExploreFakeDragShown(isShown: Boolean) =
        prefsHelper.setExploreFakeDragShown(isShown)

    override fun getExploreFakeDragShown() = prefsHelper.getExploreFakeDragShown()

    override fun setCollectionFakeDragShown(isShown: Boolean) =
        prefsHelper.setCollectionFakeDragShown(isShown)

    override fun getCollectionFakeDragShown() = prefsHelper.getCollectionFakeDragShown()

    override fun setDownloadedFakeDragShown(isShown: Boolean) =
        prefsHelper.setDownloadedFakeDragShown(isShown)

    override fun getDownloadFakeDragShown() = prefsHelper.getDownloadFakeDragShown()

    override fun setPreviousDownloadProgress(previousProgress: Double) =
        prefsHelper.setPreviousDownloadProgress(previousProgress)

    override fun getPreviousDownloadProgress() = prefsHelper.getPreviousDownloadProgress()

    override fun getIntentLinkToDownload() = prefsHelper.getIntentLinkToDownload()

    override fun setIntentLinkToDownload(link: String) = prefsHelper.setIntentLinkToDownload(link)

    override fun setCurrentPostDownloadDbId(currentPostDownloadDbId: Long) =
        prefsHelper.setCurrentPostDownloadDbId(currentPostDownloadDbId)

    override fun getCurrentPostDownloadDbId() = prefsHelper.getCurrentPostDownloadDbId()

    override fun setCurrentPostDownloadMediaCount(currentMediaCount: Int) =
        prefsHelper.setCurrentPostDownloadMediaCount(currentMediaCount)

    override fun getCurrentPostDownloadMediaCount() = prefsHelper.getCurrentPostDownloadMediaCount()

    override fun setIsDarkModeEnabled(isDarkModeEnabled: Boolean) =
        prefsHelper.setIsDarkModeEnabled(isDarkModeEnabled)

    override fun getIsDarkModeEnabled() = prefsHelper.getIsDarkModeEnabled()

    override fun setCurrentDownloadDbId(currentDownloadDbId: Long) =
        prefsHelper.setCurrentDownloadDbId(currentDownloadDbId)

    override fun getCurrentDownloadDbId() = prefsHelper.getCurrentDownloadDbId()

    override fun setExploreRefreshTimes(times: Int) = prefsHelper.setExploreRefreshTimes(times)

    override fun setExploreLastRefreshAt(refreshAt: Long) =
        prefsHelper.setExploreLastRefreshAt(refreshAt)

    override fun getExploreLastRefreshAt() = prefsHelper.getExploreLastRefreshAt()

    override fun getExploreRefreshTimes() = prefsHelper.getExploreRefreshTimes()

    override fun setCollectionRefreshTimes(times: Int) =
        prefsHelper.setCollectionRefreshTimes(times)

    override fun setCollectionLastRefreshAt(refreshAt: Long) =
        prefsHelper.setCollectionLastRefreshAt(refreshAt)

    override fun getCollectionLastRefreshAt() = prefsHelper.getCollectionLastRefreshAt()

    override fun getCollectionRefreshTimes() = prefsHelper.getCollectionRefreshTimes()

    override fun setLikeRefreshTimes(times: Int) = prefsHelper.setLikeRefreshTimes(times)

    override fun setLikeLastRefreshAt(refreshAt: Long) = prefsHelper.setLikeLastRefreshAt(refreshAt)

    override fun getLikeLastRefreshAt() = prefsHelper.getLikeLastRefreshAt()

    override fun getLikeRefreshTimes() = prefsHelper.getLikeRefreshTimes()

    override fun getCurrentLoggedInUser() = prefsHelper.getCurrentLoggedInUser()

    override fun setCurrentLoggedInUser(loggedInUser: LoggedInUser) =
        prefsHelper.setCurrentLoggedInUser(loggedInUser)

    override fun setIsIntroductionDone(done: Boolean) = prefsHelper.setIsIntroductionDone(done)

    override fun getIsIntroductionDone() = prefsHelper.getIsIntroductionDone()

    override fun setCurrentUserPlanTag(planTag: String) = prefsHelper.setCurrentUserPlanTag(planTag)

    override fun getCurrentUserPlanTag() = prefsHelper.getCurrentUserPlanTag()

    override fun setSubscriptionStartedAt(startedAt: Long) =
        prefsHelper.setSubscriptionStartedAt(startedAt)

    override fun getSubscriptionStartedAt() = prefsHelper.getSubscriptionStartedAt()

    override fun setSubscriptionExpireAt(expireAt: Long) =
        prefsHelper.setSubscriptionExpireAt(expireAt)

    override fun getSubscriptionExpireAt() = prefsHelper.getSubscriptionExpireAt()

    override fun setApplicationAccessToken(accessToken: AccessTokenResponse) =
        prefsHelper.setApplicationAccessToken(accessToken)

    override fun getApplicationAccessToken(): AccessTokenResponse? =
        prefsHelper.getApplicationAccessToken()

    override fun setApplicationAccessTokenIssuedAt(issuedAt: Long) =
        prefsHelper.setApplicationAccessTokenIssuedAt(issuedAt)

    override fun getApplicationAccessTokenIssuedAt() =
        prefsHelper.getApplicationAccessTokenIssuedAt()

    override fun signOutUser() {
        prefsHelper.signOutUser()
    }

    // endregion

    // region Room

    override suspend fun getPostWithAllByInstagramId(instagramId: String) =
        videoDownloaderRoomImp.getPostWithAllByInstagramId(instagramId)

    override suspend fun setPostsToDefaultUser(loggedInUserId: Long) =
        videoDownloaderRoomImp.setPostsToDefaultUser(loggedInUserId)

    override suspend fun setMediaUsersToDefaultUser(loggedInUserId: Long) =
        videoDownloaderRoomImp.setMediaUsersToDefaultUser(loggedInUserId)

    override suspend fun getPostToDownloadById(postToDownloadId: Long) =
        videoDownloaderRoomImp.getPostToDownloadById(postToDownloadId)

    override suspend fun deletePostToDownload(postToDownloadId: Long) =
        videoDownloaderRoomImp.deletePostToDownload(postToDownloadId)

    override suspend fun insertPostToDownload(postToDownload: PostToDownload) =
        videoDownloaderRoomImp.insertPostToDownload(postToDownload)

    override suspend fun deletePost(post: Post) = videoDownloaderRoomImp.deletePost(post)

    override suspend fun deleteMedia(media: Media) = videoDownloaderRoomImp.deleteMedia(media)

    override suspend fun insertMediaToDownload(mediaToDownload: MediaToDownload) =
        videoDownloaderRoomImp.insertMediaToDownload(mediaToDownload)

    override suspend fun insertMediaToDownload(mediaToDownloads: List<MediaToDownload>) =
        videoDownloaderRoomImp.insertMediaToDownload(mediaToDownloads)

    override suspend fun getPostsToDownload() = videoDownloaderRoomImp.getPostsToDownload()

    override suspend fun deleteMediaToDownload(identifier: Long) =
        videoDownloaderRoomImp.deleteMediaToDownload(identifier)

    override suspend fun getPostByInstagramId(instagramId: String) =
        videoDownloaderRoomImp.getPostByInstagramId(instagramId)

    override suspend fun getMediaUserByIdentifier(pkIdOrInstagramId: String): MediaUser? =
        videoDownloaderRoomImp.getMediaUserByIdentifier(pkIdOrInstagramId)

    override suspend fun getDefaultUser() = videoDownloaderRoomImp.getDefaultUser()

    override suspend fun insertCaption(caption: Caption) =
        videoDownloaderRoomImp.insertCaption(caption)

    override suspend fun insertPost(post: Post) = videoDownloaderRoomImp.insertPost(post)

    override suspend fun insertMediaUser(mediaUser: MediaUser) =
        videoDownloaderRoomImp.insertMediaUser(mediaUser)

    override suspend fun insertMedia(media: Media) = videoDownloaderRoomImp.insertMedia(media)

    override suspend fun getUserPosts() = videoDownloaderRoomImp.getUserPosts()

    override suspend fun deleteUser(user: User) = videoDownloaderRoomImp.deleteUser(user)

    override suspend fun getUserByIdentifier(identifier: Long) =
        videoDownloaderRoomImp.getUserByIdentifier(identifier)

    override suspend fun getAllUsers() = videoDownloaderRoomImp.getAllUsers()

    override suspend fun insertUser(user: User) = videoDownloaderRoomImp.insertUser(user)

    override suspend fun updateUser(user: User) = videoDownloaderRoomImp.updateUser(user)

    // endregion

    // region Api

    override suspend fun updateUserSubscription(
        accessToken: String,
        userUniqueId: String,
        data: Map<String, Any>
    ) = videoDownloaderApi.updateUserSubscription(accessToken, userUniqueId, data)

    override suspend fun saveUserSubscription(
        accessToken: String,
        userUniqueId: String,
        data: Map<String, Any>
    ) = videoDownloaderApi.saveUserSubscription(accessToken, userUniqueId, data)

    override suspend fun getLoggedInUserAudioInfo(
        userUniqueId: String,
        accessToken: String,
        data: Map<String, Any>
    ) = videoDownloaderApi.getLoggedInUserAudioInfo(userUniqueId, accessToken, data)

    override suspend fun getPlans(accessToken: String) = videoDownloaderApi.getPlans(accessToken)

    override suspend fun getAudioInfo(
        userUniqueId: String,
        accessToken: String,
        data: Map<String, Any>
    ) = videoDownloaderApi.getAudioInfo(userUniqueId, accessToken, data)

    override suspend fun getHighlightInfo(
        userUniqueId: String,
        accessToken: String,
        data: Map<String, Any>
    ) = videoDownloaderApi.getHighlightInfo(userUniqueId, accessToken, data)

    override suspend fun getMediaInfo(
        userUniqueId: String,
        accessToken: String,
        data: Map<String, Any>
    ) =
        videoDownloaderApi.getMediaInfo(userUniqueId, accessToken, data)

    override suspend fun checkVersion(accessToken: String, data: Map<String, Any>) =
        videoDownloaderApi.checkVersion(accessToken, data)

    override suspend fun findUser(accessToken: String, data: Map<String, Any>) =
        videoDownloaderApi.findUser(accessToken, data)

    override suspend fun getUserFromToken(accessToken: String) =
        videoDownloaderApi.getUserFromToken(accessToken)

    override suspend fun getUserSubscriptions(
        userUniqueId: String,
        accessToken: String
    ) = videoDownloaderApi.getUserSubscriptions(userUniqueId, accessToken)

    override suspend fun updateUser(
        userUniqueId: String,
        accessToken: String,
        data: Map<String, Any>
    ) =
        videoDownloaderApi.updateUser(userUniqueId, accessToken, data)

    override suspend fun signUp(accessToken: String, data: Map<String, Any>) =
        videoDownloaderApi.signUp(accessToken, data)

    override suspend fun login(
        accessToken: String,
        credentials: Map<String, Any>
    ) = videoDownloaderApi.login(accessToken, credentials)

    override suspend fun getAppConfig(accessToken: String, data: Map<String, Any>) =
        videoDownloaderApi.getAppConfig(accessToken, data)

    override suspend fun getAppAccessToken(credentials: Map<String, Any>) =
        videoDownloaderApi.getAppAccessToken(credentials)

    // endregion

    // region InstaApi

    override suspend fun extractHighlight(
        hashCode: String,
        data: Map<String, Any>,
        headers: Map<String, Any>
    ) = instaApi.extractHighlight(hashCode, data, headers)

    override suspend fun extractMediaById(
        storyId: String,
        query: Map<String, Any>,
        headers: Map<String, Any>
    ) = instaApi.extractMediaById(storyId, query, headers)

    override suspend fun extractPost(
        postCode: String,
        query: Map<String, Any>,
        headers: Map<String, Any>
    ) = instaApi.extractPost(postCode, query, headers)

    override suspend fun extractReel(
        postCode: String,
        query: Map<String, Any>,
        headers: Map<String, Any>
    ) = instaApi.extractReel(postCode, query, headers)

    override suspend fun getUserInfo(userId: String, headers: Map<String, Any>) =
        instaApi.getUserInfo(userId, headers)

    override suspend fun getUserExplore(headers: Map<String, Any>, queryMap: Map<String, Any>) =
        instaApi.getUserExplore(headers, queryMap)

    override suspend fun getUserCollections(
        headers: Map<String, Any>,
        params: Map<String, Any>
    ) = instaApi.getUserCollections(headers, params)


    // endregion

}