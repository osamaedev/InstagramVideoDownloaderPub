package com.instagram.video.downloader.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.instagram.video.downloader.data.local.room.entities.Post
import com.instagram.video.downloader.data.local.room.entities.PostWithMediaUserAndMedia


@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: Post): Long


    @Query("SELECT * FROM posts WHERE instagram_id=:instagramId")
    suspend fun getPostByInstagramId(instagramId: String): Post?


    @Transaction
    @Query("SELECT * FROM posts where instagram_id=:instagramId")
    suspend fun getPostWithAllByInstagramId(instagramId: String): PostWithMediaUserAndMedia

    @Delete
    suspend fun deletePost(post: Post)


    /**
     *  Called before deleting a user, I can make a checkbox to the user to choose
     *  if he want to delete the posts also or not.
     */
    @Query("""
        UPDATE posts SET user_id=-1 WHERE user_id=:loggedInUserId
    """)
    suspend fun setPostsToDefaultUser(loggedInUserId: Long)

}