package com.instagram.video.downloader.cases.explore

import com.instagram.video.downloader.cases.user.LoggedInUser
import com.instagram.video.downloader.common.extractCsrfFromCookies
import com.instagram.video.downloader.data.DataManager
import javax.inject.Inject

class GetUserExploreUseCase @Inject constructor(private val dataManager: DataManager) {

    suspend operator fun invoke(
        loggedInUser: LoggedInUser,
        pageIndex: String = "",
    ) = dataManager.getUserExplore(
        headers = HashMap<String, Any>().apply {
            set(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36"
            )
            set("Cookie", loggedInUser.cookies)
            set(
                "Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"
            )
            set("Content-Type", "application/json")
            set("Accept-Encoding", "*/*")
            set("Accept-Language", "en-US,en;q=0.9,fr-FR;q=0.8,fr;q=0.7")
            set("DNT", "1")
            set("Referer", "https://www.instagram.com/")
            set("x-csrftoken", extractCsrfFromCookies(loggedInUser.cookies))
            set("x-ig-app-id", "1217981644879628")
        },
        queryMap = HashMap<String, Any>().apply {
            HashMap<String, Any>().apply {
                set("is_prefetch", false)
                set("omit_cover_media", false)
                set("module", "explore_popular")
                set("use_sectional_payload", true)
                set("include_fixed_destinations", true)
                set("is_nonpersonalized_explore", false)
                set("max_id", pageIndex)
            }
        }
    )

}