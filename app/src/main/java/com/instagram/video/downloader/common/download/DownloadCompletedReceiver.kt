package com.instagram.video.downloader.common.download

import android.app.DownloadManager
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
class DownloadCompletedReceiver :
    BroadcastReceiver() {

    @Inject
    lateinit var vdDownloadManager: VDDownloadManager

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "android.intent.action.DOWNLOAD_COMPLETE") {
            val idFinishedDownload = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (idFinishedDownload != -1L) {
                Timber.tag("DownloadCompletedReceiv")
                    .v("Download with ID $idFinishedDownload finished!")
                CoroutineScope(Dispatchers.IO).launch {
                    vdDownloadManager.deleteFinishedDownload()
                    vdDownloadManager.startNextOrStop()
                }
            }
        }
    }
}