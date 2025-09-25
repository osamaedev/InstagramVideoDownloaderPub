package com.instagram.video.downloader.common.download

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
open class DownloadProgressReceiver : BroadcastReceiver() {

    @Inject
    lateinit var vdDownloadManager: VDDownloadManager

    companion object {
        private const val TAG = "DownloadProgressReceiver"

        // intent
        const val DOWNLOAD_IN_PROGRESS = "com.instagram.video.downloader.Download_In_Progress"
        const val DOWNLOAD_POST_COMPLETED = "com.instagram.video.downloader.Download_Post_Completed"
        const val DOWNLOAD_LIST_POSTS_COMPLETED = "com.instagram.video.downloader.Download_List_Completed"

        // keys
        const val DOWNLOAD_POST_IS_MASS_DOWNLOAD = "com.instagram.video.downloader.Is_Mass_Download"
        const val DOWNLOAD_PROGRESS_VALUE_KEY = "com.instagram.video.downloader.Download_Progress_Value"
        const val DOWNLOAD_MANAGER_ID_KEY = "com.instagram.video.downloader.Download_Manager_Id"
        const val DOWNLOAD_POST_INSTAGRAM_ID_KEY = "com.instagram.video.downloader.Post_Instagram_Id"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && intent.action == DOWNLOAD_POST_COMPLETED) {
            Timber.tag("DownloadProgressRecei").v("Post download completed ---------")
            CoroutineScope(Dispatchers.IO).launch {
                vdDownloadManager.startNextOrStop()
            }
        }
    }
}