package com.instagram.video.downloader.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.io.Serializable

@Entity(
    tableName = "posts",
    indices = [
        Index("id", unique = true),
        Index("instagram_id", unique = true),
    ],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.NO_ACTION,
        ),
        ForeignKey(
            entity = MediaUser::class,
            parentColumns = ["id"],
            childColumns = ["media_user_id"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.NO_ACTION,
        )
    ]
)
data class Post(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var identifier: Long? = null,

    @ColumnInfo(name = "instagram_id") val mediaId: String,         // represent the the instagram Id
    @ColumnInfo(name = "media_type") val mediaType: Int,            // -1 for audio
    @ColumnInfo(name = "product_type") val productType: String,     // for audios: audio
    @ColumnInfo(name = "code") val code: String,
    @ColumnInfo(name = "taken_at") val takenAt: Long,
    @ColumnInfo(name = "comment_count") val commentCount: Long,
    @ColumnInfo(name = "like_count") val likeCount: Long,

    @ColumnInfo(name = "thumbnail_url") val thumbnailUrl: String,


    // foreign
    @ColumnInfo(name = "user_id", index = true) val userId: Long?,
    @ColumnInfo(name = "media_user_id", index = true) val mediaUserId: Long,


    @ColumnInfo(name = "created_at") var createAt: Long,
    @ColumnInfo(name = "updated_at") var updatedAt: Long,
) : Serializable


data class PostWithMediaUserAndMedia(
    @Embedded val post: Post,

    @Relation(
        parentColumn = "media_user_id",
        entityColumn = "id"
    )
    val mediaUser: MediaUser,

    @Relation(
        parentColumn = "id",
        entityColumn = "post_id"
    )
    val media: List<Media>,

    @Relation(
        parentColumn = "id",
        entityColumn = "post_id",
    )
    val caption: Caption?,
) : Serializable