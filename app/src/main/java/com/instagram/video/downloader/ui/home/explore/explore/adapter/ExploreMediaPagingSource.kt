package com.instagram.video.downloader.ui.home.explore.explore.adapter

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.instagram.video.downloader.cases.explore.GetUserExploreUseCase
import com.instagram.video.downloader.cases.user.GetPostToDownload
import com.instagram.video.downloader.cases.user.GetUserLocalPostsUseCase
import com.instagram.video.downloader.cases.user.LoggedInUser
import com.instagram.video.downloader.data.remote.instaApi.dto.explore.Media
import com.instagram.video.downloader.data.remote.instaApi.dto.explore.toExplorePage
import timber.log.Timber

class ExploreMediaPagingSource(
    private val getUserExploreUseCase: GetUserExploreUseCase,
    private val getUserLocalPostsUseCase: GetUserLocalPostsUseCase,
    private val getPostToDownload: GetPostToDownload,
    private val loggedInUser: LoggedInUser,
) : PagingSource<String, Media>() {


    override fun getRefreshKey(state: PagingState<String, Media>): String {
        return ""
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Media> {
        try {
            val pageId = params.key ?: ""
            val exploreResponse = getUserExploreUseCase.invoke(loggedInUser, pageId)
            val explorePage = exploreResponse.toExplorePage()
            val exploreMedias = explorePage.mediaList

            val downloadedInstagramIds =
                getUserLocalPostsUseCase.invoke(loggedInUser).map { it.post.mediaId }
            val downloadingInstagramIds =
                getPostToDownload.invoke().map { it.postToDownload.instagramId }

            val explorePageMediaIds = exploreMedias?.map { it.id!! }

            val toSetAsDownloaded =
                explorePageMediaIds?.filter { it in downloadedInstagramIds }?.toTypedArray()

            val toSetAsDownloading =
                explorePageMediaIds?.filter { it in downloadingInstagramIds }?.toTypedArray()

            if (!toSetAsDownloaded.isNullOrEmpty()) {
                toSetAsDownloaded.forEach { id ->
                    exploreMedias.first { it.id == id }.apply { isDownloaded = true }
                }
            }

            if (!toSetAsDownloading.isNullOrEmpty()) {
                toSetAsDownloading.forEach { id ->
                    exploreMedias.first { it.id == id }.apply { isDownloading = true }
                }
            }

            val prevKey = if (pageId == "") null else pageId
            val nextKey = explorePage.maxId

            return LoadResult.Page(
                exploreMedias ?: emptyList(),
                prevKey,
                nextKey,
            )
        } catch (e: Exception) {
            Timber.tag("ExploreMediaPagSource").e(e)
            return LoadResult.Error(e)
        }
    }
}