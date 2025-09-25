package com.instagram.video.downloader.cases.explore

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.instagram.video.downloader.cases.user.GetPostToDownload
import com.instagram.video.downloader.cases.user.GetUserLocalPostsUseCase
import com.instagram.video.downloader.cases.user.LoggedInUser
import com.instagram.video.downloader.ui.home.explore.explore.adapter.ExploreMediaPagingSource
import javax.inject.Inject

class GetUserExplorePagingMedia @Inject constructor() {
    operator fun invoke(
        getUserExploreUseCase: GetUserExploreUseCase,
        getUserLocalPostsUseCase: GetUserLocalPostsUseCase,
        getPostToDownload: GetPostToDownload,
        loggedInUser: LoggedInUser
    ) = Pager(
        config = PagingConfig(
            pageSize = 20,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = {
            ExploreMediaPagingSource(
                getUserExploreUseCase,
                getUserLocalPostsUseCase,
                getPostToDownload,
                loggedInUser
            )
        }
    ).flow
}