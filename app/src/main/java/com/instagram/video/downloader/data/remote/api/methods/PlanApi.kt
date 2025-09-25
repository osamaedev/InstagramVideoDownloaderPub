package com.instagram.video.downloader.data.remote.api.methods

import com.instagram.video.downloader.data.remote.api.dto.PlanDto
import retrofit2.http.GET
import retrofit2.http.Header


@JvmSuppressWildcards
interface PlanApi {

    @GET("plans")
    suspend fun getPlans(
        @Header("Authorization") accessToken: String,
    ) : List<PlanDto>

}