package com.instagram.video.downloader.cases.user

import com.instagram.video.downloader.common.extractCsrfFromCookies
import com.instagram.video.downloader.data.DataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetInstagramUserInfo @Inject constructor(private val dataManager: DataManager) {

    suspend operator fun invoke(userId: String, cookies: String) = withContext(Dispatchers.IO) {
        return@withContext dataManager.getUserInfo(
            userId = userId,
            HashMap<String, Any>().apply {
                set("User-Agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_5 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8L1 Safari/6533.18.5")
                set("Cookie", cookies)
                set("Accept", "application/json")
                set("Content-Type", "application/json")
                set("Accept-Encoding", "*/*")
                set("Accept-Language", "en-US,en;q=0.9,fr-FR;q=0.8,fr;q=0.7")
                set("DNT", "1")
                set("Referer", "https://www.instagram.com/")
                set("x-csrftoken", extractCsrfFromCookies(cookies))

                set("x-ig-app-id", "1217981644879628")
            },
        )
    }

}