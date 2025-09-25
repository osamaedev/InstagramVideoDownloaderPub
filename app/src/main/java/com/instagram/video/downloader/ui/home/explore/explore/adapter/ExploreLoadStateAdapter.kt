package com.instagram.video.downloader.ui.home.explore.explore.adapter

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter

class ExploreLoadStateAdapter : LoadStateAdapter<ExploreLoadingViewHolder>() {

    override fun onBindViewHolder(holder: ExploreLoadingViewHolder, loadState: LoadState) {
        holder.onBind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): ExploreLoadingViewHolder {
        return ExploreLoadingViewHolder.create(parent)
    }
}