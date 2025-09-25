package com.instagram.video.downloader.cases.collection

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.instagram.video.downloader.cases.user.GetPostToDownload
import com.instagram.video.downloader.cases.user.GetUserLocalPostsUseCase
import com.instagram.video.downloader.cases.user.LoggedInUser
import com.instagram.video.downloader.data.DataManager
import com.instagram.video.downloader.ui.home.explore.collection.adapter.CollectionMediaPagingSource
import javax.inject.Inject

class GetUserCollectionPagingMedia @Inject constructor(private val dataManager: DataManager) {
    operator fun invoke(
        getUserCollectionUseCase: GetUserCollectionUseCase,
        getUserLocalPostsUseCase: GetUserLocalPostsUseCase,
        getPostToDownload: GetPostToDownload,
        loggedInUser: LoggedInUser
    ) = Pager(
        config = PagingConfig(
            pageSize = 10,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            CollectionMediaPagingSource(
                getUserCollectionUseCase, getUserLocalPostsUseCase, getPostToDownload, loggedInUser
            )
        }
    ).flow
}