package com.instagram.video.downloader.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "media",
    indices = [
        Index("id", unique = true),
        Index("instagram_id", unique = true),
        Index("file_location", unique = true),
    ],
    foreignKeys = [
        ForeignKey(
            entity = Post::class,
            parentColumns = ["id"],
            childColumns = ["post_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class Media(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var identifier: Long = 0L,

    @ColumnInfo(name = "instagram_id") val instagramId: String,            // if the post has one media, it will be the same as the post

    @ColumnInfo(name = "type") val type: String,                            // video, image or music
    @ColumnInfo(name = "display_url") val displayUrl: String,
    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String,
    @ColumnInfo(name = "file_location") val fileLocation: String,

    @ColumnInfo(name = "post_id", index = true) val postId: Long,


    @ColumnInfo(name = "created_at") var createAt: Long,
    @ColumnInfo(name = "updated_at") var updatedAt: Long,
) : Serializable
