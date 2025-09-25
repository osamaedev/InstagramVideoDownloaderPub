package com.instagram.video.downloader.data.remote.api.methods

import com.instagram.video.downloader.data.remote.api.dto.AppConfig
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST


@JvmSuppressWildcards
interface AppConfigApi {

    @FormUrlEncoded
    @POST("appConfigs")
    suspend fun getAppConfig(
        @Header("Authorization") accessToken: String,
        @FieldMap data: Map<String, Any>,
    ): AppConfig

}