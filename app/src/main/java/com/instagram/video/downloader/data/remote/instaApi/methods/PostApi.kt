package com.instagram.video.downloader.data.remote.instaApi.methods

import com.instagram.video.downloader.data.remote.instaApi.dto.extraction.HighlightExtractionResponse
import com.instagram.video.downloader.data.remote.instaApi.dto.extraction.PostExtractionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap


@JvmSuppressWildcards
interface PostApi {

    @POST("graphql/query/")
    suspend fun extractHighlight(
        @Query("query_hash") hashCode: String,
        @Body data: Map<String, Any>,
        @HeaderMap headers: Map<String, Any>,
    ): HighlightExtractionResponse

    @GET("p/{postCode}")
    suspend fun extractPost(
        @Path("postCode") postCode: String,
        @QueryMap query: Map<String, Any>,
        @HeaderMap headers: Map<String, Any>,
    ): PostExtractionResponse


    @GET("reel/{postCode}")
    suspend fun extractReel(
        @Path("postCode") postCode: String,
        @QueryMap query: Map<String, Any>,
        @HeaderMap headers: Map<String, Any>,
    ): PostExtractionResponse


    @GET("api/v1/media/{Id}/info/")
    suspend fun extractMediaById(
        @Path("Id") storyId: String,
        @QueryMap query: Map<String, Any>,
        @HeaderMap headers: Map<String, Any>,
    ): PostExtractionResponse
}