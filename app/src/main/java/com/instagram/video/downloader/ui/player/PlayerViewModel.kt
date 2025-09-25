package com.instagram.video.downloader.ui.player

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instagram.video.downloader.R
import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.cases.general.FakeDragCases
import com.instagram.video.downloader.cases.media.DownloadPostByIdUseCase
import com.instagram.video.downloader.cases.media.GetAudioInfoUseCase
import com.instagram.video.downloader.cases.user.CurrentLoggedInUserUseCase
import com.instagram.video.downloader.cases.user.GetLocalPostByInstaId
import com.instagram.video.downloader.cases.user.InsertPostUseCase
import com.instagram.video.downloader.cases.user.LoggedInUser
import com.instagram.video.downloader.cases.user.SingUpOrGetRemoteUser
import com.instagram.video.downloader.cases.user.SubscriptionLocalInfo
import com.instagram.video.downloader.cases.user.UpdateRemoteUser
import com.instagram.video.downloader.cases.version.CheckCurrentVersion
import com.instagram.video.downloader.common.Response
import com.instagram.video.downloader.common.buildInstagramLink
import com.instagram.video.downloader.common.download.MediaType
import com.instagram.video.downloader.common.getId
import com.instagram.video.downloader.common.isNetworkConnected
import com.instagram.video.downloader.data.local.room.entities.MediaToDownload
import com.instagram.video.downloader.data.local.room.entities.PostToDownload
import com.instagram.video.downloader.data.local.room.entities.PostWithMediaUserAndMedia
import com.instagram.video.downloader.data.remote.api.dto.AppConfig
import com.instagram.video.downloader.data.remote.api.dto.AudioResponse
import com.instagram.video.downloader.data.remote.api.dto.PostResponse
import com.instagram.video.downloader.data.remote.api.dto.UserDto
import com.instagram.video.downloader.data.remote.api.dto.hasValidSubscription
import com.instagram.video.downloader.data.remote.api.dto.toUser
import com.instagram.video.downloader.data.remote.instaApi.dto.collections.CollectionMedia
import com.instagram.video.downloader.data.remote.instaApi.dto.explore.Media
import com.instagram.video.downloader.data.remote.utils.GeneralErrorResponse
import com.instagram.video.downloader.ui.home.HomeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val currentLoggedInUserUseCase: CurrentLoggedInUserUseCase,
    private val signUpOrGetRemoteUser: SingUpOrGetRemoteUser,
    private val checkCurrentVersion: CheckCurrentVersion,

    private val downloadPostByIdUseCase: DownloadPostByIdUseCase,
    private val getAudioInfoUseCase: GetAudioInfoUseCase,
    private val insertPostUseCase: InsertPostUseCase,

    private val getLocalPostByInstaId: GetLocalPostByInstaId,

    private val fakeDragCases: FakeDragCases,


    private val updateRemoteUser: UpdateRemoteUser,
    private val subscriptionLocalInfo: SubscriptionLocalInfo,
) : ViewModel() {

    companion object {
        const val TAG = "PlayerViewModel"
    }

    lateinit var currentLoggedInUser: MutableStateFlow<LoggedInUser>
    var currentRemoteUser: UserDto? = null
    var appConfig = MutableStateFlow<AppConfig?>(null)

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState = _playerState.asStateFlow()

    private val internetCheckDelay = 5000L


    val selectedDownloadedPost = mutableStateOf<PostWithMediaUserAndMedia?>(null)
    val selectedExplorePost = mutableStateOf<Media?>(null)
    val selectedCollectionMedia = mutableStateOf<CollectionMedia?>(null)

    private val _playerChannel = MutableSharedFlow<PlayerAction>()
    val playerChannel: SharedFlow<PlayerAction> = _playerChannel

    init {
        viewModelScope.launch {
            try {
                val currentUser = currentLoggedInUserUseCase.invoke()
                currentLoggedInUser = MutableStateFlow(currentUser)

                while (true) {
                    _playerState.update {
                        it.copy(
                            isNetworkAvailable = isNetworkConnected(VideoDownloaderApp.application)
                        )
                    }
                    delay(internetCheckDelay)
                }

            } catch (e: Exception) {
                Timber.tag(TAG).e(e)
            }
        }
    }


    fun getRemoteUser() {
        viewModelScope.launch {
            currentRemoteUser =
                signUpOrGetRemoteUser.invoke(getId(VideoDownloaderApp.application))
            val currentVersion = checkCurrentVersion.invoke()
            if (appConfig.value == null)
                appConfig.value = currentVersion.appConfig
            currentRemoteUser = signUpOrGetRemoteUser.invoke(getId(VideoDownloaderApp.application))
            _playerState.update {
                it.copy(
                    currentUser = currentRemoteUser?.toUser(),
                    appConfig = appConfig.value
                )
            }
        }
    }


    /**
     *  Here the [media] is rechecked with a when again just to keep things consistent
     *  in the viewModel, the previous check in the activity is for files existence.
     */
    fun onStartDownload(
        media: PlayerDataModel,
        isAfterReview: Boolean = false,
        isAfterRewardedAd: Boolean = false,
    ) {
        viewModelScope.launch {
            if (currentRemoteUser == null) {
                Timber.tag(TAG).v("Remote User is null")
                if (isNetworkConnected(VideoDownloaderApp.application)) {
                    _playerChannel.emit(PlayerAction.OnShowMessage(R.string.network_not_available))
                }
                return@launch
            }

            // Check user mass remaining mass downloads
            if (currentRemoteUser?.remainingMassDownloadCount == 0
                && currentRemoteUser?.hasValidSubscription() == false
                && (media is PlayerDataModel.ExploreMediaItem || media is PlayerDataModel.CollectionMediaItem)) {
                _playerChannel.emit(PlayerAction.OnShowReachedMaxMassDownloadDialog)
                return@launch
            }


            _playerState.update {
                it.copy(
                    isFileDownloading = true,
                    error = null,
                    postWithMediaToDownload = null
                )
            }
            when (media) {
                is PlayerDataModel.DownloadedMediaItem -> {
                    when (media.mediaType) {
                        1, 2, 8 -> {   // image, video, carousel
                            if (currentLoggedInUser.value.id == -1L) downloadPostMedia(
                                media,
                                isAfterReview,
                                isAfterRewardedAd
                            ) else extractPostMedia(media)
                        }

                        -1 -> {         // audio
                            if (currentLoggedInUser.value.id == -1L) downloadAudioMedia(
                                media,
                                isAfterReview,
                                isAfterRewardedAd
                            ) else downloadLoggedInUserAudioMedia(media)
                        }
                    }
                }

                is PlayerDataModel.CollectionMediaItem -> {
                    saveCollectionPost(media)
                }

                is PlayerDataModel.ExploreMediaItem -> {
                    saveExplorePost(media)
                }

                else -> {
                    // NO-OP
                }
            }
        }
    }


    // region [LoggedIn User]

    private fun extractPostMedia(media: PlayerDataModel.DownloadedMediaItem) {
        viewModelScope.launch {
            // here user is logged in
            downloadPostByIdUseCase
                .invoke(media.media.post.mediaId, currentLoggedInUser.value)
                .catch {
                    Timber.tag(TAG).e(it)
                }
                .collect { response ->
                    when (response) {
                        is Response.Error -> handleMediaDownloadErrorResponse(response, media)
                        is Response.Loading -> {}
                        is Response.Success -> handlePostResponse(response.data!!, media)
                    }
                }
        }
    }


    /**
     *  the [media] will be always a download MediaDataModel.
     */
    private fun downloadLoggedInUserAudioMedia(media: PlayerDataModel) {
        viewModelScope.launch {
            if (media is PlayerDataModel.DownloadedMediaItem) {
                getAudioInfoUseCase
                    .getLoggedInUserAudioInfo(
                        buildInstagramLink(media.media),
                        currentRemoteUser?.uniqueId!!
                    )
                    .catch {
                        Timber.tag(TAG).e(it)
                    }
                    .collect { response ->
                        when (response) {
                            is Response.Loading -> {
                                // Already setting the loading
                            }

                            is Response.Success -> handleAudioResponse(response.data as AudioResponse)
                            is Response.Error -> handleMediaDownloadErrorResponse(response, media)
                        }
                    }
            }
        }
    }

    // endregion

    // region [Not Logged In User]

    private fun downloadPostMedia(
        media: PlayerDataModel.DownloadedMediaItem,
        isAfterReview: Boolean,
        isAfterRewardedAd: Boolean
    ) {
        _playerState.update {
            it.copy(
                error = null,
                isFileDownloading = true,
                postWithMediaToDownload = null
            )
        }
        viewModelScope.launch {
            downloadPostByIdUseCase
                .getPostInfoById(
                    currentRemoteUser?.uniqueId!!,
                    isAfterReview = isAfterReview,
                    isAfterRewarded = isAfterRewardedAd,
                    postId = media.media.post.mediaId,
                )
                .catch {
                    Timber.tag(TAG).e(it)
                }
                .collect { response ->
                    when (response) {
                        is Response.Loading -> {
                            // NO-OP
                        }

                        is Response.Success -> handlePostResponse(
                            response.data as PostResponse,
                            media
                        )

                        is Response.Error -> handleMediaDownloadErrorResponse(response, media)
                    }
                }
        }
    }

    private fun downloadAudioMedia(
        media: PlayerDataModel,
        isAfterReview: Boolean,
        isAfterRewardedAd: Boolean
    ) {
        viewModelScope.launch {
            if (media is PlayerDataModel.DownloadedMediaItem) {
                getAudioInfoUseCase
                    .invoke(
                        buildInstagramLink(media.media),
                        currentRemoteUser?.uniqueId!!,
                        isAfterReview,
                        isAfterRewardedAd
                    )
                    .catch {
                        Timber.tag(TAG).e(it)
                    }
                    .collect { response ->
                        when (response) {
                            is Response.Loading -> {}
                            is Response.Error -> handleMediaDownloadErrorResponse(response, media)
                            is Response.Success -> handleAudioResponse(response.data as AudioResponse)
                        }
                    }
            }
        }
    }

    // region

    private suspend fun updateUserRemainingMassDownloadsCount() {
        val newMassDownloadCount =
            currentRemoteUser?.remainingMassDownloadCount!! - 1
        currentRemoteUser =
            updateRemoteUser.updateRemainingMassDownloadCount(
                newMassDownloadCount = newMassDownloadCount,
                userDto = currentRemoteUser!!
            )
    }

    private fun saveExplorePost(media: PlayerDataModel.ExploreMediaItem) {
        viewModelScope.launch {
            val itemsToDownload = arrayListOf<MediaToDownload>()
            when (media.mediaType) {
                8 -> {
                    media.media.carouselMedia?.forEachIndexed { _, carouselMediaItem ->
                        if (carouselMediaItem?.mediaType == 1) {
                            itemsToDownload.add(
                                MediaToDownload(
                                    url = carouselMediaItem.imageVersions2?.candidates?.first()?.url!!,
                                    fileName = carouselMediaItem.id!!,
                                    profileUsername = currentLoggedInUser.value.username,
                                    mediaType = MediaType.IMAGE.toString(),
                                    createdAt = System.currentTimeMillis(),
                                    instagramId = carouselMediaItem.id
                                )
                            )
                        } else if (carouselMediaItem?.mediaType == 2) {
                            itemsToDownload.add(
                                MediaToDownload(
                                    url = carouselMediaItem.videoVersions?.first()?.url!!,
                                    fileName = carouselMediaItem.id!!,
                                    profileUsername = currentLoggedInUser.value.username,
                                    mediaType = MediaType.VIDEO.toString(),
                                    createdAt = System.currentTimeMillis(),
                                    instagramId = carouselMediaItem.id,
                                )
                            )
                        }
                    }
                }

                1 -> {
                    itemsToDownload.add(
                        MediaToDownload(
                            url = media.media.imageVersions2?.candidates?.first()?.url!!,
                            fileName = media.media.id!!,
                            profileUsername = currentLoggedInUser.value.username,
                            mediaType = MediaType.IMAGE.toString(),
                            createdAt = System.currentTimeMillis(),
                            instagramId = media.media.id,
                        )
                    )
                }

                2 -> {
                    itemsToDownload.add(
                        MediaToDownload(
                            url = media.media.videoVersions?.first()?.url!!,
                            fileName = media.media.id!!,
                            profileUsername = currentLoggedInUser.value.username,
                            mediaType = MediaType.VIDEO.toString(),
                            createdAt = System.currentTimeMillis(),
                            instagramId = media.media.id,
                        )
                    )
                }
            }

            insertPostUseCase.insertExtractedExplorePostAndMedia(
                media.media,
                currentLoggedInUser.value
            )

            val postToDownload = PostToDownload(
                url = buildInstagramLink(media.media),              // not very important
                profileUsername = currentLoggedInUser.value.username,
                isDownloading = false,
                mediaType = media.media.mediaType!!,
                instagramId = media.media.id!!,
                createdAt = System.currentTimeMillis(),
            )
            postToDownload.mediaToDownload = itemsToDownload

            updateUserRemainingMassDownloadsCount()

            _playerState.update {
                it.copy(
                    postWithMediaToDownload = postToDownload
                )
            }
            _playerState.update { it.copy(isFileDownloading = false) }
        }
    }


    private fun saveCollectionPost(media: PlayerDataModel.CollectionMediaItem) {
        viewModelScope.launch {
            val itemsToDownload = arrayListOf<MediaToDownload>()
            when (media.mediaType) {
                8 -> {
                    media.media.carouselMedias?.forEachIndexed { _, carouselMediaItem ->
                        if (carouselMediaItem.mediaType == 1) {
                            itemsToDownload.add(
                                MediaToDownload(
                                    url = carouselMediaItem.imageVersions2?.candidates?.first()?.url!!,
                                    fileName = carouselMediaItem.id!!,
                                    profileUsername = currentLoggedInUser.value.username,
                                    mediaType = MediaType.IMAGE.toString(),
                                    createdAt = System.currentTimeMillis(),
                                    instagramId = carouselMediaItem.id
                                )
                            )
                        } else if (carouselMediaItem.mediaType == 2) {
                            itemsToDownload.add(
                                MediaToDownload(
                                    url = carouselMediaItem.videoVersions?.first()?.url!!,
                                    fileName = carouselMediaItem.id!!,
                                    profileUsername = currentLoggedInUser.value.username,
                                    mediaType = MediaType.VIDEO.toString(),
                                    createdAt = System.currentTimeMillis(),
                                    instagramId = carouselMediaItem.id,
                                )
                            )
                        }
                    }
                }

                1 -> {
                    itemsToDownload.add(
                        MediaToDownload(
                            url = media.media.imageVersions2?.candidates?.first()?.url!!,
                            fileName = media.media.id!!,
                            profileUsername = currentLoggedInUser.value.username,
                            mediaType = MediaType.IMAGE.toString(),
                            createdAt = System.currentTimeMillis(),
                            instagramId = media.media.id,
                        )
                    )
                }

                2 -> {
                    itemsToDownload.add(
                        MediaToDownload(
                            url = media.media.videoVersions?.first()?.url!!,
                            fileName = media.media.id!!,
                            profileUsername = currentLoggedInUser.value.username,
                            mediaType = MediaType.VIDEO.toString(),
                            createdAt = System.currentTimeMillis(),
                            instagramId = media.media.id,
                        )
                    )
                }
            }

            insertPostUseCase.insertExtractedCollectionPostAndMedia(
                media.media,
                currentLoggedInUser.value
            )

            val postToDownload = PostToDownload(
                url = buildInstagramLink(media.media),              // not very important
                profileUsername = currentLoggedInUser.value.username,
                isDownloading = false,
                mediaType = media.media.mediaType!!,
                instagramId = media.media.id!!,
                createdAt = System.currentTimeMillis(),
            )
            postToDownload.mediaToDownload = itemsToDownload

            updateUserRemainingMassDownloadsCount()

            _playerState.update {
                it.copy(
                    postWithMediaToDownload = postToDownload
                )
            }
            _playerState.update { it.copy(isFileDownloading = false) }
        }
    }

    private fun handlePostResponse(data: PostResponse, media: PlayerDataModel) {
        viewModelScope.launch {
            val itemsToDownload = arrayListOf<MediaToDownload>()
            when (data.mediaType) {
                8 -> {
                    data.carouselMedia?.forEachIndexed { _, carouselMediaItem ->
                        if (carouselMediaItem?.mediaType == 1) {
                            itemsToDownload.add(
                                MediaToDownload(
                                    url = carouselMediaItem.imageVersions?.items?.first()?.url!!,
                                    fileName = carouselMediaItem.id!!,
                                    profileUsername = currentLoggedInUser.value.username,
                                    mediaType = MediaType.IMAGE.toString(),
                                    createdAt = System.currentTimeMillis(),
                                    instagramId = carouselMediaItem.id,
                                )
                            )
                        } else if (carouselMediaItem?.mediaType == 2) {
                            itemsToDownload.add(
                                MediaToDownload(
                                    url = carouselMediaItem.videoVersions?.first()?.url!!,
                                    fileName = carouselMediaItem.id!!,
                                    profileUsername = currentLoggedInUser.value.username,
                                    mediaType = MediaType.VIDEO.toString(),
                                    createdAt = System.currentTimeMillis(),
                                    instagramId = carouselMediaItem.id,
                                )
                            )
                        }
                    }
                }

                1 -> {
                    itemsToDownload.add(
                        MediaToDownload(
                            url = data.imageVersions?.items?.first()?.url!!,
                            fileName = data.id!!,
                            profileUsername = currentLoggedInUser.value.username,
                            mediaType = MediaType.IMAGE.toString(),
                            createdAt = System.currentTimeMillis(),
                            instagramId = data.id,
                        )
                    )
                }

                2 -> {
                    itemsToDownload.add(
                        MediaToDownload(
                            url = data.videoVersions?.first()?.url!!,
                            fileName = data.id!!,
                            profileUsername = currentLoggedInUser.value.username,
                            mediaType = MediaType.VIDEO.toString(),
                            createdAt = System.currentTimeMillis(),
                            instagramId = data.id,
                        )
                    )
                }
            }

            insertPostUseCase.insertPostAndMedia(data, currentLoggedInUser.value)
            val postToDownload = PostToDownload(
                url = "--",
                profileUsername = currentLoggedInUser.value.username,
                isDownloading = false,
                mediaType = data.mediaType!!,
                instagramId = data.id!!,
                createdAt = System.currentTimeMillis(),
            )
            postToDownload.mediaToDownload = itemsToDownload
            if (itemsToDownload.isNotEmpty()) {
                _playerState.update { it.copy(postWithMediaToDownload = postToDownload) }
            } else {
                Timber.tag(HomeViewModel.TAG).v("The list of media items to download is empty")
            }

            _playerState.update { it.copy(isFileDownloading = false) }
        }
    }

    private fun handleMediaDownloadErrorResponse(
        response: Response.Error<out Any>,
        media: PlayerDataModel
    ) {
        viewModelScope.launch {
            _playerState.update { it.copy(isFileDownloading = false) }
            if (response.data is GeneralErrorResponse) {
                when (response.data.error) {
                    "Not Found." -> {
                        _playerChannel.emit(PlayerAction.OnShowLoginRequiredForPrivatePost)
                    }

                    "Reached the maximum downloads." -> {
                        _playerChannel.emit(
                            PlayerAction.OnShowReachedLimitWithoutLogin(
                                media
                            )
                        )
                    }

                    "Could require a Review and Rewarded Ad." -> {
                        _playerChannel.emit(
                            PlayerAction.OnShowAdOrReviewRequiredDialog(
                                media
                            )
                        )
                    }

                    "A Rewarded Ad is required." -> {
                        _playerChannel.emit(
                            PlayerAction.OnShowRewardedAdRequiredDialog(media)
                        )
                    }

                    "Invalid subscription.", "Pending Subscription." -> {
                        _playerChannel.emit(PlayerAction.OnShowTempInvalidSubscription)
                    }

                    // extraction responses
                    "Wait some minutes or re-Login is required" -> {}
                    "Could not retrieve the media info" -> _playerState.update { it.copy(error = R.string.can_it_get_media_info) }
                    else -> _playerState.update { it.copy(error = R.string.something_went_wrong) }
                }
            } else {
                _playerState.update { it.copy(error = R.string.something_went_wrong) }
            }
        }
    }

    private fun handleAudioResponse(data: AudioResponse) {
        viewModelScope.launch {

            insertPostUseCase.insertAudio(data, currentLoggedInUser.value)

            val mediaToDownload = arrayListOf(
                MediaToDownload(
                    url = data.downloadUrl!!,
                    fileName = data.audioId,
                    profileUsername = currentLoggedInUser.value.username,
                    mediaType = MediaType.MUSIC.toString(),
                    createdAt = System.currentTimeMillis(),
                    instagramId = data.audioId
                )
            )

            val postToDownload = PostToDownload(
                url = "audio_${data.audioId}",           // no need for the url
                profileUsername = currentLoggedInUser.value.username,
                isDownloading = false,
                mediaType = -1,                          // media type is included in media items
                instagramId = data.audioId,
                createdAt = System.currentTimeMillis(),
            )

            postToDownload.mediaToDownload = mediaToDownload

            _playerState.update {
                it.copy(
                    postWithMediaToDownload = postToDownload
                )
            }
            _playerState.update { it.copy(isFileDownloading = false) }
        }
    }

    suspend fun getUpdatedItem(postDownloadedId: String): PlayerDataModel.DownloadedMediaItem {
        return PlayerDataModel.DownloadedMediaItem(getLocalPostByInstaId.invoke(postDownloadedId))
    }


    fun setUserNewMassDownloadCount() {
        viewModelScope.launch {
            try {
                val newMassDownloadCount =
                    currentRemoteUser?.remainingMassDownloadCount!! + appConfig.value?.massDownloadsToGetAsReward!!
                currentRemoteUser =
                    updateRemoteUser.updateRemainingMassDownloadCount(
                        newMassDownloadCount = newMassDownloadCount,
                        userDto = currentRemoteUser!!
                    )
            } catch (e: Exception) {
                Timber.tag(HomeViewModel.TAG).e(e)
                _playerChannel.emit(PlayerAction.OnShowMessage(R.string.something_went_wrong))
            }
        }
    }

    fun checkFakeDrag(dataModel: PlayerDataModel) {
        viewModelScope.launch {
            when (dataModel) {
                is PlayerDataModel.DownloadedMediaItem -> if (!fakeDragCases.getDownloadedFakeDrag()) {
                    fakeDragCases.setDownloadedFakeDrag(true)
                    _playerChannel.emit(PlayerAction.OnShowFakeDrag)
                }

                is PlayerDataModel.ExploreMediaItem -> if (!fakeDragCases.getExploreFakeDrag()) {
                    fakeDragCases.setExploreFakeDrag(true)
                    _playerChannel.emit(PlayerAction.OnShowFakeDrag)
                }

                is PlayerDataModel.CollectionMediaItem -> if (!fakeDragCases.getCollectionFakeDrag()) {
                    fakeDragCases.setCollectionFakeDrag(true)
                    _playerChannel.emit(PlayerAction.OnShowFakeDrag)
                }

                is PlayerDataModel.AdMediaItem -> {

                }
            }
        }
    }


    fun updateRemoteUser() {
        viewModelScope.launch {
            if (subscriptionLocalInfo.invoke().planTag.isNotBlank()) {
                currentRemoteUser = signUpOrGetRemoteUser.invoke(getId(VideoDownloaderApp.application))

                if (currentRemoteUser?.hasValidSubscription() == true) {
                    _playerChannel.emit(PlayerAction.OnHideAds)
                }
            }
        }
    }

}