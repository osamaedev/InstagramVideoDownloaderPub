package com.instagram.video.downloader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import com.instagram.video.downloader.data.local.room.entities.PostWithMediaUserAndMedia
import com.instagram.video.downloader.data.local.room.entities.User


@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE id = -1")
    suspend fun getDefaultUser(): User?

    @Query("SELECT * FROM users WHERE id != -1")
    suspend fun getAllUsers(): List<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("SELECT * FROM users WHERE id=:identifier")
    suspend fun getUserByIdentifier(identifier: Long): User

    @Delete
    suspend fun deleteUser(user: User)

    @Transaction
    @Query("SELECT * FROM posts ORDER BY created_at DESC")              // WHERE user_id=:userId OR user_id = -1
    suspend fun getUserPosts(): List<PostWithMediaUserAndMedia>
}