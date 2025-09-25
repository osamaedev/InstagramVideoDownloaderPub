package com.instagram.video.downloader.data.local.prefs

import android.content.SharedPreferences
import com.instagram.video.downloader.cases.user.LoggedInUser
import com.instagram.video.downloader.data.remote.api.dto.AccessTokenResponse
import javax.inject.Inject

class PrefsHelper @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : IPrefsHelper {

    companion object {
        private const val APP_ACCESS_TOKEN = "com.instagram.video.Application_Access_Token"
        private const val APP_EXPIRE_IN = "com.instagram.video.Application_Access_Expire_In"
        private const val APP_ACCESS_ISSUED_AT = "com.instagram.video.App_Token_Issued_At"
        private const val USER_UNIQUE_IDENTIFIER = "com.instagram.video.User_Unique_Id"

        private const val IS_INTRODUCTION_DONE = "com.instagram.video.Is_Introduction_Done"

        private const val SUBSCRIPTION_PLAN_TAG = "com.instagram.video.Subscription_Plan_Tag"
        private const val SUBSCRIPTION_STARTED_AT = "com.instagram.video.Subscription_Started_At"
        private const val SUBSCRIPTION_END_AT = "com.instagram.video.Subscription_Expire_At"

        private const val CURRENT_LOGGED_IN_INSTAGRAM_ID =
            "com.instagram.video.Current_Logged_In_Instagram_Id"
        private const val CURRENT_LOGGED_IN_ID = "com.instagram.video.Current_Logged_In_Id"
        private const val CURRENT_LOGGED_IN_COOKIES =
            "com.instagram.video.Current_Logged_In_Cookies"

        private const val EXPLORE_REFRESH_TIMES = "com.instagram.video.Explore_Refresh_Times"
        private const val EXPLORE_REFRESH_AT = "com.instagram.video.Explore_Refresh_At"

        private const val COLLECTION_REFRESH_TIMES = "com.instagram.video.Collection_Refresh_Times"
        private const val COLLECTION_REFRESH_AT = "com.instagram.video.Collection_Refresh_At"

        private const val LIKE_REFRESH_TIMES = "com.instagram.video.Like_Refresh_Times"
        private const val LIKE_REFRESH_AT = "com.instagram.video.Like_Refresh_At"

        private const val CURRENT_LOGGED_IN_USERNAME =
            "com.instagram.video.Current_Logged_In_Username"

        private const val CURRENT_DOWNLOAD_DB_ID = "com.instagram.video.Current_Download_DB_Id"
        private const val CURRENT_POST_DOWNLOAD_DB_ID =
            "com.instagram.video.Current_Post_Download_Db_Id"
        private const val CURRENT_POST_DOWNLOAD_MEDIA_COUNT =
            "com.instagram.video.Current_Download_Media_Count"
        private const val DOWNLOAD_PREVIOUS_PROGRESS =
            "com.instagram.video.Download_Previous_Progress"

        private const val IS_DARK_MODE_ENABLED = "com.instagram.video.Is_DarkMode_Enabled"

        private const val LAST_INTENT_LINK = "com.instagram.video.Last_Intent_Link_For_Download"
        private const val LAST_DOWNLOADED_INTENT_LINK =
            "com.instagram.video.Last_Downloaded_Intent_Link"

        private const val DOWNLOAD_FAKE_DRAG_SHOWN = "Downloaded_Drag_Shown"
        private const val COLLECTION_FAKE_DRAG_SHOWN = "Collection_Drag_Shown"
        private const val EXPLORE_FAKE_DRAG_SHOWN = "Explore_Drag_Shown"
    }

    override fun setExploreFakeDragShown(isShown: Boolean) {
        sharedPreferences.edit().putBoolean(EXPLORE_FAKE_DRAG_SHOWN, isShown).apply()
    }

    override fun getExploreFakeDragShown(): Boolean {
        return sharedPreferences.getBoolean(EXPLORE_FAKE_DRAG_SHOWN, false)
    }

    override fun setCollectionFakeDragShown(isShown: Boolean) {
        sharedPreferences.edit().putBoolean(COLLECTION_FAKE_DRAG_SHOWN, isShown).apply()
    }

    override fun getCollectionFakeDragShown() = sharedPreferences.getBoolean(COLLECTION_FAKE_DRAG_SHOWN, false)

    override fun setDownloadedFakeDragShown(isShown: Boolean) {
        sharedPreferences.edit().putBoolean(DOWNLOAD_FAKE_DRAG_SHOWN, isShown).apply()
    }

    override fun getDownloadFakeDragShown() = sharedPreferences.getBoolean(DOWNLOAD_FAKE_DRAG_SHOWN, false)

    override fun getIntentLinkToDownload(): String {
        sharedPreferences.getString(LAST_INTENT_LINK, "")?.let {
            return it
        }
        return ""
    }

    override fun setIntentLinkToDownload(link: String) {
        sharedPreferences.edit().putString(LAST_INTENT_LINK, link).apply()
    }

    override fun setIsDarkModeEnabled(isDarkModeEnabled: Boolean) {
        sharedPreferences.edit().putBoolean(IS_DARK_MODE_ENABLED, isDarkModeEnabled).apply()
    }

    override fun getIsDarkModeEnabled() = sharedPreferences.getBoolean(IS_DARK_MODE_ENABLED, false)

    override fun getCurrentLoggedInUser(): LoggedInUser? {
        val currentInstagramId = sharedPreferences.getString(CURRENT_LOGGED_IN_INSTAGRAM_ID, "")
        val currentId = sharedPreferences.getLong(CURRENT_LOGGED_IN_ID, -1L)
        if (!currentInstagramId.isNullOrEmpty() && currentId != -1L) return LoggedInUser(
            username = sharedPreferences.getString(CURRENT_LOGGED_IN_USERNAME, "")!!,
            instagramId = sharedPreferences.getString(CURRENT_LOGGED_IN_INSTAGRAM_ID, "")!!,
            id = sharedPreferences.getLong(CURRENT_LOGGED_IN_ID, -1L),
            cookies = sharedPreferences.getString(CURRENT_LOGGED_IN_COOKIES, "")!!
        )
        return null
    }

    override fun setPreviousDownloadProgress(previousProgress: Double) {
        sharedPreferences.edit().putFloat(DOWNLOAD_PREVIOUS_PROGRESS, previousProgress.toFloat())
            .apply()
    }

    override fun getPreviousDownloadProgress(): Double {
        return sharedPreferences.getFloat(DOWNLOAD_PREVIOUS_PROGRESS, 0.0f).toDouble()
    }

    override fun setCurrentPostDownloadMediaCount(currentMediaCount: Int) {
        sharedPreferences.edit().putInt(CURRENT_POST_DOWNLOAD_MEDIA_COUNT, currentMediaCount)
            .apply()
    }

    override fun getCurrentPostDownloadMediaCount(): Int {
        return sharedPreferences.getInt(CURRENT_POST_DOWNLOAD_MEDIA_COUNT, 0)
    }

    override fun setCurrentDownloadDbId(currentDownloadDbId: Long) {
        sharedPreferences.edit().putLong(CURRENT_DOWNLOAD_DB_ID, currentDownloadDbId).apply()
    }

    override fun getCurrentDownloadDbId(): Long {
        return sharedPreferences.getLong(CURRENT_DOWNLOAD_DB_ID, -1L)
    }

    override fun setCurrentPostDownloadDbId(currentPostDownloadDbId: Long) {
        sharedPreferences.edit().putLong(CURRENT_POST_DOWNLOAD_DB_ID, currentPostDownloadDbId)
            .apply()
    }

    override fun getCurrentPostDownloadDbId(): Long {
        return sharedPreferences.getLong(CURRENT_POST_DOWNLOAD_DB_ID, -1L)
    }

    // region [Limit Handling]

    override fun setExploreRefreshTimes(times: Int) {
        sharedPreferences.edit().putInt(EXPLORE_REFRESH_TIMES, times).apply()
    }

    override fun setExploreLastRefreshAt(refreshAt: Long) {
        sharedPreferences.edit().putLong(EXPLORE_REFRESH_AT, refreshAt).apply()
    }

    override fun getExploreLastRefreshAt(): Long {
        return sharedPreferences.getLong(EXPLORE_REFRESH_AT, 0L)
    }

    override fun getExploreRefreshTimes(): Int {
        return sharedPreferences.getInt(EXPLORE_REFRESH_TIMES, 0)
    }

    override fun setCollectionRefreshTimes(times: Int) {
        sharedPreferences.edit().putInt(COLLECTION_REFRESH_TIMES, times).apply()
    }

    override fun setCollectionLastRefreshAt(refreshAt: Long) {
        sharedPreferences.edit().putLong(COLLECTION_REFRESH_AT, refreshAt).apply()
    }

    override fun getCollectionLastRefreshAt(): Long {
        return sharedPreferences.getLong(COLLECTION_REFRESH_AT, 0L)
    }

    override fun getCollectionRefreshTimes(): Int {
        return sharedPreferences.getInt(COLLECTION_REFRESH_TIMES, 0)
    }

    override fun setLikeRefreshTimes(times: Int) {
        sharedPreferences.edit().putInt(LIKE_REFRESH_TIMES, times).apply()
    }

    override fun setLikeLastRefreshAt(refreshAt: Long) {
        sharedPreferences.edit().putLong(LIKE_REFRESH_AT, refreshAt).apply()
    }

    override fun getLikeLastRefreshAt(): Long {
        return sharedPreferences.getLong(LIKE_REFRESH_AT, 0L)
    }

    override fun getLikeRefreshTimes(): Int {
        return sharedPreferences.getInt(LIKE_REFRESH_TIMES, 0)
    }

    // endregion

    override fun setCurrentLoggedInUser(loggedInUser: LoggedInUser) {
        sharedPreferences.edit().putString(CURRENT_LOGGED_IN_USERNAME, loggedInUser.username)
            .apply()
        sharedPreferences.edit().putLong(CURRENT_LOGGED_IN_ID, loggedInUser.id).apply()
        sharedPreferences.edit().putString(CURRENT_LOGGED_IN_INSTAGRAM_ID, loggedInUser.instagramId)
            .apply()
        sharedPreferences.edit().putString(CURRENT_LOGGED_IN_COOKIES, loggedInUser.cookies).apply()
    }

    override fun setIsIntroductionDone(done: Boolean) {
        sharedPreferences.edit().putBoolean(IS_INTRODUCTION_DONE, done).apply()
    }

    override fun getIsIntroductionDone(): Boolean {
        return sharedPreferences.getBoolean(IS_INTRODUCTION_DONE, false)
    }

    override fun setCurrentUserPlanTag(planTag: String) {
        sharedPreferences.edit().putString(SUBSCRIPTION_PLAN_TAG, planTag).apply()
    }

    override fun getCurrentUserPlanTag(): String {
        sharedPreferences.getString(SUBSCRIPTION_PLAN_TAG, "")?.let {
            return it
        }
        return ""
    }

    override fun setSubscriptionStartedAt(startedAt: Long) {
        sharedPreferences.edit().putLong(SUBSCRIPTION_STARTED_AT, startedAt).apply()
    }

    override fun getSubscriptionStartedAt() =
        sharedPreferences.getLong(SUBSCRIPTION_STARTED_AT, -1L)

    override fun setSubscriptionExpireAt(expireAt: Long) {
        sharedPreferences.edit().putLong(SUBSCRIPTION_END_AT, expireAt).apply()
    }

    override fun getSubscriptionExpireAt() = sharedPreferences.getLong(SUBSCRIPTION_END_AT, -1L)

    override fun setApplicationAccessToken(accessToken: AccessTokenResponse) {
        sharedPreferences.edit().putString(APP_ACCESS_TOKEN, accessToken.accessToken).apply()
        sharedPreferences.edit().putLong(APP_EXPIRE_IN, accessToken.expiresIn).apply()
    }

    override fun getApplicationAccessToken(): AccessTokenResponse? {
        sharedPreferences.getString(APP_ACCESS_TOKEN, "")?.let {
            return AccessTokenResponse(
                "Bearer",
                sharedPreferences.getLong(APP_EXPIRE_IN, 0L),
                "Bearer $it",
                ""
            )
        }
        return null
    }

    override fun setApplicationAccessTokenIssuedAt(issuedAt: Long) {
        sharedPreferences.edit().putLong(APP_ACCESS_ISSUED_AT, issuedAt).apply()
    }

    override fun getApplicationAccessTokenIssuedAt(): Long {
        return sharedPreferences.getLong(APP_ACCESS_ISSUED_AT, 0L)
    }

    override fun signOutUser() {
        sharedPreferences.edit().putString(USER_UNIQUE_IDENTIFIER, "").apply()
        sharedPreferences.edit().putString(SUBSCRIPTION_PLAN_TAG, "").apply()
        sharedPreferences.edit().putLong(SUBSCRIPTION_STARTED_AT, -1L).apply()
        sharedPreferences.edit().putLong(SUBSCRIPTION_END_AT, -1L).apply()
    }
}