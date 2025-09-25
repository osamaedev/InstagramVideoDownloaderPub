package com.instagram.video.downloader.common

import com.instagram.video.downloader.R
import com.instagram.video.downloader.ui.howTo.HowToItem

object Constants {

    const val PREFS_NAME = "instagram_video_downloader_prefs"

    const val SIZE_TO_INCLUDE_NATIVE_PLAYER_AFTER = 8
    const val PLAYER_NATIVE_AD_INTERVAL = 6


    val howToSteps: List<HowToItem> = arrayListOf(
        HowToItem(R.drawable.step_1_1, 1, false, R.string.step_11_description, R.drawable.how_to_step_1),
        HowToItem(R.drawable.step_1_2, 1, false, R.string.step_12_description, R.drawable.how_to_step_2),

        HowToItem(R.drawable.step_1_1, 1, true, R.string.methods_divider, R.drawable.how_to_step_3),

        HowToItem(R.drawable.step_2_1, 2, false, R.string.step_21_description, R.drawable.how_to_step_1),
        HowToItem(R.drawable.step_2_1, 2, false, R.string.step_22_description, R.drawable.how_to_step_2, true)
    )
}