package com.instagram.video.downloader.cases.user

import com.instagram.video.downloader.data.DataManager
import javax.inject.Inject

class SubscriptionLocalInfo @Inject constructor(private val dataManager: DataManager) {


    operator fun invoke() = LocalSubscriptionInfo(
        planTag = dataManager.getCurrentUserPlanTag(),
        startedAt = dataManager.getSubscriptionStartedAt(),
        expiringAt = dataManager.getSubscriptionExpireAt(),
    )

    data class LocalSubscriptionInfo(
        val planTag: String,
        val startedAt: Long,
        val expiringAt: Long,
    )

}