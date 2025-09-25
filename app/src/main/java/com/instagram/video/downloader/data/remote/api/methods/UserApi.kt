package com.instagram.video.downloader.data.remote.api.methods

import com.instagram.video.downloader.data.remote.api.dto.AccessTokenResponse
import com.instagram.video.downloader.data.remote.api.dto.AudioResponse
import com.instagram.video.downloader.data.remote.api.dto.SubscriptionDtoExtended
import com.instagram.video.downloader.data.remote.api.dto.UserDto
import com.instagram.video.downloader.data.remote.api.dto.HighlightResponse
import com.instagram.video.downloader.data.remote.api.dto.PostResponse
import com.instagram.video.downloader.data.remote.api.dto.SubscriptionDto
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path


@JvmSuppressWildcards
interface UserApi {

    @FormUrlEncoded
    @PATCH("users/{user}/plans")
    suspend fun updateUserSubscription(
        @Header("Authorization") accessToken: String,
        @Path("user") userUniqueId: String,
        @FieldMap data: Map<String, Any>,
    ) : SubscriptionDto


    @FormUrlEncoded
    @POST("users/{user}/plans")
    suspend fun saveUserSubscription(
        @Header("Authorization") accessToken: String,
        @Path("user") userUniqueId: String,
        @FieldMap data: Map<String, Any>,
    ) : SubscriptionDto

    @GET("users/profile/me")
    suspend fun getUserFromToken(
        @Header("Authorization") accessToken: String,
    ): UserDto


    @GET("users/{user}/subscriptions")
    suspend fun getUserSubscriptions(
        @Path("user") userUniqueId: String,
        @Header("Authorization") accessToken: String,
    ): SubscriptionDto



    @FormUrlEncoded
    @PATCH("users/{user}")
    suspend fun updateUser(
        @Path("user") userUniqueId: String,
        @Header("Authorization") accessToken: String,
        @FieldMap data: Map<String, Any>,
    ): UserDto


    @FormUrlEncoded
    @POST("users")
    suspend fun signUp(
        @Header("Authorization") accessToken: String,
        @FieldMap data: Map<String, Any>,
    ): UserDto

    @FormUrlEncoded
    @POST("users/login")
    suspend fun login(
        @Header("Authorization") accessToken: String,
        @FieldMap credentials: Map<String, Any>,
    ): AccessTokenResponse


    @FormUrlEncoded
    @POST("users/findUser")
    suspend fun findUser(
        @Header("Authorization") accessToken: String,
        @FieldMap data: Map<String, Any>,
    ): UserDto


    @FormUrlEncoded
    @POST("users/{user}/downloadRequests")
    suspend fun getHighlightInfo(
        @Path("user") userUniqueId: String,
        @Header("Authorization") accessToken: String,
        @FieldMap data: Map<String, Any>,
    ) : HighlightResponse


    @FormUrlEncoded
    @POST("users/{user}/downloadRequests")
    suspend fun getMediaInfo(
        @Path("user") userUniqueId: String,
        @Header("Authorization") accessToken: String,
        @FieldMap data: Map<String, Any>,
    ) : PostResponse



    @FormUrlEncoded
    @POST("users/{user}/downloadRequests")
    suspend fun getAudioInfo(
        @Path("user") userUniqueId: String,
        @Header("Authorization") accessToken: String,
        @FieldMap data: Map<String, Any>,
    ) : AudioResponse


    @FormUrlEncoded
    @POST("downloadRequest/{user}/getAudioInfo")
    suspend fun getLoggedInUserAudioInfo(
        @Path("user") userUniqueId: String,
        @Header("Authorization") accessToken: String,
        @FieldMap data: Map<String, Any>,
    ) : AudioResponse

}