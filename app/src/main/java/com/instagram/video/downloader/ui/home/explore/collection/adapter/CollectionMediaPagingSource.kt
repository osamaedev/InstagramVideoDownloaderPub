package com.instagram.video.downloader.ui.home.explore.collection.adapter

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.instagram.video.downloader.cases.collection.GetUserCollectionUseCase
import com.instagram.video.downloader.cases.user.GetPostToDownload
import com.instagram.video.downloader.cases.user.GetUserLocalPostsUseCase
import com.instagram.video.downloader.cases.user.LoggedInUser
import com.instagram.video.downloader.data.remote.instaApi.dto.collections.CollectionMedia
import com.instagram.video.downloader.data.remote.instaApi.dto.collections.toCollectionPage
import kotlinx.coroutines.delay
import timber.log.Timber

class CollectionMediaPagingSource(
    private val getUserCollectionUseCase: GetUserCollectionUseCase,
    private val getUserLocalPostsUseCase: GetUserLocalPostsUseCase,
    private val getPostToDownload: GetPostToDownload,
    private val loggedInUser: LoggedInUser,
) : PagingSource<String, CollectionMedia>() {

    override fun getRefreshKey(state: PagingState<String, CollectionMedia>): String {
        return ""
    }

    override suspend fun load(params: LoadParams<String>): LoadResult<String, CollectionMedia> {
        try {
            val pageAfter = params.key ?: ""
            val collectionPage =
                getUserCollectionUseCase.invoke(loggedInUser, pageAfter).toCollectionPage()
            val collectionMedias = collectionPage.mediaList

            val downloadedInstagramIds = getUserLocalPostsUseCase(loggedInUser).map { it.post.mediaId }
            val downloadingInstagramIds = getPostToDownload.invoke().map { it.postToDownload.instagramId }

            val collectionMediaIds = collectionMedias?.map { it.id!! }

            val toSetAsDownloaded = collectionMediaIds?.filter { it in downloadedInstagramIds }?.toTypedArray()
            val toSetAsDownloading = collectionMediaIds?.filter { it in downloadingInstagramIds }?.toTypedArray()

            if (!toSetAsDownloaded.isNullOrEmpty()) {
                toSetAsDownloaded.forEach { id ->
                    collectionMedias.first { it.id == id }.apply { isDownloaded = true }
                }
            }

            if (!toSetAsDownloading.isNullOrEmpty()) {
                toSetAsDownloading.forEach { id ->
                    collectionMedias.first { it.id == id }.apply { isDownloading = true }
                }
            }

            val prevKey = if (pageAfter == "") null else pageAfter
            val nextKey = collectionPage.pageAfter

            return LoadResult.Page(
                collectionPage.mediaList ?: emptyList(),
                prevKey,
                nextKey,
            )

        } catch (e: Exception) {
            Timber.tag("CollectionMediaPaging").e(e)
            return LoadResult.Error(e)
        }
    }
}