package com.instagram.video.downloader.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.google.android.gms.ads.AdView
import com.instagram.video.downloader.R
import com.instagram.video.downloader.VideoDownloaderApp
import com.instagram.video.downloader.cases.collection.GetUserCollectionPagingMedia
import com.instagram.video.downloader.cases.collection.GetUserCollectionUseCase
import com.instagram.video.downloader.cases.explore.GetUserExplorePagingMedia
import com.instagram.video.downloader.cases.explore.GetUserExploreRefreshLimitExceeded
import com.instagram.video.downloader.cases.explore.GetUserExploreUseCase
import com.instagram.video.downloader.cases.general.CheckAppAccessTokenUseCase
import com.instagram.video.downloader.cases.general.CheckIntentDownloadLink
import com.instagram.video.downloader.cases.media.PostAudioHighlightDownloadUseCase
import com.instagram.video.downloader.cases.media.PostAudioHighlightExtractionUseCase
import com.instagram.video.downloader.cases.user.CurrentLoggedInUserUseCase
import com.instagram.video.downloader.cases.user.DeleteUserProfile
import com.instagram.video.downloader.cases.user.GetAllLoggedInUsers
import com.instagram.video.downloader.cases.user.GetInstagramUserInfo
import com.instagram.video.downloader.cases.user.GetPostToDownload
import com.instagram.video.downloader.cases.user.GetUserLocalPostsUseCase
import com.instagram.video.downloader.cases.user.InsertLoggedInInstagram
import com.instagram.video.downloader.cases.user.InsertNewUser
import com.instagram.video.downloader.cases.user.InsertPostUseCase
import com.instagram.video.downloader.cases.user.LoggedInUser
import com.instagram.video.downloader.cases.user.SingUpOrGetRemoteUser
import com.instagram.video.downloader.cases.user.SubscriptionLocalInfo
import com.instagram.video.downloader.cases.user.UpdateRemoteUser
import com.instagram.video.downloader.cases.user.UserDeletePostUseCase
import com.instagram.video.downloader.cases.version.CheckCurrentVersion
import com.instagram.video.downloader.common.Response
import com.instagram.video.downloader.common.buildHighlightLink
import com.instagram.video.downloader.common.clearCookies
import com.instagram.video.downloader.common.download.MediaType
import com.instagram.video.downloader.common.extractHighlightIdPhoneShareLink
import com.instagram.video.downloader.common.extractUserInstagramIdFromCookies
import com.instagram.video.downloader.common.getId
import com.instagram.video.downloader.common.isAudioLink
import com.instagram.video.downloader.common.isHighlightLink
import com.instagram.video.downloader.common.isHighlightShareLink
import com.instagram.video.downloader.common.isNetworkConnected
import com.instagram.video.downloader.common.isValidInstagramLink
import com.instagram.video.downloader.data.local.room.entities.MediaToDownload
import com.instagram.video.downloader.data.local.room.entities.PostToDownload
import com.instagram.video.downloader.data.local.room.entities.PostWithMediaUserAndMedia
import com.instagram.video.downloader.data.local.room.entities.User
import com.instagram.video.downloader.data.remote.api.dto.AppConfig
import com.instagram.video.downloader.data.remote.api.dto.AudioResponse
import com.instagram.video.downloader.data.remote.api.dto.HighlightData
import com.instagram.video.downloader.data.remote.api.dto.HighlightMedia
import com.instagram.video.downloader.data.remote.api.dto.HighlightResponse
import com.instagram.video.downloader.data.remote.api.dto.PostResponse
import com.instagram.video.downloader.data.remote.api.dto.UserDto
import com.instagram.video.downloader.data.remote.api.dto.hasValidSubscription
import com.instagram.video.downloader.data.remote.api.dto.toUser
import com.instagram.video.downloader.data.remote.instaApi.dto.extraction.HighlightExtractionResponse
import com.instagram.video.downloader.data.remote.instaApi.dto.extraction.toHighlightData
import com.instagram.video.downloader.data.remote.instaApi.dto.extraction.toHighlightMedia
import com.instagram.video.downloader.data.remote.utils.GeneralErrorResponse
import com.instagram.video.downloader.ui.home.explore.MassItemDownloadingInfo
import com.instagram.video.downloader.ui.home.explore.collection.CollectionAction
import com.instagram.video.downloader.ui.home.explore.collection.CollectionState
import com.instagram.video.downloader.ui.home.explore.collection.adapter.CollectionUiModel
import com.instagram.video.downloader.ui.home.explore.explore.ExploreAction
import com.instagram.video.downloader.ui.home.explore.explore.ExploreState
import com.instagram.video.downloader.ui.home.explore.explore.adapter.ExploreUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(

    private val getPostToDownload: GetPostToDownload,

    private val updateRemoteUser: UpdateRemoteUser,

    private val getUserLocalPostsUseCase: GetUserLocalPostsUseCase,

    private val insertPostUseCase: InsertPostUseCase,
    private val userDeletePostUseCase: UserDeletePostUseCase,

    private val postAudioHighlightDownloadUseCase: PostAudioHighlightDownloadUseCase,
    private val postAudioHighlightExtractionUseCase: PostAudioHighlightExtractionUseCase,

    private val getInstagramUserInfo: GetInstagramUserInfo,
    private val insertLoggedInInstagram: InsertLoggedInInstagram,


    private val checkAppAccessTokenUseCase: CheckAppAccessTokenUseCase,
    private val signUpOrGetRemoteUser: SingUpOrGetRemoteUser,
    private val checkCurrentVersion: CheckCurrentVersion,

    private val currentLoggedInUserUseCase: CurrentLoggedInUserUseCase,

    private val getUserExplorePagingMedia: GetUserExplorePagingMedia,
    private val getUserExploreUseCase: GetUserExploreUseCase,


    private val getUserCollectionPagingMedia: GetUserCollectionPagingMedia,
    private val getUserCollectionUseCase: GetUserCollectionUseCase,


    private val userExploreRefreshLimitExceeded: GetUserExploreRefreshLimitExceeded,


    private val getAllLoggedInUsers: GetAllLoggedInUsers,
    private val insertNewUser: InsertNewUser,
    private val deleteUserUseCase: DeleteUserProfile,

    private val checkIntentDownloadLink: CheckIntentDownloadLink,

    private val subscriptionLocalInfo: SubscriptionLocalInfo,

    ) : ViewModel() {

    companion object {
        const val TAG = "HomeViewModel"
    }


    // region [HomeFragment]

    val selectedPostForOptions = mutableStateOf<PostWithMediaUserAndMedia?>(null)

    // endregion

    // region [Mass Downloads]

    val resetSelection = MutableSharedFlow<Boolean>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val massDownloadItemDownloadingInfo = MutableSharedFlow<MassItemDownloadingInfo>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val massDownloadRewarded = MutableSharedFlow<Boolean>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    // endregion

    private val internetCheckDelay = 5000L

    lateinit var currentLoggedInUser: MutableStateFlow<LoggedInUser>
    var currentRemoteUser: UserDto? = null
    var appConfig = MutableStateFlow<AppConfig?>(null)

    private val _homeChannel = MutableSharedFlow<HomeAction>()
    val homeChannel: SharedFlow<HomeAction> = _homeChannel

    private val _exploreChannel = MutableSharedFlow<ExploreAction>()
    val exploreChannel: SharedFlow<ExploreAction> = _exploreChannel

    private val _collectionChannel = MutableSharedFlow<CollectionAction>()
    val collectionChannel: SharedFlow<CollectionAction> = _collectionChannel


    private val _homeState = MutableStateFlow(HomeState())
    val homeState = _homeState.asStateFlow()

    private val _exploreState = MutableStateFlow(ExploreState())
    val exploreState = _exploreState.asStateFlow()

    private val _collectionState = MutableStateFlow(CollectionState())
    val collectionState = _collectionState

    var collectionDataFlow: Flow<PagingData<CollectionUiModel>> =
        MutableStateFlow(PagingData.empty())
    var exploreDataFlow: Flow<PagingData<ExploreUiModel>> = MutableStateFlow(PagingData.empty())


    var profilesDataFlow = MutableStateFlow<List<User>>(arrayListOf())

    var postsDataFlow = MutableStateFlow<List<PostWithMediaUserAndMedia>>(arrayListOf())


    init {
        viewModelScope.launch {
            try {
                userExploreRefreshLimitExceeded.resetRefreshTimes()

                if (checkIntentDownloadLink.getLinkToDownload().isNotBlank())
                    _homeState.update { it.copy(isLoading = true) }

                _exploreState.update { it.copy(isLoading = true) }
                val currentUser = currentLoggedInUserUseCase.invoke()
                currentLoggedInUser = MutableStateFlow(currentUser)

                postsDataFlow.value = getUserLocalPostsUseCase.invoke(currentUser)

                if (currentUser.id != -1L) {
                    exploreDataFlow = getExploreItems(currentUser).cachedIn(viewModelScope)
                    collectionDataFlow = getCollectionItems(currentUser).cachedIn(viewModelScope)
                }

                _collectionState.update { it.copy(loggedInUser = currentUser, isLoading = false) }
                _exploreState.update { it.copy(loggedInUser = currentUser, isLoading = false) }

                profilesDataFlow.value = getAllLoggedInUsers.invoke()

                while (true) {
                    _homeState.update {
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

    private fun getExploreItems(loggedInUser: LoggedInUser): Flow<PagingData<ExploreUiModel>> {
        var index = 0
        return getUserExplorePagingMedia
            .invoke(
                getUserExploreUseCase,
                getUserLocalPostsUseCase,
                getPostToDownload,
                loggedInUser
            )
            .map { pagingData ->
                pagingData.map { ExploreUiModel.MediaItem(it, index++) }
            }
            .map {
                it.insertSeparators { before, after ->
                    if (after == null) {
                        // we're at the end of the list
                        return@insertSeparators null               // For example insert at the end each time
                    }

                    if (before == null) {
                        // we're at the beginning of the list
                        return@insertSeparators null
                    }
                    if (after.index % 12 == 0 && after.index != 0) {
//                        return@insertSeparators ExploreUiModel.AdItem(" ad This is for ad This is for ad This is for ad ")
                        return@insertSeparators null
                    }

                    null
                }
            }
    }

    private fun getCollectionItems(loggedInUser: LoggedInUser): Flow<PagingData<CollectionUiModel>> {
        var index = 0
        return getUserCollectionPagingMedia
            .invoke(
                getUserCollectionUseCase,
                getUserLocalPostsUseCase,
                getPostToDownload,
                loggedInUser
            )
            .map { pagingData ->
                pagingData.map { CollectionUiModel.MediaItem(it, index++) }
            }
            .map {
                it.insertSeparators { before, after ->

                    if (after == null) {
                        // we're at the end of the list
                        return@insertSeparators null               // For example insert at the end each time
                    }

                    if (before == null) {
                        // we're at the beginning of the list
                        return@insertSeparators null
                    }

                    if (after.index % 12 == 0 && after.index != 0) {
//                        return@insertSeparators CollectionUiModel.AdItem("This for ads")
                        return@insertSeparators null
                    }
                    null
                }
            }
    }


    /**
     *  This method is called when network is available,
     *  and called just one time if the network is available.
     */
    fun getUserAndCheckCurrentVersion() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                checkAppAccessTokenUseCase.invoke()
                val currentVersion = checkCurrentVersion.invoke()
                if (appConfig.value == null)
                    appConfig.value = currentVersion.appConfig
                if (!currentVersion.isSupported) {
                    _homeChannel.emit(HomeAction.OnShowVersionNoLongerSupported)
                    return@withContext
                }
                currentRemoteUser =
                    signUpOrGetRemoteUser.invoke(getId(VideoDownloaderApp.application))
                _homeState.update { it.copy(currentUser = currentRemoteUser?.toUser()) }

                // Only load ads if the app config allows it
                if (appConfig.value?.isAdsEnabled == true && currentRemoteUser?.hasValidSubscription() == false) {
                    _homeChannel.emit(HomeAction.OnLoadNativeAds)           // for the fragment
                    _homeChannel.emit(HomeAction.OnLoadInterAds)            // for the activity (load inter and open ad)
                }

                // if the user share the link through the app
                if (checkIntentDownloadLink.getLinkToDownload().isNotBlank() && appConfig.value?.isTesting == false) {
                    val receivedUrl = checkIntentDownloadLink.getLinkToDownload()
                    // check if the link is instagram link
                    _homeState.update { it.copy(isLoading = false) }
                    if (!isValidInstagramLink(receivedUrl)
                        && !isHighlightShareLink(receivedUrl)
                        && !isAudioLink(receivedUrl)
                    ) {
                        Timber.tag(TAG).v("Invalid instagram link from intent share")
                        _homeState.update { it.copy(error = R.string.invalid_instagram_link) }
                    } else {
                        _homeChannel.emit(HomeAction.OnCheckIntent)
                    }
                    checkIntentDownloadLink.clearLinkToDownload()
                } else if (checkIntentDownloadLink.getLinkToDownload().isNotBlank() && appConfig.value?.isTesting == true) {
                    _homeChannel.emit(HomeAction.OnJustOpenSplash)
                }
            }
        }
    }

    fun reloadDownloadedPosts() {
        if (this::currentLoggedInUser.isInitialized.not())
            return
        viewModelScope.launch {
            postsDataFlow.value = getUserLocalPostsUseCase.invoke(currentLoggedInUser.value)
        }
    }

    fun setIntentDownloadLink(link: String) = checkIntentDownloadLink.setDownloadLink(link)

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
                massDownloadRewarded.tryEmit(true)
            } catch (e: Exception) {
                Timber.tag(TAG).e(e)
                _homeChannel.emit(HomeAction.OnShowMessage(R.string.something_went_wrong))
            }
        }
    }

    // region [DownloadLinkDialog]

    fun onStartDownload(
        link: String,
        isAfterReview: Boolean = false,
        isAfterRewardedAd: Boolean = false
    ) {
        if (currentLoggedInUser.value.id == -1L) {
            when {
                isHighlightLink(link) -> startHighlightMediaDownload(
                    link,
                    isAfterReview,
                    isAfterRewardedAd
                )

                isHighlightShareLink(link) -> {
                    startHighlightMediaDownload(
                        buildHighlightLink(extractHighlightIdPhoneShareLink(link)),
                        isAfterReview,
                        isAfterRewardedAd
                    )
                }

                isAudioLink(link) -> startAudioMediaDownload(link, isAfterReview, isAfterRewardedAd)

                else -> startPostMediaDownload(link, isAfterReview, isAfterRewardedAd)
            }
        } else {
            when {
                isHighlightLink(link) || isHighlightShareLink(link) -> startHighlightExtraction(link)

                isAudioLink(link) -> startAudioExtraction(link)

                else -> startPostExtraction(link)           // no review or reward, unlimited downloads
            }
        }
    }

    // region [LoggedIn User Case]

    private fun startHighlightExtraction(link: String) {
        _homeState.update { it.copy(error = null) }
        viewModelScope.launch {
            postAudioHighlightExtractionUseCase
                .invoke(
                    userUniqueId = currentRemoteUser?.uniqueId!!,
                    mediaUrl = link,
                    isHighlight = true,
                    isAudio = false,
                    currentLoggedInUser = currentLoggedInUser.value,
                )
                .catch { e ->
                    Timber.tag(TAG).e(e)
                    _homeState.update { it.copy(error = R.string.something_went_wrong) }
                }
                .collect { response ->
                    when (response) {
                        is Response.Loading -> _homeState.update {
                            it.copy(
                                postWithMediaToDownload = null,
                                isFileDownloading = true
                            )
                        }

                        is Response.Error -> handleMediaDownloadErrorResponse(response, link)

                        is Response.Success -> {
                            val highlightDataAll = response.data as HighlightExtractionResponse

                            if (highlightDataAll.data?.reelsMedia == null || highlightDataAll.data.reelsMedia.isEmpty()) {
                                _homeState.update { it.copy(error = R.string.something_went_wrong) }
                                return@collect
                            }

                            val highlightData =
                                highlightDataAll.data.reelsMedia.first()?.items?.first()
                                    ?.toHighlightData(highlightDataAll.data.reelsMedia.first()!!)
                            val highlights =
                                highlightDataAll.data.reelsMedia.first()?.items?.map { it?.toHighlightMedia()!! }!!

                            highlightsToChoose =
                                Pair(highlightData, highlights)
                            _homeState.update { it.copy(isFileDownloading = false) }
                            _homeChannel.emit(HomeAction.OnShowHighlightsToDownloadDialog)
                        }
                    }
                }
        }
    }

    private fun startAudioExtraction(link: String) {
        _homeState.update { it.copy(error = null) }
        viewModelScope.launch {
            postAudioHighlightExtractionUseCase
                .invoke(
                    userUniqueId = currentRemoteUser?.uniqueId!!,
                    mediaUrl = link,
                    isHighlight = false,
                    isAudio = true,
                    currentLoggedInUser = currentLoggedInUser.value
                )
                .catch { e ->
                    Timber.tag(TAG).e(e)
                    _homeState.update { it.copy(error = R.string.something_went_wrong) }
                }
                .collect { response ->
                    when (response) {
                        is Response.Loading -> _homeState.update {
                            it.copy(
                                postWithMediaToDownload = null,
                                isFileDownloading = true
                            )
                        }

                        is Response.Error -> handleMediaDownloadErrorResponse(response, link)

                        is Response.Success -> handleAudioResponse(
                            response.data as AudioResponse,
                            link
                        )
                    }
                }
        }
    }

    private fun startPostExtraction(link: String) {
        _homeState.update { it.copy(error = null) }
        viewModelScope.launch {
            postAudioHighlightExtractionUseCase
                .invoke(
                    userUniqueId = currentRemoteUser?.uniqueId!!,
                    mediaUrl = link,
                    isHighlight = false,
                    isAudio = false,
                    currentLoggedInUser = currentLoggedInUser.value
                )
                .catch { e ->
                    Timber.tag(TAG).e(e)
                    _homeState.update { it.copy(error = R.string.something_went_wrong) }
                }
                .collect { response ->
                    when (response) {
                        is Response.Loading -> _homeState.update {
                            it.copy(
                                isFileDownloading = true,
                                postWithMediaToDownload = null
                            )
                        }

                        is Response.Error -> handleMediaDownloadErrorResponse(response, link)

                        is Response.Success -> handlePostResponse(
                            response.data as PostResponse,
                            link
                        )
                    }
                }
        }
    }

    // endregion

    // region [Non-Logged In user Case]

    private fun startPostMediaDownload(
        link: String,
        isAfterReview: Boolean,
        isAfterRewardedAd: Boolean
    ) {
        _homeState.update { it.copy(error = null) }
        viewModelScope.launch {
            postAudioHighlightDownloadUseCase
                .invoke(
                    currentRemoteUser?.uniqueId!!,
                    link,
                    false,
                    isAfterReview,
                    isAfterRewardedAd
                )
                .catch { e ->
                    Timber.tag(TAG).e(e)
                    _homeState.update { it.copy(error = R.string.something_went_wrong) }
                }
                .collect { response ->
                    when (response) {
                        is Response.Loading -> _homeState.update {
                            it.copy(
                                isFileDownloading = true,
                                postWithMediaToDownload = null
                            )
                        }

                        is Response.Error -> handleMediaDownloadErrorResponse(response, link)

                        is Response.Success -> handlePostResponse(
                            response.data as PostResponse,
                            link
                        )
                    }
                }
        }
    }


    /**
     *  The response to handle is from my api or from instagram api
     */
    private fun handlePostResponse(data: PostResponse, postLink: String) {
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
                url = postLink,
                profileUsername = currentLoggedInUser.value.username,
                isDownloading = false,
                mediaType = data.mediaType!!,
                instagramId = data.id!!,
                createdAt = System.currentTimeMillis(),
            )
            postToDownload.mediaToDownload = itemsToDownload

            if (itemsToDownload.isNotEmpty()) {
                _homeState.update { it.copy(postWithMediaToDownload = postToDownload) }
            } else {
                Timber.tag(TAG).v("The list of media items to download is empty")
            }

            _homeState.update { it.copy(isFileDownloading = false) }
        }
    }

    private fun handleAudioResponse(data: AudioResponse, audioLink: String) {
        viewModelScope.launch {
            _homeState.update { it.copy(isFileDownloading = true) }

            insertPostUseCase.insertAudio(data, currentLoggedInUser.value)

            val mediaToDownload = arrayListOf(
                MediaToDownload(
                    url = data.downloadUrl!!,
                    fileName = data.audioId.toString(),
                    profileUsername = currentLoggedInUser.value.username,
                    mediaType = MediaType.MUSIC.toString(),
                    createdAt = System.currentTimeMillis(),
                    instagramId = data.audioId.toString()
                )
            )

            val postToDownload = PostToDownload(
                url = "audio_${data.audioId}",           // no need for the url
                profileUsername = currentLoggedInUser.value.username,
                isDownloading = false,
                mediaType = -1,                                     // media type is included in media items
                instagramId = data.audioId.toString(),
                createdAt = System.currentTimeMillis(),
            )

            postToDownload.mediaToDownload = mediaToDownload

            _homeState.update {
                it.copy(
                    postWithMediaToDownload = postToDownload
                )
            }
            _homeState.update { it.copy(isFileDownloading = false) }
        }
    }

    private fun handleMediaDownloadErrorResponse(response: Response.Error<out Any>, link: String) {
        viewModelScope.launch {
            _homeState.update { it.copy(isFileDownloading = false) }
            if (response.data is GeneralErrorResponse) {
                when (response.data.error) {
                    "Not Found." -> {
                        _homeChannel.emit(
                            HomeAction.OnShowLoginRequiredForPrivatePost
                        )
                    }

                    "Reached the maximum downloads." -> {
                        _homeChannel.emit(
                            HomeAction.OnShowReachedLimitWithoutLogin(
                                link
                            )
                        )
                    }

                    "Could require a Review and Rewarded Ad." -> {
                        _homeChannel.emit(
                            HomeAction.OnShowAdOrReviewRequiredDialog(
                                link
                            )
                        )
                    }

                    "A Rewarded Ad is required." -> {
                        _homeChannel.emit(
                            HomeAction.OnShowRewardedAdRequiredDialog(
                                link
                            )
                        )
                    }

                    "Invalid subscription.", "Pending Subscription." -> {
                        _homeChannel.emit(HomeAction.OnShowTempInvalidSubscription)
                    }


                    // these from extraction

                    "Wait some minutes or re-Login is required" -> {
                        // TODO ===>
                    }

                    "Could not retrieve the media info" -> _homeState.update { it.copy(error = R.string.can_it_get_media_info) }

                    else -> _homeState.update { it.copy(error = R.string.something_went_wrong) }
                }
            } else {
                _homeState.update { it.copy(error = R.string.something_went_wrong) }
            }
        }
    }

    var highlightsToChoose: Pair<HighlightData?, List<HighlightMedia>> = Pair(null, arrayListOf())

    fun onHighlightsStartFilesDownload(highlightData: HighlightData, medias: List<HighlightMedia>) {
        viewModelScope.launch {
            // here I have just tow types 1 and 2
            val itemsToDownload = arrayListOf<MediaToDownload>()
            medias.forEach { media ->
                when (media.mediaType) {
                    1 -> {
                        itemsToDownload.add(
                            MediaToDownload(
                                url = media.imageVersions?.items?.first()?.url!!,
                                fileName = media.id!!,
                                profileUsername = currentLoggedInUser.value.username,
                                mediaType = MediaType.IMAGE.toString(),
                                createdAt = System.currentTimeMillis(),
                                instagramId = media.id
                            )
                        )
                    }

                    2 -> {
                        itemsToDownload.add(
                            MediaToDownload(
                                url = media.videoVersions?.first()?.url!!,
                                fileName = media.id!!,
                                profileUsername = currentLoggedInUser.value.username,
                                mediaType = MediaType.VIDEO.toString(),
                                createdAt = System.currentTimeMillis(),
                                instagramId = media.id
                            )
                        )
                    }
                }
            }
            insertPostUseCase.insertHighlight(highlightData, medias, currentLoggedInUser.value)

            val postToDownload = PostToDownload(
                url = "highlight_${highlightData.id!!}",           // no need for the url
                profileUsername = currentLoggedInUser.value.username,
                isDownloading = false,
                mediaType = -1,                                     // media type is included in media items
                instagramId = highlightData.id,
                createdAt = System.currentTimeMillis(),
            )
            postToDownload.mediaToDownload = itemsToDownload

            _homeState.update {
                it.copy(
                    postWithMediaToDownload = postToDownload,
                    isFileDownloading = true,
                )
            }

            // the download of files started here
            _homeState.update { it.copy(isFileDownloading = false) }
        }
    }

    private fun startHighlightMediaDownload(
        link: String,
        isAfterReview: Boolean,
        isAfterRewardedAd: Boolean
    ) {
        _homeState.update { it.copy(error = null) }
        viewModelScope.launch {
            postAudioHighlightDownloadUseCase
                .invoke(
                    currentRemoteUser?.uniqueId!!,
                    link,
                    true,
                    isAfterReview,
                    isAfterRewardedAd
                )
                .catch { e ->
                    Timber.tag(TAG).e(e)
                    _homeState.update { it.copy(error = R.string.something_went_wrong) }
                }
                .collect { response ->
                    when (response) {
                        is Response.Loading -> _homeState.update {
                            it.copy(
                                postWithMediaToDownload = null,
                                isFileDownloading = true,
                            )
                        }

                        is Response.Error -> handleMediaDownloadErrorResponse(response, link)

                        is Response.Success -> {
                            _homeState.update { it.copy(isFileDownloading = false) }
                            val data = response.data as HighlightResponse
                            highlightsToChoose =
                                Pair(data.additionalData!!, data.medias?.filterNotNull()!!)
                            _homeChannel.emit(HomeAction.OnShowHighlightsToDownloadDialog)
                        }
                    }
                }
        }
    }

    private fun startAudioMediaDownload(
        link: String,
        isAfterReview: Boolean,
        isAfterRewardedAd: Boolean
    ) {
        _homeState.update { it.copy(error = null) }
        viewModelScope.launch {
            postAudioHighlightDownloadUseCase
                .invoke(
                    currentRemoteUser?.uniqueId!!,
                    link,
                    false,
                    isAfterReview,
                    isAfterRewardedAd,
                    true
                )
                .catch { e ->
                    Timber.tag(TAG).e(e)
                    _homeState.update { it.copy(error = R.string.something_went_wrong) }
                }
                .collect { response ->
                    when (response) {
                        is Response.Loading -> _homeState.update {
                            it.copy(
                                postWithMediaToDownload = null,
                                isFileDownloading = true
                            )
                        }

                        is Response.Error -> handleMediaDownloadErrorResponse(response, link)

                        is Response.Success -> handleAudioResponse(
                            response.data as AudioResponse,
                            link
                        )
                    }
                }
        }
    }

    // endregion

    // endregion

    // region [LoginBottomSheet]

    fun handleUserInstagramLogin(
        cookies: String,
        isFromProfiles: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                _homeState.update { it.copy(isLoading = true) }
                extractUserInstagramIdFromCookies(cookies).also { userInstagramId ->
                    if (userInstagramId.isNotEmpty()) {
                        val userInfo = getInstagramUserInfo.invoke(
                            userInstagramId,
                            cookies
                        )
                        if (isFromProfiles) {
                            // just insert the new user
                            val newUser = insertNewUser.invoke(userInfo, cookies)
                            if (profilesDataFlow.value.isEmpty()) {
                                // no user exist already
                                val loggedInUser = LoggedInUser(
                                    username = newUser.username,
                                    instagramId = newUser.instagramId,
                                    id = newUser.identifier,
                                    cookies = newUser.cookies,
                                    isCookiesExpired = false,
                                )
                                currentLoggedInUserUseCase.setCurrentLoggedInUser(loggedInUser)
                                currentLoggedInUser.value = loggedInUser

                                exploreDataFlow =
                                    getExploreItems(currentLoggedInUser.value).cachedIn(
                                        viewModelScope
                                    )
                                collectionDataFlow =
                                    getCollectionItems(currentLoggedInUser.value).cachedIn(
                                        viewModelScope
                                    )
                            }
                        } else {
                            insertLoggedInInstagram.invoke(userInfo, cookies)
                            currentLoggedInUser.value = currentLoggedInUserUseCase()

                            exploreDataFlow =
                                getExploreItems(currentLoggedInUser.value).cachedIn(viewModelScope)
                            collectionDataFlow =
                                getCollectionItems(currentLoggedInUser.value).cachedIn(
                                    viewModelScope
                                )
                        }

                        _homeState.update {
                            it.copy(
                                isLoading = false,
                                isExtractingUserInfoFailed = false,
                                error = null,
                            )
                        }

                        _exploreState.update {
                            it.copy(
                                isLoading = false,
                                loggedInUser = currentLoggedInUser.value
                            )
                        }
                        _collectionState.update {
                            it.copy(
                                isLoading = false,
                                loggedInUser = currentLoggedInUser.value
                            )
                        }
                        profilesDataFlow.value = getAllLoggedInUsers.invoke()
                    } else {
                        _homeState.update {
                            it.copy(
                                isLoading = false,
                                isExtractingUserInfoFailed = true,
                                error = R.string.something_went_wrong
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e)
                _homeState.update {
                    it.copy(
                        isExtractingUserInfoFailed = true,
                        isLoading = false,
                        error = R.string.something_went_wrong,
                    )
                }
            }
        }
    }

    // endregion

    // region [Actions from Fragments]

    fun onHomeAction(homeAction: HomeAction) {
        viewModelScope.launch {
            if (homeAction == HomeAction.OnAddNewUser && profilesDataFlow.value.isNotEmpty()) {
                if (currentRemoteUser != null && currentRemoteUser?.subscription == null) {
                    // user has no subscription
                    _homeChannel.emit(HomeAction.OnShowShouldSubscribeDialog)
                    return@launch
                }
            }
            _homeChannel.emit(homeAction)
        }
    }

    fun onExploreAction(exploreAction: ExploreAction) {
        viewModelScope.launch {
            when (exploreAction) {
                ExploreAction.OnSwipeRefresh -> {
                    _exploreState.update { it.copy(isSwipeLoading = true) }
                    delay(1000)
                    if (userExploreRefreshLimitExceeded.invoke()) {
                        _exploreState.update { it.copy(error = R.string.swipe_reached_limit) }
                        delay(1000)
                        _exploreState.update { it.copy(error = null) }
                    } else {
                        _exploreChannel.emit(ExploreAction.OnSwipeRefresh)
                    }
                    _exploreState.update { it.copy(isSwipeLoading = false) }
                }

                ExploreAction.OnShowInstagramLoginDialog -> {
                    _homeChannel.emit(HomeAction.OnShowInstagramLogin)
                }

                ExploreAction.HideAds -> {
                    //
                }
            }
        }
    }

    fun onCollectionAction(collectionAction: CollectionAction) {
        viewModelScope.launch {
            when (collectionAction) {
                CollectionAction.OnSwipeRefresh -> {
                    _collectionState.update { it.copy(isSwipeLoading = true) }
                    _collectionChannel.emit(CollectionAction.OnSwipeRefresh)
                    delay(1000)
                    _collectionState.update { it.copy(isSwipeLoading = false) }
                }

                CollectionAction.OnShowInstagramLoginDialog -> {
                    _homeChannel.emit(HomeAction.OnShowInstagramLogin)
                }

                CollectionAction.OnReloadClick -> {
                    // TODO add here the limit logic
                    _collectionChannel.emit(CollectionAction.OnReloadClick)
                }

                CollectionAction.HideAds -> {

                }
            }
        }
    }

    fun onLikeAction() {

    }

    // endregion

    // region [ProfilesFragment]

    fun switchTo(user: User, loggedInUser: LoggedInUser) {

    }

    fun deleteProfileUser(user: User) {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true, error = null) }
            deleteUserUseCase.invoke(user)
            profilesDataFlow.value = getAllLoggedInUsers.invoke()
            if (user.identifier == currentLoggedInUser.value.id) {
                // user delete the current Logged in user
                if (profilesDataFlow.value.isEmpty()) {
                    currentLoggedInUserUseCase.setCurrentLoggedInUser(null)
                    clearCookies()
                } else {
                    val randomUser = profilesDataFlow.value.first()
                    currentLoggedInUserUseCase.setCurrentLoggedInUser(
                        LoggedInUser(
                            username = randomUser.username,
                            instagramId = randomUser.instagramId,
                            id = randomUser.identifier,
                            cookies = randomUser.cookies,
                            isCookiesExpired = randomUser.cookiesExpiringAt < System.currentTimeMillis()
                        )
                    )
                }
                currentLoggedInUser.value = currentLoggedInUserUseCase.invoke()
                _exploreState.update { it.copy(loggedInUser = currentLoggedInUser.value) }
                _collectionState.update { it.copy(loggedInUser = currentLoggedInUser.value) }
            }
            _homeState.update { it.copy(isLoading = false) }
        }
    }

    // endregion

    // region [HomeFragment]

    fun deletePost(post: PostWithMediaUserAndMedia) {
        viewModelScope.launch {
            _homeState.update { it.copy(isLoading = true) }
            userDeletePostUseCase.invoke(post)
            this@HomeViewModel.reloadDownloadedPosts()
            _homeChannel.emit(HomeAction.OnShowMessage(R.string.post_deleted_successfully))
            _homeState.update {
                it.copy(isLoading = false)
            }
        }
    }

    // endregion

    // region [ExploreFragment]

    fun handleExploreMassDownload(items: List<ExploreUiModel.MediaItem>) {
        viewModelScope.launch {
            _exploreState.update { it.copy(isMassDownloading = true, massMediaToDownload = null) }
            val postsToDownload = arrayListOf<PostToDownload>()
            items.forEach { mediaUi ->
                val itemsToDownload = arrayListOf<MediaToDownload>()
                when (mediaUi.media.mediaType) {
                    1 -> {
                        itemsToDownload.add(
                            MediaToDownload(
                                url = mediaUi.media.imageVersions2?.candidates?.first()?.url!!,
                                fileName = mediaUi.media.id!!,
                                profileUsername = currentLoggedInUser.value.username,
                                mediaType = MediaType.IMAGE.toString(),
                                createdAt = System.currentTimeMillis(),
                                instagramId = mediaUi.media.id,
                            )
                        )
                    }

                    2 -> {
                        itemsToDownload.add(
                            MediaToDownload(
                                url = mediaUi.media.videoVersions?.first()?.url!!,
                                fileName = mediaUi.media.id!!,
                                profileUsername = currentLoggedInUser.value.username,
                                mediaType = MediaType.VIDEO.toString(),
                                createdAt = System.currentTimeMillis(),
                                instagramId = mediaUi.media.id,
                            )
                        )
                    }

                    8 -> {
                        mediaUi.media.carouselMedia?.forEachIndexed { _, carouselMediaItem ->
                            if (carouselMediaItem?.mediaType == 1) {
                                itemsToDownload.add(
                                    MediaToDownload(
                                        url = carouselMediaItem.imageVersions2?.candidates?.first()?.url!!,
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
                }
                val postToDownload = PostToDownload(
                    url = "https://instagram.com/p/${mediaUi.media.code}/",
                    profileUsername = currentLoggedInUser.value.username,
                    isDownloading = false,
                    mediaType = mediaUi.media.mediaType!!,
                    instagramId = mediaUi.media.id!!,
                    createdAt = System.currentTimeMillis(),
                    isMassDownload = true,
                )

                postToDownload.mediaToDownload = itemsToDownload

                if (itemsToDownload.isNotEmpty()) {
                    postsToDownload.add(postToDownload)
                } else {
                    Timber.tag(TAG).v("The list of media items to download is empty")
                }
                insertPostUseCase.insertExtractedExplorePostAndMedia(
                    mediaUi.media,
                    currentLoggedInUser.value
                )
            }

            val newMassDownloadCount =
                currentRemoteUser?.remainingMassDownloadCount!! - items.count()
            currentRemoteUser =
                updateRemoteUser.updateRemainingMassDownloadCount(
                    newMassDownloadCount = newMassDownloadCount,
                    userDto = currentRemoteUser!!
                )

            if (postsToDownload.isNotEmpty()) {
                _exploreState.update {
                    it.copy(
                        massMediaToDownload = postsToDownload,
                        isMassDownloading = true
                    )
                }
            } else {
                Timber.tag(TAG).v("List of Mass Downloading items is empty")
            }
            _exploreState.update { it.copy(isMassDownloading = false) }
        }
    }

    // endregion

    // region [CollectionFragment]

    fun handleCollectionMassDownload(items: List<CollectionUiModel.MediaItem>) {
        viewModelScope.launch {
            _collectionState.update {
                it.copy(
                    isMassDownloading = true,
                    massMediaToDownload = null
                )
            }
            val postsToDownload = arrayListOf<PostToDownload>()

            items.forEach { mediaUi ->
                val itemsToDownload = arrayListOf<MediaToDownload>()
                when (mediaUi.collectionItem.mediaType) {
                    1 -> {
                        itemsToDownload.add(
                            MediaToDownload(
                                url = mediaUi.collectionItem.imageVersions2?.candidates?.first()?.url!!,
                                fileName = mediaUi.collectionItem.id!!,
                                profileUsername = currentLoggedInUser.value.username,
                                mediaType = MediaType.IMAGE.toString(),
                                createdAt = System.currentTimeMillis(),
                                instagramId = mediaUi.collectionItem.id,
                            )
                        )
                    }

                    2 -> {
                        itemsToDownload.add(
                            MediaToDownload(
                                url = mediaUi.collectionItem.videoVersions?.first()?.url!!,
                                fileName = mediaUi.collectionItem.id!!,
                                profileUsername = currentLoggedInUser.value.username,
                                mediaType = MediaType.VIDEO.toString(),
                                createdAt = System.currentTimeMillis(),
                                instagramId = mediaUi.collectionItem.id,
                            )
                        )
                    }

                    8 -> {
                        mediaUi.collectionItem.carouselMedias?.forEachIndexed { _, carouselMediaItem ->
                            if (carouselMediaItem.mediaType == 1) {
                                itemsToDownload.add(
                                    MediaToDownload(
                                        url = carouselMediaItem.imageVersions2?.candidates?.first()?.url!!,
                                        fileName = carouselMediaItem.id!!,
                                        profileUsername = currentLoggedInUser.value.username,
                                        mediaType = MediaType.IMAGE.toString(),
                                        createdAt = System.currentTimeMillis(),
                                        instagramId = carouselMediaItem.id,
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
                }
                val postToDownload = PostToDownload(
                    url = "https://instagram.com/p/${mediaUi.collectionItem.code}/",
                    profileUsername = currentLoggedInUser.value.username,
                    isDownloading = false,
                    mediaType = mediaUi.collectionItem.mediaType!!,
                    instagramId = mediaUi.collectionItem.id!!,
                    createdAt = System.currentTimeMillis(),
                    isMassDownload = true,
                )

                postToDownload.mediaToDownload = itemsToDownload

                if (itemsToDownload.isNotEmpty()) {
                    postsToDownload.add(postToDownload)
                } else {
                    Timber.tag(TAG).v("The list of media items to download is empty")
                }
                insertPostUseCase.insertExtractedCollectionPostAndMedia(
                    mediaUi.collectionItem,
                    currentLoggedInUser.value
                )
            }

            val newMassDownloadCount =
                currentRemoteUser?.remainingMassDownloadCount!! - items.count()
            currentRemoteUser =
                updateRemoteUser.updateRemainingMassDownloadCount(
                    newMassDownloadCount = newMassDownloadCount,
                    userDto = currentRemoteUser!!
                )

            if (postsToDownload.isNotEmpty()) {
                _collectionState.update {
                    it.copy(
                        massMediaToDownload = postsToDownload,
                        isMassDownloading = true
                    )
                }
            } else {
                Timber.tag(TAG).v("Collection Mass Downloading items is empty")
            }
            _collectionState.update { it.copy(isMassDownloading = false) }

        }
    }

    // endregion

    fun updateRemoteUser() {
        viewModelScope.launch {
            if (subscriptionLocalInfo.invoke().planTag.isNotBlank()) {
                currentRemoteUser = signUpOrGetRemoteUser.invoke(getId(VideoDownloaderApp.application))

                if (currentRemoteUser?.hasValidSubscription() == true) {
                    _homeChannel.emit(HomeAction.HideAds)
                    _exploreChannel.emit(ExploreAction.HideAds)
                    _collectionChannel.emit(CollectionAction.HideAds)
                }
            }
        }
    }

}