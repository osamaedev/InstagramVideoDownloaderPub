package com.instagram.video.downloader.ui.splash

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instagram.video.downloader.R
import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.cases.appConfig.GetAppConfigUseCase
import com.instagram.video.downloader.cases.general.CheckAppAccessTokenUseCase
import com.instagram.video.downloader.cases.user.SingUpOrGetRemoteUser
import com.instagram.video.downloader.common.getId
import com.instagram.video.downloader.common.isNetworkConnected
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class SplashViewModel @Inject constructor(
    private val checkAppAccessTokenUseCase: CheckAppAccessTokenUseCase,
    private val singUpOrGetRemoteUser: SingUpOrGetRemoteUser,
    private val getAppConfigUseCase: GetAppConfigUseCase,
    state: SavedStateHandle,
) : ViewModel() {


    private var _state = MutableStateFlow(SplashState())
    val state = _state.asStateFlow()

    init {
        if (isNetworkConnected(VideoDownloaderApp.application)) {
            checkTokenAndSubscription()
        } else {
            _state.update {
                it.copy(
                    isUserAndAppCheckDone = true,
                    appConfig = null,
                    isLoading = false
                )
            }
        }
    }


    private fun checkTokenAndSubscription() {
        viewModelScope.launch {
            try {
                if (!isNetworkConnected(VideoDownloaderApp.application)) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isNetworkAvailable = false,
                            isUserAndAppCheckDone = true,
                        )
                    }
                    return@launch
                }
                _state.update {
                    it.copy(
                        isLoading = true,
                        isNetworkAvailable = isNetworkConnected(VideoDownloaderApp.application)
                    )
                }
                val accessTokenDone = checkAppAccessTokenUseCase.invoke()
                val user = singUpOrGetRemoteUser.invoke(getId(VideoDownloaderApp.application))
                val appConfig = getAppConfigUseCase.invoke()
                if (user == null) {
                    Timber.tag("SplashViewModel")
                        .i("User is null from use case check other errors.")
                    _state.update { it.copy(error = R.string.something_went_wrong) }
                }
                _state.update {
                    it.copy(
                        isUserAndAppCheckDone = accessTokenDone && user != null,
                        appConfig = appConfig,
                        isLoading = false,
                        user = user,
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = R.string.something_went_wrong, isLoading = false) }
                Timber.tag(SplashViewModel::class.simpleName.toString()).e(e)
            }
        }
    }

}