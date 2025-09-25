package com.instagram.video.downloader.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation


@Entity(
    tableName = "media_to_download",
    indices = [
        Index("id", unique = true),
        Index("media_instagram_id", unique = true),
    ],
    foreignKeys = [
        ForeignKey(
            entity = PostToDownload::class,
            parentColumns = ["id"],
            childColumns = ["post_to_download_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class MediaToDownload(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var identifier: Long = 0L,

//    @ColumnInfo(name = "download_manager_id") val downloadManagerId: Long,
    @ColumnInfo(name = "url") val url: String,
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "profile_username") val profileUsername: String,
    @ColumnInfo(name = "media_type") val mediaType: String,

    @ColumnInfo(name = "is_downloading") val isDownloading: Boolean = false,
    @ColumnInfo(name = "media_instagram_id") val instagramId: String,

    @ColumnInfo(name = "created_at") val createdAt: Long,

    @ColumnInfo(name = "index") var index: Int = 0,

    @ColumnInfo(name = "post_to_download_id", index = true) var postToDownloadId: Long = 0,
) {
    @Ignore
    var downloadManagerId: Long = -1L
}

@Entity(
    tableName = "posts_to_download",
    indices = [
        Index("id", unique = true),
        Index("media_instagram_id", unique = true),
    ]
)
data class PostToDownload(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var identifier: Long = 0L,

    @ColumnInfo(name = "url") val url: String,

    @ColumnInfo(name = "profile_username") val profileUsername: String,
    @ColumnInfo(name = "media_type") val mediaType: Int,

    @ColumnInfo(name = "is_downloading") val isDownloading: Boolean = false,
    @ColumnInfo(name = "media_instagram_id") val instagramId: String,

    @ColumnInfo(name = "created_at") val createdAt: Long,

    @ColumnInfo(name = "is_mass_download", defaultValue = "0") val isMassDownload: Boolean = false,
) {
    @Ignore
    var mediaToDownload: List<MediaToDownload> = arrayListOf()
}

data class PostToDownloadWithMediaToDownload(
    @Embedded val postToDownload: PostToDownload,
    @Relation(
        parentColumn = "id",
        entityColumn = "post_to_download_id",
    )
    val mediaToDownload: List<MediaToDownload>,
)
