package com.instagram.video.downloader.ui.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.instagram.video.downloader.databinding.ExploreCollectionNativeAdContainerBinding
import com.instagram.video.downloader.databinding.PlayerNativeAdItemBinding
import com.instagram.video.downloader.ui.home.explore.explore.adapter.ExploreAdapter
import com.instagram.video.downloader.ui.player.PlayerAdapter
import com.instagram.video.downloader.ui.player.PlayerDataModel
import com.instagram.video.downloader.ui.player.CarouselAdapter
import com.instagram.video.downloader.ui.player.CarouselPlayerDataModel

abstract class BaseViewHolder(private val viewItem: View) : RecyclerView.ViewHolder(viewItem) {
    abstract fun onBind(position: Int)
}

abstract class PlayerBaseViewHolder(private val viewItem: View) : RecyclerView.ViewHolder(viewItem) {
    abstract fun onBind(position: Int, adapter: PlayerAdapter, media: PlayerDataModel)
}

abstract class CarouselPlayerBaseViewHolder(private val viewItem: View): RecyclerView.ViewHolder(viewItem) {
    abstract fun onBind(position: Int, adapter: CarouselAdapter, media: CarouselPlayerDataModel)
}

abstract class PlayerBaseNativeAdViewHolder(private val binding: PlayerNativeAdItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

   abstract fun onBind(position: Int, adapter: PlayerAdapter, adProvider: String, adId: String)
}

abstract class ExploreBaseNativeAdViewHolder(private val binding: PlayerNativeAdItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    abstract fun onBind(position: Int, adapter: ExploreAdapter, adProvider: String, adId: String)
}

class NativeAdViewHolder(private val binding: ExploreCollectionNativeAdContainerBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun onBind(someText: String) {
        binding.adContainer.text = someText
    }

    companion object {
        fun create(parent: ViewGroup): NativeAdViewHolder {
            return NativeAdViewHolder(
                ExploreCollectionNativeAdContainerBinding.inflate(
                    LayoutInflater.from(parent.context)
                )
            )
        }
    }
}








