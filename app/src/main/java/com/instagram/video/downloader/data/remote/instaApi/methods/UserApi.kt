package com.instagram.video.downloader.data.remote.instaApi.methods

import com.instagram.video.downloader.data.remote.instaApi.dto.collections.CollectionResponse
import com.instagram.video.downloader.data.remote.instaApi.dto.explore.ExploreResponse
import com.instagram.video.downloader.data.remote.instaApi.dto.userInfo.UserInfo
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap


@JvmSuppressWildcards
interface UserApi {

    @GET("api/v1/discover/web/explore_grid")
    suspend fun getUserExplore(
        @HeaderMap headers: Map<String, Any>,
        @QueryMap queryMap: Map<String, Any>
    ): ExploreResponse


    @GET("api/v1/feed/saved/posts")
    suspend fun getUserCollections(
        @HeaderMap headers: Map<String, Any>,
        @QueryMap params: Map<String, Any>,
    ): CollectionResponse


    @GET("api/v1/users/{userId}/info")
    suspend fun getUserInfo(
        @Path("userId") userId: String,
        @HeaderMap headers: Map<String, Any>
    ): UserInfo

}