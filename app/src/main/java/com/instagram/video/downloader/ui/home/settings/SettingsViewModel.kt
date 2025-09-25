package com.instagram.video.downloader.ui.home.settings

import androidx.lifecycle.ViewModel
import com.instagram.video.downloader.cases.general.SetDarkModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val setDarkModeUseCase: SetDarkModeUseCase,
) : ViewModel() {


    fun setIsDarkModeEnabled(isDarkModeEnabled: Boolean) {
        setDarkModeUseCase.invoke(isDarkModeEnabled)
    }


}