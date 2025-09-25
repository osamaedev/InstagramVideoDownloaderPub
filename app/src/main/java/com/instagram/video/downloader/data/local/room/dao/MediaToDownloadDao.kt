package com.instagram.video.downloader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.instagram.video.downloader.data.local.room.entities.MediaToDownload
import com.instagram.video.downloader.data.local.room.entities.PostToDownloadWithMediaToDownload


@Dao
interface MediaToDownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaToDownload(mediaToDownload: MediaToDownload): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaToDownload(mediaToDownloads: List<MediaToDownload>)


    @Query("DELETE FROM media_to_download WHERE id=:identifier")
    suspend fun deleteMediaToDownload(identifier: Long)

}