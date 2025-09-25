package com.instagram.video.downloader.common.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.instagram.video.downloader.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp

@Composable
fun VideoDownloaderSettingsPage(
    modifier: Modifier = Modifier,
    onHowToDownloadClick: () -> Unit,
    onFaqClick: () -> Unit,
    onShareClick: () -> Unit,
    onPrivacyClick: () -> Unit,
) {
    SettingsMenuItem(
        icon = {
            Icon(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(40.dp, 40.dp),
                imageVector = ImageVector.vectorResource(R.drawable.settings_how_to_download),
                contentDescription = stringResource(id = R.string.how_to_download),
            )
        },
        title = { Text(text = stringResource(id = R.string.how_to_download)) },
        subtitle = { Text(text = stringResource(id = R.string.how_to_download_subtitle)) },
        onClick = onHowToDownloadClick
    )


    SettingsMenuItem(
        icon = { ImageVector.vectorResource(R.drawable.settings_faq) },
        title = { Text(text = stringResource(id = R.string.settings_faq)) },
        onClick = onFaqClick,
    )

    SettingsMenuItem(
        icon = { ImageVector.vectorResource(R.drawable.settings_share) },
        title = {
            Text(
                text = stringResource(
                    id = R.string.settings_share,
                    LocalContext.current.resources.getString(R.string.app_name)
                )
            )
        },
        onClick = onShareClick
    )

    SettingsMenuItem(
        icon = { ImageVector.vectorResource(R.drawable.settings_privacy) },
        title = { Text(text = stringResource(id = R.string.settings_privacy)) },
        onClick = onPrivacyClick,
    )
}

@Composable
fun SettingsMenuItem(
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
) {
    Surface {
//        SettingsMenuLink(
//            enabled = enabled,
//            icon = icon,
//            title = title,
//            subtitle = subtitle,
//            action = action,
//            onClick = onClick,
//            tonalElevation = 5.dp,
//            shadowElevation = 5.dp,
//        )
    }
}