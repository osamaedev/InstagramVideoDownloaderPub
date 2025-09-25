package com.instagram.video.downloader.data.remote.instaApi

import javax.inject.Inject

class InstaApi @Inject constructor(
    private val instaApi: IInstaApi
) : IInstaApi {


    override suspend fun extractHighlight(
        hashCode: String,
        data: Map<String, Any>,
        headers: Map<String, Any>
    ) = instaApi.extractHighlight(hashCode, data, headers)

    override suspend fun getUserExplore(headers: Map<String, Any>, queryMap: Map<String, Any>) =
        instaApi.getUserExplore(headers, queryMap)


    override suspend fun getUserCollections(
        headers: Map<String, Any>,
        params: Map<String, Any>
    ) = instaApi.getUserCollections(headers, params)

    override suspend fun getUserInfo(
        userId: String, headers: Map<String, Any>,
    ) = instaApi.getUserInfo(userId, headers)

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

    override suspend fun extractMediaById(
        storyId: String,
        query: Map<String, Any>,
        headers: Map<String, Any>
    ) = instaApi.extractMediaById(storyId, query, headers)
}