package com.instagram.video.downloader.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
    tableName = "captions",
    indices = [
        Index("id", unique = true),
        Index("pk", unique = true),
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
data class Caption(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var identifier: Long = 0L,

    @ColumnInfo(name = "pk") val pk: String,
    @ColumnInfo(name = "text") val text: String,

    @ColumnInfo(name = "post_id", index = true) val postId: Long,


    @ColumnInfo(name = "created_at") var createAt: Long,
    @ColumnInfo(name = "updated_at") var updatedAt: Long,
) : Serializable