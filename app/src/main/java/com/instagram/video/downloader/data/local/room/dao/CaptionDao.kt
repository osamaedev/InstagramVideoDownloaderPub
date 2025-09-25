package com.instagram.video.downloader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Upsert
import com.instagram.video.downloader.data.local.room.entities.Caption


@Dao
interface CaptionDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCaption(caption: Caption): Long

}