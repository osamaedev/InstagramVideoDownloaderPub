package com.instagram.video.downloader.cases.general

import com.instagram.video.downloader.data.DataManager
import javax.inject.Inject

class SetDarkModeUseCase @Inject constructor(private val dataManager: DataManager) {

    operator fun invoke(isDarkModeEnabled: Boolean) {
        dataManager.setIsDarkModeEnabled(isDarkModeEnabled)
    }

}