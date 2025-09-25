package com.instagram.video.downloader.cases.user

import com.instagram.video.downloader.data.DataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CurrentLoggedInUserUseCase @Inject constructor(private val dataManager: DataManager) {

    suspend operator fun invoke(): LoggedInUser {
        return withContext(Dispatchers.IO) {
            val currentUser = dataManager.getCurrentLoggedInUser()
            if (currentUser != null) {
                return@withContext checkCurrentUserCookieExpiration(currentUser)
            }
            return@withContext LoggedInUser(
                username = "insta_saver_default",
                instagramId = "default",
                id = -1L,
                cookies = "",
                isCookiesExpired = false
            )
        }
    }

    fun setCurrentLoggedInUser(loggedInUser: LoggedInUser?) {
        if (loggedInUser == null)
            dataManager.setCurrentLoggedInUser(
                LoggedInUser(
                    username = "insta_saver_default",
                    instagramId = "default",
                    id = -1L,
                    cookies = "",
                    isCookiesExpired = false
                )
            )
        else
            dataManager.setCurrentLoggedInUser(loggedInUser)
    }


    private suspend fun checkCurrentUserCookieExpiration(userToCheck: LoggedInUser): LoggedInUser =
        withContext(Dispatchers.IO) {
            val dbUser = dataManager.getUserByIdentifier(userToCheck.id)
            userToCheck.isCookiesExpired = dbUser.cookiesExpiringAt < System.currentTimeMillis()
            userToCheck.cookies = dbUser.cookies
            return@withContext userToCheck
        }
}


data class LoggedInUser(
    val username: String,
    val instagramId: String,
    val id: Long,
    var cookies: String,
    var isCookiesExpired: Boolean = false,
)