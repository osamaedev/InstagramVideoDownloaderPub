package com.instagram.video.downloader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.instagram.video.downloader.data.local.room.entities.Media


@Dao
interface MediaDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: Media): Long

    @Delete
    suspend fun deleteMedia(media: Media)

}