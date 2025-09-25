package com.instagram.video.downloader.common.download

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.instagram.video.downloader.common.download.DownloadProgressReceiver.Companion.DOWNLOAD_IN_PROGRESS
import com.instagram.video.downloader.common.download.DownloadProgressReceiver.Companion.DOWNLOAD_LIST_POSTS_COMPLETED
import com.instagram.video.downloader.common.download.DownloadProgressReceiver.Companion.DOWNLOAD_MANAGER_ID_KEY
import com.instagram.video.downloader.common.download.DownloadProgressReceiver.Companion.DOWNLOAD_POST_COMPLETED
import com.instagram.video.downloader.common.download.DownloadProgressReceiver.Companion.DOWNLOAD_POST_INSTAGRAM_ID_KEY
import com.instagram.video.downloader.common.download.DownloadProgressReceiver.Companion.DOWNLOAD_POST_IS_MASS_DOWNLOAD
import com.instagram.video.downloader.common.download.DownloadProgressReceiver.Companion.DOWNLOAD_PROGRESS_VALUE_KEY
import com.instagram.video.downloader.common.roundTo
import com.instagram.video.downloader.data.IDataManager
import com.instagram.video.downloader.data.local.room.entities.MediaToDownload
import com.instagram.video.downloader.data.local.room.entities.PostToDownload
import com.instagram.video.downloader.data.local.room.entities.PostToDownloadWithMediaToDownload
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject

class VDDownloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataManager: IDataManager,
    private val downloadManager: DownloadManager
) : IDownloaderManager {

    fun getCurrentDownloadDbId() = dataManager.getCurrentDownloadDbId()

    suspend fun deleteFinishedDownload() {
        dataManager.deleteMediaToDownload(dataManager.getCurrentDownloadDbId())
    }


    /**
     *  This method is called after check that postsToDownload non empty media to Download list
     */
    override suspend fun buildNextRequest(postAndMediaItems: PostToDownloadWithMediaToDownload): MediaToDownload {
        val itemsToDownload =
            postAndMediaItems.mediaToDownload.sortedBy { it.index }              // I sort by index
        val mimeTypeAndExtension = getMimeTypeAndExtension(itemsToDownload.first().url)
        val mediaToDownload = itemsToDownload.first()
        val request = DownloadManager
            .Request(mediaToDownload.url.toUri())
            .setMimeType(mimeTypeAndExtension.first)
            .setVisibleInDownloadsUi(true)
            .setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        or DownloadManager.Request.NETWORK_MOBILE
            )
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setTitle(mediaToDownload.fileName)
            .setDestinationInExternalPublicDir(
                getWhereToDownloadLocation(mediaToDownload.mediaType.toMediaType()),
                "${mediaToDownload.profileUsername}/${mediaToDownload.fileName}.${mimeTypeAndExtension.second}"
            )
        dataManager.setCurrentDownloadDbId(mediaToDownload.identifier)
        mediaToDownload.downloadManagerId = downloadManager.enqueue(request)
        return mediaToDownload
    }

    override suspend fun addToDownloadItems(postAndMediaItems: PostToDownload) {
        val postDbId = dataManager.insertPostToDownload(postAndMediaItems)
        postAndMediaItems.mediaToDownload.sortedBy { it.createdAt }.forEachIndexed { index, media ->
            media.index = index
            media.postToDownloadId = postDbId
        }
        dataManager.insertMediaToDownload(postAndMediaItems.mediaToDownload)
        if (dataManager.getCurrentPostDownloadDbId() == -1L) {          // no post is downloading, so start, otherwise the download is running
            startNextOrStop()
        }
    }

    override suspend fun addToDownloadItems(postsAndMediaItems: List<PostToDownload>) {
        postsAndMediaItems.forEach { postAndMediaItems ->
            val postDbId = dataManager.insertPostToDownload(postAndMediaItems)
            postAndMediaItems.mediaToDownload.sortedBy { it.createdAt }
                .forEachIndexed { index, media ->
                    media.index = index
                    media.postToDownloadId = postDbId
                }
            dataManager.insertMediaToDownload(postAndMediaItems.mediaToDownload)
        }
        if (dataManager.getCurrentPostDownloadDbId() == -1L) {          // no post is downloading, so start, otherwise the download is running
            startNextOrStop()
        }
    }

    suspend fun startNextOrStop() {
        val postsToDownload = dataManager.getPostsToDownload()
        if (postsToDownload.isEmpty()) {
            val intent = Intent(DOWNLOAD_LIST_POSTS_COMPLETED).apply {
                setPackage(context.packageName)
                putExtra(DOWNLOAD_MANAGER_ID_KEY, -1L)
            }
            context.sendBroadcast(intent)
            return
        }


        // TODO: check the status of downloading and start, case when app crashes or download failed

        /**
         *  No current post is downloading
         */
        val currentDownloadingPost = if (dataManager.getCurrentPostDownloadDbId() == -1L) {
            dataManager.setCurrentPostDownloadDbId(postsToDownload.first().postToDownload.identifier)
            dataManager.setCurrentPostDownloadMediaCount(postsToDownload.first().mediaToDownload.count())
            postsToDownload.first()
        } else {
            dataManager.getPostToDownloadById(dataManager.getCurrentPostDownloadDbId())
        }


        if (currentDownloadingPost.mediaToDownload.isEmpty()) {
            // means all media downloaded
            dataManager.setPreviousDownloadProgress(0.0)
            dataManager.deletePostToDownload(dataManager.getCurrentPostDownloadDbId())
            dataManager.setCurrentPostDownloadDbId(-1L)             // set current downloading post id to -1L

            val intent = Intent(DOWNLOAD_POST_COMPLETED).apply {
                setPackage(context.packageName)
                putExtra(
                    DOWNLOAD_POST_INSTAGRAM_ID_KEY,
                    currentDownloadingPost.postToDownload.instagramId
                )
                putExtra(
                    DOWNLOAD_POST_IS_MASS_DOWNLOAD,
                    currentDownloadingPost.postToDownload.isMassDownload
                )
            }
            context.sendBroadcast(intent)
            return
        }

        val mediaToDownload = buildNextRequest(currentDownloadingPost)

        val previousProgress = dataManager.getPreviousDownloadProgress()
        var progress = 0.0

        var isDownloadFinished = false

        val intent = Intent(DOWNLOAD_IN_PROGRESS).apply {
            setPackage(context.packageName)
            putExtra(DOWNLOAD_PROGRESS_VALUE_KEY, previousProgress)
            putExtra(
                DOWNLOAD_POST_INSTAGRAM_ID_KEY,
                currentDownloadingPost.postToDownload.instagramId
            )
            putExtra(DOWNLOAD_MANAGER_ID_KEY, mediaToDownload.downloadManagerId)
            putExtra(
                DOWNLOAD_POST_IS_MASS_DOWNLOAD,
                currentDownloadingPost.postToDownload.isMassDownload
            )
        }
        context.sendBroadcast(intent)


        var lastProgressSent = 0.0          // to prevent sending same value multiple times

        while (isDownloadFinished.not()) {
            val cursor =
                downloadManager.query(
                    DownloadManager.Query().setFilterById(mediaToDownload.downloadManagerId)
                )
            if (cursor.moveToFirst()) {
                val columnStatus = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                if (columnStatus >= 0) {
                    val downloadStatus =
                        cursor.getInt(columnStatus)

                    when (downloadStatus) {
                        DownloadManager.STATUS_RUNNING -> {
                            val totalSizeColumn =
                                cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                            val downloadSizeColumn =
                                cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            if (totalSizeColumn >= 0 && downloadSizeColumn >= 0) {
                                val totalBytes =
                                    cursor.getLong(totalSizeColumn)
                                if (totalBytes >= 1) {
                                    val downloadedBytes =
                                        cursor.getLong(downloadSizeColumn)
                                    progress = if (downloadedBytes > 100L) {
                                        ((downloadedBytes * 100.0) / totalBytes)
                                    } else {
                                        0.0
                                    }
                                }
                            }
                        }

                        DownloadManager.STATUS_SUCCESSFUL -> {
                            progress = 100.0
                            isDownloadFinished = true
                        }

                        DownloadManager.STATUS_FAILED or DownloadManager.ERROR_UNKNOWN -> {
                            progress = -1.0
                            isDownloadFinished = true
                        }
                    }

                    if (/*dataManager.getCurrentPostDownloadMediaCount() > 0 && */ progress > 0.0) {
                        val mediaCount = dataManager.getCurrentPostDownloadMediaCount()
                        val part1 = (progress / mediaCount)
                        val part2 =
                            if (mediaToDownload.index != 0) (mediaToDownload.index.toDouble() / mediaCount) * 100.0 else 0.0
                        progress = (part1 + part2)
                    }


                    if (dataManager.getCurrentDownloadDbId() != -1L
                        && progress > previousProgress
                        && progress > 0.0
                        && progress > lastProgressSent
                    ) {
                        dataManager.setPreviousDownloadProgress(progress)
                        intent.apply {
                            putExtra(DOWNLOAD_PROGRESS_VALUE_KEY, progress.roundTo(2))
                            putExtra(
                                DOWNLOAD_POST_INSTAGRAM_ID_KEY,
                                currentDownloadingPost.postToDownload.instagramId
                            )
                        }
                        context.sendBroadcast(intent)
                        lastProgressSent = progress
                    }

                }
                delay(50)
                cursor.close()
            }
        }
    }


    /**
     *  Check if a download is running
     */
    @SuppressLint("Range")
    fun getDownloadStatus(downloadId: Long): Int {
        val cursor =
            downloadManager.query(DownloadManager.Query().setFilterById(downloadId))
        if (cursor.moveToFirst()) {
            return cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
        }
        return -1
    }

    private fun getWhereToDownloadLocation(mediaType: MediaType): String {
        return when (mediaType) {
            MediaType.IMAGE -> Environment.DIRECTORY_PICTURES
            MediaType.VIDEO -> Environment.DIRECTORY_MOVIES
            MediaType.MUSIC -> Environment.DIRECTORY_MUSIC
        }
    }

    private fun getMimeTypeAndExtension(url: String): Pair<String, String> {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            val mimeType =
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "video/mp4"
            return mimeType to extension
        }
        return "video/mp4" to "mp4"
    }
}