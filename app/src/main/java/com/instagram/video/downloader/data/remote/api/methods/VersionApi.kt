package com.instagram.video.downloader.data.remote.api.methods

import com.instagram.video.downloader.data.remote.api.dto.Version
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST


@JvmSuppressWildcards
interface VersionApi {

    @FormUrlEncoded
    @POST("versions/checkVersion")
    suspend fun checkVersion(
        @Header("Authorization") accessToken: String,
        @FieldMap data: Map<String, Any>,
    ): Version

}