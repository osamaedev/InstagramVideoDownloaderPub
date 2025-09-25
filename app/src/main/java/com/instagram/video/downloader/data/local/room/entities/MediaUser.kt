package com.instagram.video.downloader.data.local.room.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Entity(
    tableName = "media_users",
    indices = [
        Index("id", unique = true),
    ],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.SET_NULL,
            onUpdate = ForeignKey.NO_ACTION,
        )
    ]
)
data class MediaUser(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var identifier: Long? = null,

    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "is_private") val isPrivate: Boolean,
    @ColumnInfo(name = "pk_id") val pkId: String,
    @ColumnInfo(name = "fbid_v2") val fbIdV2: String,
    @ColumnInfo(name = "account_type") val accountType: Int,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "profile_pic_id") val profilePicId: String,
    @ColumnInfo(name = "profile_pic_url") val profilePicUrl: String,

    @ColumnInfo(name = "user_id", index = true) val userId: Long?,

    @ColumnInfo(name = "created_at") var createAt: Long,
    @ColumnInfo(name = "updated_at") var updatedAt: Long,
) : Serializable
