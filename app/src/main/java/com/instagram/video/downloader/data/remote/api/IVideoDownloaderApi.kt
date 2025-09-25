package com.instagram.video.downloader.data.remote.api

import com.instagram.video.downloader.data.remote.api.dto.AccessTokenResponse
import com.instagram.video.downloader.data.remote.api.methods.AppConfigApi
import com.instagram.video.downloader.data.remote.api.methods.PlanApi
import com.instagram.video.downloader.data.remote.api.methods.UserApi
import com.instagram.video.downloader.data.remote.api.methods.VersionApi
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


@JvmSuppressWildcards
interface IVideoDownloaderApi : AppConfigApi, UserApi, VersionApi, PlanApi {

    @FormUrlEncoded
    @POST("oauth/token")
    suspend fun getAppAccessToken(
        @FieldMap credentials: Map<String, Any>,
    ): AccessTokenResponse

}