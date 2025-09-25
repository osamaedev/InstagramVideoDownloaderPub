package com.instagram.video.downloader.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation


@Entity(
    tableName = "users",
    indices = [
        Index("id", unique = true),
    ]
)
data class User(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var identifier: Long = 0L,

    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "biography") val biography: String,
    @ColumnInfo(name = "username") val username: String,

    @ColumnInfo(name = "profile_pic_url") val profilePicUrl: String,

    @ColumnInfo(name = "instagram_id") val instagramId: String,
    @ColumnInfo(name = "cookies") val cookies: String,

    @ColumnInfo(name = "logged_in_at") val loggedInAt: Long,
    @ColumnInfo(name = "cookies_expiring_at") val cookiesExpiringAt: Long,

    @ColumnInfo(name = "created_at") var createAt: Long,
    @ColumnInfo(name = "updated_at") var updatedAt: Long,
)