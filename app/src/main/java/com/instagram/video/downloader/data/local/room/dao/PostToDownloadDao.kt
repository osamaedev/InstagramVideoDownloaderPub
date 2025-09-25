package com.instagram.video.downloader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.instagram.video.downloader.data.local.room.entities.PostToDownload
import com.instagram.video.downloader.data.local.room.entities.PostToDownloadWithMediaToDownload


@Dao
interface PostToDownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPostToDownload(postToDownload: PostToDownload): Long

    @Transaction
    @Query("SELECT * FROM posts_to_download ORDER BY created_at DESC")
    suspend fun getPostsToDownload(): List<PostToDownloadWithMediaToDownload>


    @Transaction
    @Query("SELECT * FROM posts_to_download WHERE id=:postToDownloadId")
    suspend fun getPostToDownloadById(postToDownloadId: Long) : PostToDownloadWithMediaToDownload


    @Query("DELETE FROM posts_to_download WHERE id=:postToDownloadId")
    suspend fun deletePostToDownload(postToDownloadId: Long)

}