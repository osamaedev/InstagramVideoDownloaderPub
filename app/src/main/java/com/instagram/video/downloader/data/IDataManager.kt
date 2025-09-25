package com.instagram.video.downloader.data

import com.instagram.video.downloader.data.local.prefs.IPrefsHelper
import com.instagram.video.downloader.data.local.room.IVideoDownloaderRoom
import com.instagram.video.downloader.data.remote.api.IVideoDownloaderApi
import com.instagram.video.downloader.data.remote.instaApi.IInstaApi

interface IDataManager : IPrefsHelper, IVideoDownloaderRoom, IVideoDownloaderApi, IInstaApi {

    fun signOut()

}