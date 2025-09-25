package com.instagram.video.downloader.common

import com.instagram.video.downloader.ui.player.PlayerDataModel
import java.io.Serializable

data class TempPlayerDataModel(
    val list: ArrayList<PlayerDataModel>
) : Serializable