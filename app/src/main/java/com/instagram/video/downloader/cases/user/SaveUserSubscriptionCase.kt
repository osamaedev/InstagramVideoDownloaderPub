package com.instagram.video.downloader.cases.user

import com.android.billingclient.api.Purchase
import com.instagram.video.downloader.data.DataManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SaveUserSubscriptionCase @Inject constructor(private val dataManager: DataManager) {

    suspend fun invoke(
        androidId: String,
        planTagName: String,
        purchase: Purchase,
        userId: String
    ) = withContext(Dispatchers.IO) {
        val subscription = dataManager.saveUserSubscription(
            accessToken = dataManager.getApplicationAccessToken()?.accessToken!!,
            userUniqueId = userId,
            data = HashMap<String, Any>().apply {
                set("plan_tag_name", planTagName)
                set("google_play_token", purchase.purchaseToken)
                set("developer_payload", purchase.developerPayload)
                set("order_id", purchase.orderId!!)
                set("is_auto_renewing", value = if (purchase.isAutoRenewing) 1 else 0)
                set("android_id", androidId)
            }
        )
        dataManager.setCurrentUserPlanTag(planTagName)
        dataManager.setSubscriptionStartedAt(subscription.startTime)
        dataManager.setSubscriptionExpireAt(subscription.expiryTime)
        return@withContext subscription
    }

}