package com.instagram.video.downloader.cases.collection

import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.cases.user.LoggedInUser
import com.instagram.video.downloader.common.extractCsrfFromCookies
import com.instagram.video.downloader.R
import com.instagram.video.downloader.data.DataManager
import javax.inject.Inject

class GetUserCollectionUseCase @Inject constructor(private val dataManager: DataManager) {
    suspend operator fun invoke(
        loggedInUser: LoggedInUser,
        pageAfter: String = "",
    ) = dataManager.getUserCollections(
        headers = HashMap<String, Any>().apply {
            set(
                "User-Agent",
                VideoDownloaderApp.application.resources.getString(R.string.web_agent)
            )
            set("Cookie", loggedInUser.cookies)
            set(
                "Accept",
                "*/*"
            )
            set("Content-Type", "application/json")
            set("Accept-Encoding", "*/*")
            set("Accept-Language", "en-US,en;q=0.9,fr-FR;q=0.8,fr;q=0.7")
            set("DNT", "1")
            set("Referer", "https://www.instagram.com/")
            set("x-csrftoken", extractCsrfFromCookies(loggedInUser.cookies))
            set("x-ig-app-id", "1217981644879628")
            set("dpr", "1")
            set("origin", "https://www.instagram.com")
            set("sec-fetch-mode", "cors")
            set("sec-fetch-site", "same-origin")
        },
        params = HashMap<String, Any>().apply {
            set("max_id", pageAfter)
        }
    )
}