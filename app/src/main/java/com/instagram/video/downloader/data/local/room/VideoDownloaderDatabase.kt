package com.instagram.video.downloader.data.local.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.instagram.video.downloader.data.local.room.dao.CaptionDao
import com.instagram.video.downloader.data.local.room.dao.MediaDao
import com.instagram.video.downloader.data.local.room.dao.MediaToDownloadDao
import com.instagram.video.downloader.data.local.room.dao.MediaUserDao
import com.instagram.video.downloader.data.local.room.dao.PostDao
import com.instagram.video.downloader.data.local.room.dao.PostToDownloadDao
import com.instagram.video.downloader.data.local.room.dao.UserDao
import com.instagram.video.downloader.data.local.room.entities.Caption
import com.instagram.video.downloader.data.local.room.entities.Media
import com.instagram.video.downloader.data.local.room.entities.MediaToDownload
import com.instagram.video.downloader.data.local.room.entities.MediaUser
import com.instagram.video.downloader.data.local.room.entities.Post
import com.instagram.video.downloader.data.local.room.entities.PostToDownload
import com.instagram.video.downloader.data.local.room.entities.User


@Database(
    entities = [
        User::class,
        Media::class,
        Caption::class,
        MediaUser::class,
        Post::class,
        MediaToDownload::class,
        PostToDownload::class,
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ]
)
abstract class VideoDownloaderDatabase : RoomDatabase() {

    abstract fun postToDownloadDao(): PostToDownloadDao
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun captionDao(): CaptionDao
    abstract fun mediaDao(): MediaDao
    abstract fun mediaUserDao(): MediaUserDao
    abstract fun mediaToDownloadDao(): MediaToDownloadDao

}