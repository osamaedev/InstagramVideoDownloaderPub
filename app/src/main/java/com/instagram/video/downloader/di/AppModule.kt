package com.instagram.video.downloader.di

import android.app.DownloadManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.GsonBuilder
import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.common.Constants.PREFS_NAME
import com.instagram.video.downloader.common.download.IDownloaderManager
import com.instagram.video.downloader.common.download.VDDownloadManager
import com.instagram.video.downloader.data.local.prefs.IPrefsHelper
import com.instagram.video.downloader.data.local.prefs.PrefsHelper
import com.instagram.video.downloader.data.local.room.IVideoDownloaderRoom
import com.instagram.video.downloader.data.local.room.VideoDownloaderDatabase
import com.instagram.video.downloader.data.local.room.VideoDownloaderRoomImp
import com.instagram.video.downloader.data.remote.api.IVideoDownloaderApi
import com.instagram.video.downloader.data.DataManager
import com.instagram.video.downloader.data.IDataManager
import com.instagram.video.downloader.data.remote.api.VideoDownloaderApi
import com.instagram.video.downloader.data.remote.instaApi.IInstaApi
import com.instagram.video.downloader.data.remote.utils.ItemTypeAdapterFactory
import com.instagram.video.downloader.di.qualifiers.MainThreadHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import okhttp3.CipherSuite
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.security.GeneralSecurityException
import java.util.concurrent.TimeUnit
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {


    @Provides
    @Singleton
    fun provideIInstaApi(): IInstaApi {
        val connectionSpec = ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
            .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
            .cipherSuites(
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA
            )
            .build()

        val okHttpClient = OkHttpClient.Builder()
            .followRedirects(true)
            .connectionSpecs(listOf(connectionSpec))
            .connectTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .addInterceptor {
                val request = it.request()
                    .newBuilder()
                    .build()
                return@addInterceptor it.proceed(request)
            }

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        okHttpClient.addInterceptor(interceptor)

        return Retrofit.Builder()
            .baseUrl(VideoDownloaderApp.instaBaseUrl)
            .client(okHttpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IInstaApi::class.java)
    }

    @Provides
    @Singleton
    fun provideIVideoDownloaderApi(): IVideoDownloaderApi {
        val connectionSpec = ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
            .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
            .cipherSuites(
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
                CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
                CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
                CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA
            )
            .build()

        val okHttpClient = OkHttpClient.Builder()
            .connectionSpecs(listOf(connectionSpec))
            .connectTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .addInterceptor {
                val request = it.request()
                    .newBuilder()
                    .addHeader("Accept", "application/json")
                    .build()
                return@addInterceptor it.proceed(request)
            }

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        okHttpClient.addInterceptor(interceptor)

        val gsonBuilder = GsonBuilder()
            .registerTypeAdapterFactory(ItemTypeAdapterFactory())
            .create()

        return Retrofit.Builder()
            .baseUrl(VideoDownloaderApp.baseUrl)
            .client(okHttpClient.build())
            .addConverterFactory(GsonConverterFactory.create(gsonBuilder))
            .build()
            .create(IVideoDownloaderApi::class.java)
    }

    @Provides
    @Singleton
    fun provideVideoDownloaderDatabase(@ApplicationContext context: Context): VideoDownloaderDatabase {
        val supportFactory =
            SupportFactory(VideoDownloaderApp.key.reversed().toByteArray().reversedArray())
        return Room
            .databaseBuilder(
                context,
                VideoDownloaderDatabase::class.java,
                "instagram_video_downloader"
            )
            .openHelperFactory(supportFactory)
            .build()
    }


    @Provides
    @Singleton
    fun provideIVideoDownloaderRoom(videoDownloaderDatabase: VideoDownloaderDatabase): IVideoDownloaderRoom {
        return VideoDownloaderRoomImp(videoDownloaderDatabase)
    }

    @Provides
    @Singleton
    fun provideDataManager(
        prefsHelper: PrefsHelper,
        videoDownloaderRoomImp: VideoDownloaderRoomImp,
        videoDownloaderApi: VideoDownloaderApi,
        iInstaApi: IInstaApi
    ): IDataManager {
        return DataManager(
            prefsHelper = prefsHelper,
            videoDownloaderRoomImp = videoDownloaderRoomImp,
            videoDownloaderApi = videoDownloaderApi,
            instaApi = iInstaApi
        )
    }


    @Provides
    @Singleton
    fun providerPrefsHelper(sharedPreferences: SharedPreferences): IPrefsHelper {
        return PrefsHelper(sharedPreferences)
    }


    @Provides
    @Singleton
    fun providerSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        try {
            val masterKey =
                MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
            return EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
            return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        } catch (e: IOException) {
            e.printStackTrace()
            return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        } catch (e: Exception) {
            e.printStackTrace()
            return context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        }
    }

    @Provides
    @Singleton
    fun provideDownloaderManager(
        @ApplicationContext context: Context,
        dataManager: IDataManager,
        downloadManager: DownloadManager,
    ): IDownloaderManager {
        return VDDownloadManager(
            context = context,
            dataManager = dataManager,
            downloadManager = downloadManager,
        )
    }


    @Provides
    fun provideDownloadManager(@ApplicationContext context: Context): DownloadManager {
        return context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }


    @MainThreadHandler
    @Provides
    fun provideMainThreadHandler(): Handler {
        return Handler(Looper.getMainLooper()!!)
    }

//    @Provides
//    @Singleton
//    fun provideFlowSharedPreferences(sharedPreferences: SharedPreferences): FlowSharedPreferences {
//        return FlowSharedPreferences(sharedPreferences)
//    }
}