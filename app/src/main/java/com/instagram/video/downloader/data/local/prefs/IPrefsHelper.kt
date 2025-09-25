package com.instagram.video.downloader.data.local.prefs

import com.instagram.video.downloader.cases.user.LoggedInUser
import com.instagram.video.downloader.data.remote.api.dto.AccessTokenResponse

interface IPrefsHelper {

    fun setExploreFakeDragShown(isShown: Boolean)
    fun getExploreFakeDragShown(): Boolean

    fun setCollectionFakeDragShown(isShown: Boolean)
    fun getCollectionFakeDragShown(): Boolean


    fun setDownloadedFakeDragShown(isShown: Boolean)
    fun getDownloadFakeDragShown(): Boolean

    fun getIntentLinkToDownload(): String
    fun setIntentLinkToDownload(link: String)

    fun setIsDarkModeEnabled(isDarkModeEnabled: Boolean)
    fun getIsDarkModeEnabled(): Boolean

    fun setCurrentDownloadDbId(currentDownloadDbId: Long)               // represent the media to download db Id
    fun getCurrentDownloadDbId(): Long

    fun setPreviousDownloadProgress(previousProgress: Double)
    fun getPreviousDownloadProgress(): Double

    fun setCurrentPostDownloadDbId(currentPostDownloadDbId: Long)
    fun getCurrentPostDownloadDbId(): Long

    fun setCurrentPostDownloadMediaCount(currentMediaCount: Int)        // this the progress handling
    fun getCurrentPostDownloadMediaCount(): Int

    fun setExploreRefreshTimes(times: Int)
    fun setExploreLastRefreshAt(refreshAt: Long)
    fun getExploreLastRefreshAt(): Long
    fun getExploreRefreshTimes(): Int

    fun setCollectionRefreshTimes(times: Int)
    fun setCollectionLastRefreshAt(refreshAt: Long)
    fun getCollectionLastRefreshAt(): Long
    fun getCollectionRefreshTimes(): Int


    fun setLikeRefreshTimes(times: Int)
    fun setLikeLastRefreshAt(refreshAt: Long)
    fun getLikeLastRefreshAt(): Long
    fun getLikeRefreshTimes(): Int


    fun getCurrentLoggedInUser(): LoggedInUser?
    fun setCurrentLoggedInUser(loggedInUser: LoggedInUser)


    fun setIsIntroductionDone(done: Boolean)
    fun getIsIntroductionDone(): Boolean


    // region Subscription information

    fun setCurrentUserPlanTag(planTag: String)
    fun getCurrentUserPlanTag(): String

    fun setSubscriptionStartedAt(startedAt: Long)
    fun getSubscriptionStartedAt(): Long

    fun setSubscriptionExpireAt(expireAt: Long)
    fun getSubscriptionExpireAt(): Long

    // endregion


    // region AppAccessToken

    fun setApplicationAccessToken(accessToken: AccessTokenResponse)
    fun getApplicationAccessToken(): AccessTokenResponse?

    fun setApplicationAccessTokenIssuedAt(issuedAt: Long)
    fun getApplicationAccessTokenIssuedAt(): Long

    // endregion


    fun signOutUser()

}