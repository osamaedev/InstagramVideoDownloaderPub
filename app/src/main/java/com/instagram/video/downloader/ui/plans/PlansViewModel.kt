package com.instagram.video.downloader.ui.plans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.Purchase
import com.instagram.video.downloader.R
import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.cases.plans.GetPlansUseCase
import com.instagram.video.downloader.cases.user.LoggedInUser
import com.instagram.video.downloader.cases.user.SaveUserSubscriptionCase
import com.instagram.video.downloader.cases.user.SingUpOrGetRemoteUser
import com.instagram.video.downloader.cases.user.UpdateRemoteUser
import com.instagram.video.downloader.cases.version.CheckCurrentVersion
import com.instagram.video.downloader.common.getId
import com.instagram.video.downloader.common.isNetworkConnected
import com.instagram.video.downloader.data.remote.api.dto.AppConfig
import com.instagram.video.downloader.data.remote.api.dto.PlanDto
import com.instagram.video.downloader.data.remote.api.dto.UserDto
import com.instagram.video.downloader.data.remote.api.dto.toUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class PlansViewModel @Inject constructor(
    private val getPlansUseCase: GetPlansUseCase,
    private val signUpOrGetRemoteUser: SingUpOrGetRemoteUser,


    private val updateRemoteUser: UpdateRemoteUser,
    private val currentVersion: CheckCurrentVersion,

    private val saveUserSubscriptionCase: SaveUserSubscriptionCase,

    ) : ViewModel() {


    var currentRemoteUser: UserDto? = null
    var appConfig = MutableStateFlow<AppConfig?>(null)
    val plans = MutableStateFlow<List<PlanDto>>(arrayListOf())


    private val _plansActions = MutableSharedFlow<PlansAction>()
    var plansAction: SharedFlow<PlansAction> = _plansActions

    private val _plansState = MutableStateFlow(PlansState())
    val plansState = _plansState.asStateFlow()

    init {
        _plansState.update {
            it.copy(
                isPlansLoading = true,
                isNetworkAvailable = isNetworkConnected(VideoDownloaderApp.application),
                isGoogleBillingSetupDone = false
            )
        }
    }

    /**
     * This method is called only when the google billing client
     * is set.
     */
    fun startSubscriptionsProcess() {
        viewModelScope.launch {
            currentRemoteUser = signUpOrGetRemoteUser.invoke(getId(VideoDownloaderApp.application))
            if (currentRemoteUser?.subscription != null) {
                currentRemoteUser?.subscription = updateRemoteUser.updateUserSubscription(
                    currentRemoteUser?.subscription!!,
                    currentRemoteUser?.uniqueId!!
                )
            }
            appConfig.value = currentVersion.invoke().appConfig
            plans.value = getPlansUseCase.invoke()
            _plansState.update {
                it.copy(
                    currentUser = currentRemoteUser?.toUser(),
                    isPlansLoading = false,
                    error = null,
                    isGoogleBillingSetupDone = true
                )
            }
        }
    }


    fun pricesInitDone() {
        _plansState.update {
            it.copy(
                isPlansLoading = false,
                error = null,
                isGoogleBillingSetupDone = true
            )
        }
    }

    fun saveUserSubscription(purchase: Purchase, planTagName: String) {
        _plansState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            try {
                saveUserSubscriptionCase
                    .invoke(
                        androidId = getId(VideoDownloaderApp.application),
                        planTagName = planTagName,
                        purchase = purchase,
                        userId = currentRemoteUser?.uniqueId!!
                    )
                _plansState.update { it.copy(isLoading = false) }
                _plansActions.emit(PlansAction.OnShowSubscribedDialog)
            } catch (e: Exception) {
                _plansState.update { it.copy(isLoading = false, error = R.string.contact_support) }
                Timber.tag(PlansActivity.TAG).e(e)
            }
        }
    }

}