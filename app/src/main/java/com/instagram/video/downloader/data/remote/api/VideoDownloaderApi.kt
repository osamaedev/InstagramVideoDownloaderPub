package com.instagram.video.downloader.data.remote.api

import javax.inject.Inject

class VideoDownloaderApi @Inject constructor(
    private val api: IVideoDownloaderApi
) : IVideoDownloaderApi {

    override suspend fun updateUserSubscription(
        accessToken: String,
        userUniqueId: String,
        data: Map<String, Any>
    ) = api.updateUserSubscription(accessToken, userUniqueId, data)

    override suspend fun saveUserSubscription(
        accessToken: String,
        userUniqueId: String,
        data: Map<String, Any>
    ) = api.saveUserSubscription(accessToken, userUniqueId, data)

    override suspend fun getPlans(accessToken: String) = api.getPlans(accessToken)

    override suspend fun getAudioInfo(
        userUniqueId: String,
        accessToken: String,
        data: Map<String, Any>
    ) = api.getAudioInfo(userUniqueId, accessToken, data)

    override suspend fun getLoggedInUserAudioInfo(
        userUniqueId: String,
        accessToken: String,
        data: Map<String, Any>
    ) = api.getLoggedInUserAudioInfo(
        userUniqueId, accessToken, data
    )

    override suspend fun getHighlightInfo(
        userUniqueId: String,
        accessToken: String,
        data: Map<String, Any>
    ) = api.getHighlightInfo(userUniqueId, accessToken, data)

    override suspend fun getMediaInfo(
        userUniqueId: String,
        accessToken: String,
        data: Map<String, Any>
    ) =
        api.getMediaInfo(userUniqueId, accessToken, data)

    override suspend fun getUserFromToken(accessToken: String) = api.getUserFromToken(accessToken)

    override suspend fun getUserSubscriptions(
        userUniqueId: String,
        accessToken: String
    ) = api.getUserSubscriptions(userUniqueId, accessToken)

    override suspend fun updateUser(
        userUniqueId: String,
        accessToken: String,
        data: Map<String, Any>
    ) =
        api.updateUser(userUniqueId, accessToken, data)

    override suspend fun signUp(accessToken: String, data: Map<String, Any>) =
        api.signUp(accessToken, data)

    override suspend fun login(
        accessToken: String,
        credentials: Map<String, Any>
    ) = api.login(accessToken, credentials)

    override suspend fun getAppConfig(accessToken: String, data: Map<String, Any>) =
        api.getAppConfig(accessToken, data)

    override suspend fun getAppAccessToken(credentials: Map<String, Any>) =
        api.getAppAccessToken(credentials)

    override suspend fun findUser(accessToken: String, data: Map<String, Any>) =
        api.findUser(accessToken, data)

    override suspend fun checkVersion(accessToken: String, data: Map<String, Any>) =
        api.checkVersion(accessToken, data)
}