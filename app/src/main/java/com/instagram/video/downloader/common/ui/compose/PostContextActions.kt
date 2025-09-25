package com.instagram.video.downloader.common.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.instagram.video.downloader.R
import com.instagram.video.downloader.data.local.room.entities.PostWithMediaUserAndMedia
import com.instagram.video.downloader.data.remote.instaApi.dto.collections.CollectionMedia
import com.instagram.video.downloader.data.remote.instaApi.dto.explore.Media


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadedPostPlayerActions(
    selectedPost: MutableState<PostWithMediaUserAndMedia?>,
    onShare: (PostWithMediaUserAndMedia) -> Unit,
    onOpenInInstagram: (PostWithMediaUserAndMedia) -> Unit,
) {

    val modalSheetState = rememberModalBottomSheetState(true)
    val haptic = LocalHapticFeedback.current

    val post = selectedPost.value

    LaunchedEffect(post) {
        if (post != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            modalSheetState.show()
        } else {
            modalSheetState.hide()
            selectedPost.value = null
        }
    }

    FakeScrim(modalSheetState)

    if (post != null) {
        ModalBottomSheet(
            sheetState = modalSheetState,
            onDismissRequest = { selectedPost.value = null },
            scrimColor = Color.Transparent,
        ) {
            DownloadedPostPlayerActionContent(
                post,
                selectedPost,
                onShare,
                onOpenInInstagram
            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionMediaPlayerActions(
    selectedPost: MutableState<CollectionMedia?>,
    onShare: (CollectionMedia) -> Unit,
    onOpenInInstagram: (CollectionMedia) -> Unit,
) {

    val modalSheetState = rememberModalBottomSheetState(true)
    val haptic = LocalHapticFeedback.current

    val post = selectedPost.value

    LaunchedEffect(post) {
        if (post != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            modalSheetState.show()
        } else {
            modalSheetState.hide()
            selectedPost.value = null
        }
    }

    FakeScrim(modalSheetState)

    if (post != null) {
        ModalBottomSheet(
            sheetState = modalSheetState,
            onDismissRequest = { selectedPost.value = null },
            scrimColor = Color.Transparent,
        ) {
            CollectionPostPlayerActionContent(
                post,
                selectedPost,
                onShare,
                onOpenInInstagram
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreMediaPlayerActions(
    selectedPost: MutableState<Media?>,
    onShare: (Media) -> Unit,
    onOpenInInstagram: (Media) -> Unit,
) {

    val modalSheetState = rememberModalBottomSheetState(true)
    val haptic = LocalHapticFeedback.current

    val post = selectedPost.value

    LaunchedEffect(post) {
        if (post != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            modalSheetState.show()
        } else {
            modalSheetState.hide()
            selectedPost.value = null
        }
    }

    FakeScrim(modalSheetState)

    if (post != null) {
        ModalBottomSheet(
            sheetState = modalSheetState,
            onDismissRequest = { selectedPost.value = null },
            scrimColor = Color.Transparent,
        ) {
            ExplorePostPlayerActionContent(
                post,
                selectedPost,
                onShare,
                onOpenInInstagram
            )
        }
    }
}

@Composable
private fun ExplorePostPlayerActionContent(
    selectedPost: Media,
    selectedPostState: MutableState<Media?>,
    onShare: (Media) -> Unit,
    onOpenInInstagram: (Media) -> Unit,
) {
    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Bottom)),
    ) {
        ContextActionEntry(
            label = stringResource(id = R.string.word_share),
            icon = ImageVector.vectorResource(R.drawable.ic_share),
            onClick = {
                onShare(selectedPost)
                selectedPostState.value = null
            },
        )
        ContextActionEntry(
            label = stringResource(id = R.string.open_in_instagram),
            icon = ImageVector.vectorResource(R.drawable.ic_instagram_material)
        ) {
            onOpenInInstagram(selectedPost)
            selectedPostState.value = null
        }
    }
}

@Composable
private fun CollectionPostPlayerActionContent(
    selectedPost: CollectionMedia,
    selectedPostState: MutableState<CollectionMedia?>,
    onShare: (CollectionMedia) -> Unit,
    onOpenInInstagram: (CollectionMedia) -> Unit,
) {
    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Bottom)),
    ) {
        ContextActionEntry(
            label = stringResource(id = R.string.word_share),
            icon = ImageVector.vectorResource(R.drawable.ic_share),
            onClick = {
                onShare(selectedPost)
                selectedPostState.value = null
            },
        )
        ContextActionEntry(
            label = stringResource(id = R.string.open_in_instagram),
            icon = ImageVector.vectorResource(R.drawable.ic_instagram_material)
        ) {
            onOpenInInstagram(selectedPost)
            selectedPostState.value = null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostContextActions(
    selectedPost: MutableState<PostWithMediaUserAndMedia?>,
    onShare: (PostWithMediaUserAndMedia) -> Unit,
    onShowFileLocation: (PostWithMediaUserAndMedia) -> Unit,
    onRename: (PostWithMediaUserAndMedia) -> Unit,
    onOpenInInstagram: (PostWithMediaUserAndMedia) -> Unit,
    onDelete: (PostWithMediaUserAndMedia) -> Unit,
) {
    val modalSheetState = rememberModalBottomSheetState(true)
    val haptic = LocalHapticFeedback.current

    val post = selectedPost.value

    LaunchedEffect(post) {
        if (post != null) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            modalSheetState.show()
        } else {
            modalSheetState.hide()
            selectedPost.value = null
        }
    }

    FakeScrim(modalSheetState)

    if (post != null) {
        ModalBottomSheet(
            sheetState = modalSheetState,
            onDismissRequest = { selectedPost.value = null },
            scrimColor = Color.Transparent,
        ) {
            ContextActionContent(
                post,
                selectedPost,
                onShare,
                onShowFileLocation,
                onRename,
                onOpenInInstagram,
                onDelete
            )
        }
    }
}


@Composable
private fun DownloadedPostPlayerActionContent(
    selectedPost: PostWithMediaUserAndMedia,
    selectedPostState: MutableState<PostWithMediaUserAndMedia?>,
    onShare: (PostWithMediaUserAndMedia) -> Unit,
    onOpenInInstagram: (PostWithMediaUserAndMedia) -> Unit,
) {
    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Bottom)),
    ) {
        ContextActionEntry(
            label = stringResource(id = R.string.word_share),
            icon = ImageVector.vectorResource(R.drawable.ic_share),
            onClick = {
                onShare(selectedPost)
                selectedPostState.value = null
            },
        )
        ContextActionEntry(
            label = stringResource(id = R.string.open_in_instagram),
            icon = ImageVector.vectorResource(R.drawable.ic_instagram_material)
        ) {
            onOpenInInstagram(selectedPost)
            selectedPostState.value = null
        }
    }
}

@Composable
private fun ContextActionContent(
    selectedPost: PostWithMediaUserAndMedia,
    selectedPostState: MutableState<PostWithMediaUserAndMedia?>,
    onShare: (PostWithMediaUserAndMedia) -> Unit,
    onShowFileLocation: (PostWithMediaUserAndMedia) -> Unit,
    onRename: (PostWithMediaUserAndMedia) -> Unit,
    onOpenInInstagram: (PostWithMediaUserAndMedia) -> Unit,
    onDelete: (PostWithMediaUserAndMedia) -> Unit,
) {
    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeContent.only(WindowInsetsSides.Bottom)),
    ) {
        ContextActionHeader(post = selectedPost)
        HorizontalDivider()
        ContextActionEntry(
            label = stringResource(id = R.string.word_share),
            icon = ImageVector.vectorResource(R.drawable.ic_share),
            onClick = {
                onShare(selectedPost)
                selectedPostState.value = null
            },
        )

        ContextActionEntry(
            label = stringResource(id = R.string.file_location),
            icon = ImageVector.vectorResource(R.drawable.ic_file_location)
        ) {
            onShowFileLocation(selectedPost)
            selectedPostState.value = null
        }

        if (selectedPost.media.size == 1) {
//            ContextActionEntry(
//                label = stringResource(id = R.string.rename_file),
//                icon = ImageVector.vectorResource(R.drawable.ic_rename)
//            ) {
//                onRename(selectedPost)
//                selectedPostState.value = null
//            }
        }

        ContextActionEntry(
            label = stringResource(id = R.string.open_in_instagram),
            icon = ImageVector.vectorResource(R.drawable.ic_instagram_material)
        ) {
            onOpenInInstagram(selectedPost)
            selectedPostState.value = null
        }

        ContextActionEntry(
            label = stringResource(id = R.string.word_delete),
            icon = ImageVector.vectorResource(R.drawable.ic_delete_post)
        ) {
            onDelete(selectedPost)
            selectedPostState.value = null
        }
    }
}


@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ContextActionHeader(post: PostWithMediaUserAndMedia) {
    Row(
        modifier =
        Modifier.padding(
            start = 16.dp,
            top = 8.dp,
            bottom = 8.dp,
            end = 16.dp,
        ),
    ) {
        GlideImage(
            post.post.thumbnailUrl,
            contentDescription = "",
            modifier = Modifier
                .width(50.dp)
                .height(50.dp)
                .align(Alignment.CenterVertically)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
                .padding(8.dp),
        ) {
            Text(
                text = post.post.mediaId,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ContextActionEntry(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.padding(start = 16.dp),
            imageVector = icon,
            contentDescription = label,
        )
        Text(
            modifier = Modifier.padding(start = 16.dp),
            fontWeight = FontWeight.Bold,
            text = label,
        )
    }
}


@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FakeScrim(modalSheetState: SheetState) {
    AnimatedVisibility(
        visible = modalSheetState.targetValue != SheetValue.Hidden,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Box(
            modifier =
            Modifier
                .fillMaxSize()
                .background(BottomSheetDefaults.ScrimColor),
        )
    }
}