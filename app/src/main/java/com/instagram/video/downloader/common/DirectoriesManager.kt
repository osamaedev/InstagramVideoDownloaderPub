package com.instagram.video.downloader.common

import android.content.Context
import android.os.Environment
import java.io.File

class DirectoriesManager(private val appContext: Context) {

    fun getImagesDirectory(profileName: String): File =
        File(Environment.DIRECTORY_PICTURES, "InstaSaved/$profileName").apply {
            mkdirs()
        }

    fun getVideosDirectory(profileName: String): File =
        File(Environment.DIRECTORY_MOVIES, "InstaSaved/$profileName").apply {
            mkdirs()
        }

    fun getMusicDirectory(profileName: String): File =
        File(Environment.DIRECTORY_MUSIC, "InstaSaved/$profileName").apply {
            mkdirs()
        }
}
