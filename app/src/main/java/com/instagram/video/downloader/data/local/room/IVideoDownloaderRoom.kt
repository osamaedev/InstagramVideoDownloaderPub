package com.instagram.video.downloader.data.local.room

import com.instagram.video.downloader.data.local.room.dao.CaptionDao
import com.instagram.video.downloader.data.local.room.dao.MediaDao
import com.instagram.video.downloader.data.local.room.dao.MediaToDownloadDao
import com.instagram.video.downloader.data.local.room.dao.MediaUserDao
import com.instagram.video.downloader.data.local.room.dao.PostDao
import com.instagram.video.downloader.data.local.room.dao.PostToDownloadDao
import com.instagram.video.downloader.data.local.room.dao.UserDao

interface IVideoDownloaderRoom : UserDao, PostDao, CaptionDao, MediaUserDao, MediaDao, MediaToDownloadDao, PostToDownloadDao