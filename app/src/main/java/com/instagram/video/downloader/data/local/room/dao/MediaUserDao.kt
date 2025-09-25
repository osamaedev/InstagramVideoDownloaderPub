package com.instagram.video.downloader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.instagram.video.downloader.data.local.room.entities.MediaUser


@Dao
interface MediaUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaUser(mediaUser: MediaUser) : Long


    @Query("SELECT * FROM media_users WHERE pk_id=:pkIdOrInstagramId")
    suspend fun getMediaUserByIdentifier(pkIdOrInstagramId: String) : MediaUser?

    /**
     *  Called before deleting a user, I can make a checkbox to the user to choose
     *  if he want to delete the posts also or not.
     */
    @Query("UPDATE media_users SET user_id=-1 WHERE user_id=:loggedInUserId")
    suspend fun setMediaUsersToDefaultUser(loggedInUserId: Long)
}