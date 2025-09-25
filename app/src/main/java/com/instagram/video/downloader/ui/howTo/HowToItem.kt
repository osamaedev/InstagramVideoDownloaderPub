package com.instagram.video.downloader.ui.howTo

import androidx.annotation.DrawableRes

data class HowToItem(
    @DrawableRes val stepImageUrl: Int,
    val methodNum: Int,
    val isMethodsDivider: Boolean = false,
    val methodDescription: Int,
    @DrawableRes val methodDrawable: Int,
    val isLastItem: Boolean = false,
)
