package com.instagram.video.downloader.cases.explore

import com.instagram.video.downloader.data.DataManager
import javax.inject.Inject

class GetUserExploreRefreshLimitExceeded @Inject constructor(private val dataManager: DataManager) {

    operator fun invoke(): Boolean {
        if (dataManager.getExploreRefreshTimes() == 3) {
            return true
        }
        if (System.currentTimeMillis() - dataManager.getExploreLastRefreshAt() <= 20000L) {
            return true
        }
        dataManager.setExploreLastRefreshAt(System.currentTimeMillis())
        dataManager.setExploreRefreshTimes(dataManager.getLikeRefreshTimes() + 1)
        return false
    }


    fun resetRefreshTimes() {
        dataManager.setExploreRefreshTimes(1)
        dataManager.setExploreLastRefreshAt(System.currentTimeMillis())
    }
}