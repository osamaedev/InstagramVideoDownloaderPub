package com.instagram.video.downloader.common

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import android.view.View
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.viewpager2.widget.ViewPager2
import com.instagram.video.downloader.BuildConfig
import com.instagram.video.downloader.R
import com.instagram.video.downloader.data.local.room.entities.PostWithMediaUserAndMedia
import com.instagram.video.downloader.data.remote.instaApi.dto.collections.CollectionMedia
import com.instagram.video.downloader.data.remote.instaApi.dto.explore.Media
import com.instagram.video.downloader.ui.base.BaseActivity
import com.instagram.video.downloader.ui.base.showMessage
import com.instagram.video.downloader.ui.player.PlayerDataModel
import timber.log.Timber
import java.io.File
import java.util.Calendar
import java.util.Locale
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Duration.Companion.milliseconds


fun lockView(view: View, handler: Handler, time: Long = 300) {
    view.isClickable = false
    handler.postDelayed({
        view.isClickable = true
    }, time)
}


/**
 *  if want to open instagram profile use this: https://instagram.com/_u/user_name
 */
fun openInstagram(linkToOpen: String, context: Context) {
    val uri = Uri.parse(linkToOpen)
    val forBrowser = Intent(Intent.ACTION_VIEW, uri)
    val forApp = Intent(Intent.ACTION_VIEW, uri)
    forApp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    forBrowser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    forApp.component =
        ComponentName(
            "com.instagram.android",
            "com.instagram.android.activity.UrlHandlerActivity"
        )

    try {
        context.startActivity(forApp)
    } catch (e: ActivityNotFoundException) {
        Timber.tag("OpenInstagramMethod").e(e)
        context.startActivity(forBrowser)
    }
}

fun openWeblink(link: String, context: Context) {
    try {
        Intent(Intent.ACTION_VIEW, Uri.parse(link)).let {
            context.startActivity(it)
        }
    } catch (e: Exception) {
        Timber.tag("openWeblink").e(e)
        (context as BaseActivity<*>).showMessage("Can\'t handle link.")
    }
}

fun shareApp(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            setType("text/plain")
            val googlePlayLink =
                context.resources.getString(R.string.play_store_url, context.packageName)
            val message = context.resources.getString(
                R.string.share_plan_text,
                context.resources.getString(R.string.app_name),
                googlePlayLink
            )
            putExtra(Intent.EXTRA_TEXT, message)
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_app_subject))
        }
        val intent1 = Intent.createChooser(intent, context.resources.getString(R.string.share_via))
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent1)
    } catch (e: Exception) {
        Timber.tag("shareApp").e(e)
    }
}

fun shareText(context: Context, link: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            setType("text/plain")
            putExtra(Intent.EXTRA_TEXT, link)
            putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_media_subject))
            val intent1 =
                Intent.createChooser(this, context.resources.getString(R.string.share_via))
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent1)
        }
    } catch (e: Exception) {
        Timber.tag("shareText").e(e)
    }
}

@SuppressLint("HardwareIds")
fun getId(context: Context): String =
    Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

fun extractUserInstagramIdFromCookies(cookies: String): String {
    val cookiesItems = cookies.split("; ")
    var username = ""
    cookiesItems.forEach { cookie ->
        val fullCookie = cookie.split("=")
        if (fullCookie[0] == "ds_user_id") {
            username = fullCookie[1]
        }
    }
    return username
}

fun extractCsrfFromCookies(cookies: String): String {
    val cookiesItems = cookies.split("; ")
    var token = ""
    cookiesItems.forEach { cookie ->
        val fullCookie = cookie.split("=")
        if (fullCookie[0] == "csrftoken") {
            token = fullCookie[1]
        }
    }
    return token
}

fun getCookiesExpiringAt(cookies: String = ""): Long {
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, 4)
    val date = calendar.time
    return date.time.milliseconds.inWholeMilliseconds
}

fun isValidInstagramLink(link: String): Boolean {
    return link.startsWith("https://www.instagram.com")
            || link.startsWith("https://instagram.com")
            || link.startsWith("instagram.com")
}

fun clearCookies() {
    CookieManager.getInstance().removeAllCookies(null)
    CookieManager.getInstance().flush()
}

fun openAppInPlayStore(appPackageName: String, context: Context) {
    try {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$appPackageName")
            )
        )
    } catch (exception: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
            )
        )
    }
}


fun isStoryLink(url: String): Boolean {
    val regex = Regex(
        """https://www\.instagram\.com/stories/[a-zA-Z0-9_.-]+/[0-9]+/?"""
    )
    return regex.find(url) != null
}

fun extractStoryId(url: String): String {
    val regex = "/(\\d{19})".toRegex()
    val match = regex.find(url)
    return if (match != null) {
        match.groupValues[1]
    } else {
        ""
    }
}

fun isPostLink(url: String): Boolean {
    val regex = Regex(
        """https://www\.instagram\.com/p/[a-zA-Z0-9_-]+/"""
    )
    return regex.find(url) != null
}

fun extractPostCode(url: String): String {
    val regex = """https://www\.instagram\.com/p/([a-zA-Z0-9_-]+)/""".toRegex()
    val matchResult = regex.find(url)
    return matchResult?.groups?.get(1)?.value ?: ""
}

fun isReelLink(url: String): Boolean {
    val regex = Regex(
        """https://www\.instagram\.com/(reel|reels)/[a-zA-Z0-9_-]+/"""
    )
    return regex.find(url) != null
}

fun extractReelCode(url: String): String {
    var newUrl = url
    if (url.contains("reels")) {
        newUrl = url.replace("reels", "reel")
    }
    val regex = """https://www\.instagram\.com/reel/([a-zA-Z0-9_-]+)/""".toRegex()
    val matchResult = regex.find(newUrl)
    return matchResult?.groups?.get(1)?.value ?: ""
}

fun checkInstagramLink(url: String): Boolean {
    val regex = Regex(
        """https://www\.instagram\.com/(p|stories/p|stories|reel|reels|stories/highlights|reels/audio|stories/)/[a-zA-Z0-9_\\-]+/?"""
    )
    return regex.find(url) != null
}

fun isHighlightShareLink(url: String): Boolean {
    val regex = Regex(
        """https://www\.instagram\.com/s/[a-zA-Z0-9_\\-]+\?story_media_id=[0-9]+_[0-9]+/?"""
    )
    return regex.find(url) != null
}

@OptIn(ExperimentalEncodingApi::class)
fun extractHighlightIdPhoneShareLink(url: String): String {
    val regex = Regex("(?<=/s/)[a-zA-Z0-9]+")
    val match = regex.find(url)
    return if (match != null) {
        val decodedBytes = Base64.Default.decode(match.value)
        val stringValue = String(decodedBytes)
        stringValue.split(":")[1]
    } else {
        ""
    }
}

fun extractHighlightId(url: String): String {
    val regex = "/(\\d{17})".toRegex()
    val match = regex.find(url)
    return if (match != null) {
        match.groupValues[1]
    } else {
        ""
    }
}

fun buildHighlightLink(highlightId: String): String {
    return "https://www.instagram.com/stories/highlights/${highlightId}/"
}

fun isHighlightLink(url: String): Boolean {
    val regex = Regex("^https://www\\.instagram\\.com/stories/highlights/\\d+/?$")
    return regex.matches(url)
}

fun cleanShareLink(url: String): String {
    return if (url.contains("&igsh=")) url.replace(
        Regex("&igsh=.*"),
        ""
    ) else if (url.contains("?igsh=")) url.replace(Regex("""\?igsh=.*"""), "") else url
}

fun isAudioLink(url: String): Boolean {
    val regex = Regex("""^https://www\.instagram\.com/reels/audio/\d+""")
    return regex.find(url) != null
}

fun shareFile(file: File, context: Context) {
    try {
        val uri =
            FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)

        ShareCompat
            .IntentBuilder(context)
            .setType("*/*")
            .addStream(uri)
            .setChooserTitle(context.resources.getString(R.string.share_via))
            .startChooser()
    } catch (e: Exception) {
        Timber.tag("shareFile").e(e)
    }
}

fun shareFiles(files: ArrayList<File>, context: Context) {
    try {

        val shareIntent = ShareCompat
            .IntentBuilder(context)
            .setType("*/*")
            .setChooserTitle(context.resources.getString(R.string.share_via))

        files.forEach { file ->
            shareIntent.addStream(
                FileProvider.getUriForFile(
                    context,
                    BuildConfig.APPLICATION_ID + ".provider",
                    file
                )
            )
        }
        shareIntent.startChooser()
    } catch (e: Exception) {
        Timber.tag("shareFile").e(e)
    }
}

fun buildInstagramLink(post: PostWithMediaUserAndMedia): String {
    return when (post.post.productType) {
        "highlight_reel" -> {
            val highlightId = post.post.mediaId.split(":")[1]
            "https://www.instagram.com/stories/highlights/${highlightId}"
        }

        "story" -> {
            val storyId = post.post.mediaId.split("-")[0]
            "https://www.instagram.com/stories/${post.mediaUser.username}/${storyId}"
        }

        "carousel_container", "feed", "clips"  -> {
            "https://www.instagram.com/p/${post.post.code}"
        }

        "audio"-> {
            "https://www.instagram.com/reels/audio/${post.post.code}"       // the code for audios is the id
        }


        else -> {
            "https://www.instagram.com/"
        }
    }
}

fun buildInstagramLink(media: Media): String {
    return when (media.productType) {
//        "highlight_reel" -> {
//            val highlightId = media.mediaId.split(":")[1]
//            "https://www.instagram.com/stories/highlights/${highlightId}"
//        }

        "story" -> {
            val storyId = media.id!!.split("-")[0]
            "https://www.instagram.com/stories/${media.owner?.username}/${storyId}"
        }

        "carousel_container", "feed", "clips"  -> {
            "https://www.instagram.com/p/${media.code}"
        }

//        "audio", "clips" -> {
//            "https://www.instagram.com/reels/audio/${media.code}"       // the code for audios is the id
//        }


        else -> {
            "https://www.instagram.com/"
        }
    }
}

fun buildInstagramLink(media: CollectionMedia): String {
    return when (media.productType) {
        "story" -> {
            val storyId = media.id!!.split("-")[0]
            "https://www.instagram.com/stories/${media.owner?.username}/${storyId}"
        }

        "carousel_container", "feed", "clips"  -> {
            "https://www.instagram.com/p/${media.code}"
        }

        else -> {
            "https://www.instagram.com/"
        }
    }
}

fun Double.roundTo(n: Int): Double {
    return String.format(Locale.US, "%.${n}f", this).toDouble()
}

//fun Float.roundTo(n: Int): Float {
//    return "%.${n}f".format(this).toFloat()
//}

fun PostWithMediaUserAndMedia.toPlayerDataModel(): PlayerDataModel {
    return PlayerDataModel.DownloadedMediaItem(this)
}

fun Media.toPlayerDataModel(): PlayerDataModel {
    return PlayerDataModel.ExploreMediaItem(this)
}

fun CollectionMedia.toPlayerDataModel(): PlayerDataModel {
    return PlayerDataModel.CollectionMediaItem(this)
}


fun formatAudioTime(time: Int): String {
    val hours = (time / 1000) / 60 / 60
    val minutes = (time / 1000 / 60) % 60
    val seconds = (time / 1000) % 60

    val hoursValue = if (hours <= 9) {
        "0$hours"
    } else "$hours"

    val minutesValue = if (minutes <= 9) "0$minutes" else "$minutes"
    val secondsValue = if (seconds <= 9) "0$seconds" else "$seconds"
    return if (hours != 0) "$hoursValue:$minutesValue:$secondsValue" else "$minutesValue:$secondsValue"
}

data class AudioModel<A, B, C, D>(
    var mediaPlayer: A,
    var isPrepared: B,
    var isPaused: C,
    var adapterPosition: D,
)

data class CarouselCallbackPair<A, B>(
    var callback: A,
    var position: B
)

data class CarouselAdapterPair<A, B>(
    var adapter: A,
    var position: B,
)

data class VideoPlayerPair<A, B>(
    var mediaPlayer: A,
    var adapterPosition: B,
)

data class AudioJobPair<A, B>(
    var position: A,
    var job: B,
)

data class AdmobNativeAdControllerPair<A, B>(
    var videoController: A,
    var adapterPosition: B,
)


fun getExtension(url: String): String {
    val extension = MimeTypeMap.getFileExtensionFromUrl(url)
    if (extension != null) {
        return extension
    }
    return "mp4"
}

fun <T> insertItemEveryNItems(
    originalList: MutableList<T>,
    newItem: T,
    interval: Int
) : MutableList<T> {
    val resultList = mutableListOf<T>()
    for ((index, item) in originalList.withIndex()) {
        resultList.add(item)
        if ((index + 1) % interval == 0 && index != originalList.lastIndex) {
            resultList.add(newItem)
        }
    }
    return resultList
}