package com.instagram.video.downloader.data.remote.instaApi.dto.explore

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.Serializable

fun ExploreResponse.toExplorePage(): ExplorePage {

    val sectionalItems = this.sectionalItems

    val section1 =
        sectionalItems?.flatMap {
            val content = it?.layoutContent
            val onByTwoItem = content?.oneByTwoItem
            val clips = onByTwoItem?.clips
            val itemsItems = clips?.items
            itemsItems?.map { item -> item.media } ?: emptyList()
        } ?: emptyList()

    val section2 =
        sectionalItems?.flatMap { it?.layoutContent?.fillItems?.map { item -> item.media } ?: emptyList() } ?: emptyList()

    return ExplorePage(
        maxId = maxId!!,
        mediaList = section2.plus(section1)
    )
}

data class ExplorePage(
    val maxId: String,
    val mediaList: List<Media>?,
)

data class ExploreResponse(

//    @field:SerializedName("session_paging_token")
//    val sessionPagingToken: String? = null,

    @field:SerializedName("sectional_items")
    val sectionalItems: List<SectionalItemsItem?>? = null,

//    @field:SerializedName("rank_token")
//    val rankToken: String? = null,

    @field:SerializedName("more_available")
    val moreAvailable: Boolean? = null,

    @field:SerializedName("max_id")
    val maxId: String? = null,

    @field:SerializedName("auto_load_more_enabled")
    val autoLoadMoreEnabled: Boolean? = null,

    @field:SerializedName("next_max_id")
    val nextMaxId: String? = null,

//    @field:SerializedName("clusters")
//    val clusters: List<ClustersItem?>? = null,

    @field:SerializedName("status")
    val status: String? = null
)


@Parcelize
data class CandidatesItem(

    @field:SerializedName("width")
    val width: Int? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("height")
    val height: Int? = null,

//    @field:SerializedName("scans_profile")
//    val scansProfile: String? = null
) : Parcelable, Serializable

data class MusicMetadata(

    @field:SerializedName("music_info")
    val musicInfo: MusicInfo? = null,

    @field:SerializedName("music_canonical_id")
    val musicCanonicalId: String? = null,

    @field:SerializedName("pinned_media_ids")
    val pinnedMediaIds: List<Any?>? = null,

    @field:SerializedName("original_sound_info")
    val originalSoundInfo: Any? = null,

    @field:SerializedName("audio_type")
    val audioType: String? = null
)

data class LayoutContent(

    @field:SerializedName("fill_items")
    val fillItems: List<FillItemsItem>,

    @field:SerializedName("one_by_two_item")
    val oneByTwoItem: OneByTwoItem? = null
)


@Parcelize
data class VideoVersionsItem(

    @field:SerializedName("width")
    val width: Int? = null,

//    @field:SerializedName("id")
//    val id: String? = null,

//    @field:SerializedName("type")
//    val type: Int? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("height")
    val height: Int? = null
) : Parcelable, Serializable

@Parcelize
data class ImageVersions2(

    @field:SerializedName("candidates")
    val candidates: List<CandidatesItem?>? = null,
) : Parcelable, Serializable


data class ClustersItem(

    @field:SerializedName("debug_info")
    val debugInfo: String? = null,

    @field:SerializedName("context")
    val context: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("description")
    val description: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("can_mute")
    val canMute: Boolean? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("is_muted")
    val isMuted: Boolean? = null,

    @field:SerializedName("labels")
    val labels: List<Any?>? = null
)

@Parcelize
data class Media(

    @field:SerializedName("comment_count")
    val commentCount: Long? = null,

//    @field:SerializedName("has_liked")
//    val hasLiked: Boolean? = null,

//    @field:SerializedName("has_shared_to_fb")
//    val hasSharedToFb: Int? = null,

    @field:SerializedName("caption")
    val caption: Caption? = null,

//    @field:SerializedName("can_viewer_reshare")
//    val canViewerReshare: Boolean? = null,

//    @field:SerializedName("is_comments_gif_composer_enabled")
//    val isCommentsGifComposerEnabled: Boolean? = null,

//    @field:SerializedName("explore_hide_comments")
//    val exploreHideComments: Boolean? = null,

    @field:SerializedName("media_type")
    val mediaType: Int? = null,

//    @field:SerializedName("has_privately_liked")
//    val hasPrivatelyLiked: Boolean? = null,

//    @field:SerializedName("comment_threading_enabled")
//    val commentThreadingEnabled: Boolean? = null,

    @field:SerializedName("id")
    val id: String? = null,

//    @field:SerializedName("algorithm")
//    val algorithm: String? = null,

//    @field:SerializedName("is_paid_partnership")
//    val isPaidPartnership: Boolean? = null,
//
//    @field:SerializedName("commerciality_status")
//    val commercialityStatus: String? = null,
//
//    @field:SerializedName("organic_tracking_token")
//    val organicTrackingToken: String? = null,
//
//    @field:SerializedName("commenting_disabled_for_viewer")
//    val commentingDisabledForViewer: Boolean? = null,
//
//    @field:SerializedName("is_post_live_clips_media")
//    val isPostLiveClipsMedia: Boolean? = null,

    @field:SerializedName("taken_at")
    val takenAt: Long? = null,

    @field:SerializedName("image_versions2")
    val imageVersions2: ImageVersions2? = null,

//    @field:SerializedName("should_request_ads")
//    val shouldRequestAds: Boolean? = null,
//
//    @field:SerializedName("open_carousel_show_follow_button")
//    val openCarouselShowFollowButton: Boolean? = null,
//
//    @field:SerializedName("should_show_author_pog_for_tagged_media_shared_to_profile_grid")
//    val shouldShowAuthorPogForTaggedMediaSharedToProfileGrid: Boolean? = null,
//
//    @field:SerializedName("profile_grid_control_enabled")
//    val profileGridControlEnabled: Boolean? = null,
//
//    @field:SerializedName("is_eligible_for_meta_ai_share")
//    val isEligibleForMetaAiShare: Boolean? = null,
//
//    @field:SerializedName("max_num_visible_preview_comments")
//    val maxNumVisiblePreviewComments: Int? = null,
//
//    @field:SerializedName("filter_type")
//    val filterType: Int? = null,
//
//    @field:SerializedName("is_eligible_for_media_note_recs_nux")
//    val isEligibleForMediaNoteRecsNux: Boolean? = null,
//
//    @field:SerializedName("deleted_reason")
//    val deletedReason: Int? = null,
//
//    @field:SerializedName("original_height")
//    val originalHeight: Int? = null,
//
//    @field:SerializedName("can_see_insights_as_brand")
//    val canSeeInsightsAsBrand: Boolean? = null,
//
//    @field:SerializedName("integrity_review_decision")
//    val integrityReviewDecision: String? = null,
//
//    @field:SerializedName("fb_aggregated_comment_count")
//    val fbAggregatedCommentCount: Int? = null,
//
//    @field:SerializedName("explore")
//    val explore: Explore? = null,

//    @field:SerializedName("has_delayed_metadata")
//    val hasDelayedMetadata: Boolean? = null,
//
//    @field:SerializedName("ig_media_sharing_disabled")
//    val igMediaSharingDisabled: Boolean? = null,
//
//    @field:SerializedName("subscribe_cta_visible")
//    val subscribeCtaVisible: Boolean? = null,
//
//    @field:SerializedName("top_likers")
//    val topLikers: List<String?>? = null,
//
//    @field:SerializedName("client_cache_key")
//    val clientCacheKey: String? = null,
//
//    @field:SerializedName("connection_id")
//    val connectionId: String? = null,
//
//    @field:SerializedName("media_level_comment_controls")
//    val mediaLevelCommentControls: String? = null,
//
//    @field:SerializedName("hide_view_all_comment_entrypoint")
//    val hideViewAllCommentEntrypoint: Boolean? = null,
//
//    @field:SerializedName("is_open_to_public_submission")
//    val isOpenToPublicSubmission: Boolean? = null,
//
//    @field:SerializedName("logging_info_token")
//    val loggingInfoToken: String? = null,
//
//    @field:SerializedName("impression_token")
//    val impressionToken: String? = null,
//
//    @field:SerializedName("has_high_risk_gen_ai_inform_treatment")
//    val hasHighRiskGenAiInformTreatment: Boolean? = null,
//
//    @field:SerializedName("is_visual_reply_commenter_notice_enabled")
//    val isVisualReplyCommenterNoticeEnabled: Boolean? = null,
//
//    @field:SerializedName("device_timestamp")
//    val deviceTimestamp: Long? = null,
//
//    @field:SerializedName("is_reshare_of_text_post_app_media_in_ig")
//    val isReshareOfTextPostAppMediaInIg: Boolean? = null,
//
//    @field:SerializedName("mezql_token")
//    val mezqlToken: String? = null,
//
//    @field:SerializedName("comments_disabled")
//    val commentsDisabled: Boolean? = null,
//

    @field:SerializedName("like_count")
    val likeCount: Long? = null,

//    @field:SerializedName("is_unified_video")
//    val isUnifiedVideo: Boolean? = null,

//    @field:SerializedName("is_cutout_sticker_allowed")
//    val isCutoutStickerAllowed: Boolean? = null,

//    @field:SerializedName("photo_of_you")
//    val photoOfYou: Boolean? = null,

//    @field:SerializedName("enable_media_notes_production")
//    val enableMediaNotesProduction: Boolean? = null,

//    @field:SerializedName("caption_is_edited")
//    val captionIsEdited: Boolean? = null,
//
//    @field:SerializedName("can_viewer_save")
//    val canViewerSave: Boolean? = null,
//
//    @field:SerializedName("original_media_has_visual_reply_media")
//    val originalMediaHasVisualReplyMedia: Boolean? = null,

    @field:SerializedName("code")
    val code: String? = null,

//    @field:SerializedName("enable_waist")
//    val enableWaist: Boolean? = null,
//
//    @field:SerializedName("recommendation_data")
//    val recommendationData: String? = null,
//
//    @field:SerializedName("is_social_ufi_disabled")
//    val isSocialUfiDisabled: Boolean? = null,
//
//    @field:SerializedName("is_tagged_media_shared_to_viewer_profile_grid")
//    val isTaggedMediaSharedToViewerProfileGrid: Boolean? = null,
//
//    @field:SerializedName("strong_id__")
//    val strongId: String? = null,

    @field:SerializedName("owner")
    val owner: Owner? = null,

//    @field:SerializedName("original_width")
//    val originalWidth: Int? = null,
//
//    @field:SerializedName("inventory_source")
//    val inventorySource: String? = null,
//
//    @field:SerializedName("is_organic_product_tagging_eligible")
//    val isOrganicProductTaggingEligible: Boolean? = null,
//
//    @field:SerializedName("like_and_view_counts_disabled")
//    val likeAndViewCountsDisabled: Boolean? = null,
//
    @field:SerializedName("product_type")
    val productType: String? = null,

//    @field:SerializedName("explore_context")
//    val exploreContext: String? = null,

    @field:SerializedName("fbid")
    val fbid: Long? = null,

    @field:SerializedName("pk")
    val pk: Long? = null,

//    @field:SerializedName("is_in_profile_grid")
//    val isInProfileGrid: Boolean? = null,
//
//    @field:SerializedName("fb_aggregated_like_count")
//    val fbAggregatedLikeCount: Int? = null,

    @field:SerializedName("user")
    val user: User? = null,

//    @field:SerializedName("preview_comments")
//    val previewComments: List<String?>? = null,
//
//    @field:SerializedName("can_view_more_preview_comments")
//    val canViewMorePreviewComments: Boolean? = null,
//
//    @field:SerializedName("comments")
//    val comments: List<String?>? = null,

//    @field:SerializedName("has_more_comments")
//    val hasMoreComments: Boolean? = null,

//    @field:SerializedName("is_third_party_downloads_eligible")
//    val isThirdPartyDownloadsEligible: Boolean? = null,

    @field:SerializedName("video_versions")
    val videoVersions: List<VideoVersionsItem?>? = null,

//    @field:SerializedName("are_remixes_crosspostable")
//    val areRemixesCrosspostable: Boolean? = null,
//
//    @field:SerializedName("is_spinnable")
//    val isSpinnable: Boolean? = null,
//
//    @field:SerializedName("video_dash_manifest")
//    val videoDashManifest: String? = null,

//    @field:SerializedName("is_dash_eligible")
//    val isDashEligible: Int? = null,
//
//    @field:SerializedName("number_of_qualities")
//    val numberOfQualities: Int? = null,
//
    @field:SerializedName("play_count")
    val playCount: Int? = null,

//    @field:SerializedName("video_duration")
//    val videoDuration: Double? = null,
//
//    @field:SerializedName("is_artist_pick")
//    val isArtistPick: Boolean? = null,
//
//    @field:SerializedName("has_audio")
//    val hasAudio: Boolean? = null,
//
//    @field:SerializedName("commerce_integrity_review_decision")
//    val commerceIntegrityReviewDecision: String? = null,
//
//    @field:SerializedName("video_codec")
//    val videoCodec: String? = null,

    @field:SerializedName("carousel_media")
    val carouselMedia: List<CarouselMediaItem?>? = null,

//    @field:SerializedName("carousel_media_ids")
//    val carouselMediaIds: List<Long?>? = null,

//    @field:SerializedName("carousel_media_pending_post_count")
//    val carouselMediaPendingPostCount: Int? = null,

//    @field:SerializedName("carousel_media_count")
//    val carouselMediaCount: Int? = null,
//
//    @field:SerializedName("open_carousel_submission_state")
//    val openCarouselSubmissionState: String? = null,

//    @field:SerializedName("ranked_at")
//    val rankedAt: Int? = null,

//    @field:SerializedName("view_state_item_type")
//    val viewStateItemType: Int? = null,
//
//    @field:SerializedName("is_reuse_allowed")
//    val isReuseAllowed: Boolean? = null,
//
//    @field:SerializedName("video_subtitles_locale")
//    val videoSubtitlesLocale: String? = null,
//
//
//    @field:SerializedName("timeline_pinned_user_ids")
//    val timelinePinnedUserIds: List<Long?>? = null,

//    @field:SerializedName("fb_play_count")
//    val fbPlayCount: Int? = null,
//
//    @field:SerializedName("video_subtitles_uri")
//    val videoSubtitlesUri: String? = null,

    ) : Parcelable, Serializable {

    @IgnoredOnParcel
    var isDownloaded = false

    @IgnoredOnParcel
    var isDownloading = false

    @IgnoredOnParcel
    var isChecked = false

    @IgnoredOnParcel
    var isCheckable = false

    @IgnoredOnParcel
    var downloadProgress = 0.0
}

@Parcelize
data class Owner(

//    @field:SerializedName("is_private")
//    val isPrivate: Boolean? = null,

//    @field:SerializedName("account_type")
//    val accountType: Int? = null,

//    @field:SerializedName("pk_id")
//    val pkId: String? = null,

//    @field:SerializedName("is_favorite")
//    val isFavorite: Boolean? = null,

//    @field:SerializedName("show_account_transparency_details")
//    val showAccountTransparencyDetails: Boolean? = null,

//    @field:SerializedName("third_party_downloads_enabled")
//    val thirdPartyDownloadsEnabled: Int? = null,

//    @field:SerializedName("fbid_v2")
//    val fbidV2: String? = null,

//    @field:SerializedName("transparency_product_enabled")
//    val transparencyProductEnabled: Boolean? = null,

    @field:SerializedName("hd_profile_pic_url_info")
    val hdProfilePicUrlInfo: HdProfilePicUrlInfo? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("profile_pic_url")
    val profilePicUrl: String? = null,

//    @field:SerializedName("strong_id__")
//    val strongId: String? = null,

    @field:SerializedName("profile_pic_id")
    val profilePicId: String? = null,

//    @field:SerializedName("has_anonymous_profile_picture")
//    val hasAnonymousProfilePicture: Boolean? = null,

    @field:SerializedName("is_verified")
    val isVerified: Boolean? = null,

    @field:SerializedName("hd_profile_pic_versions")
    val hdProfilePicVersions: List<HdProfilePicVersionsItem?>? = null,

    @field:SerializedName("full_name")
    val fullName: String? = null,

//    @field:SerializedName("is_unpublished")
//    val isUnpublished: Boolean? = null,

//    @field:SerializedName("feed_post_reshare_disabled")
//    val feedPostReshareDisabled: Boolean? = null,

    @field:SerializedName("pk")
    val pk: String? = null,

//    @field:SerializedName("latest_reel_media")
//    val latestReelMedia: Int? = null,

    @field:SerializedName("username")
    val username: String? = null
) : Parcelable, Serializable

data class MusicConsumptionInfo(

    @field:SerializedName("previous_trend_rank")
    val previousTrendRank: Any? = null,

    @field:SerializedName("display_labels")
    val displayLabels: Any? = null,

    @field:SerializedName("should_allow_music_editing")
    val shouldAllowMusicEditing: Boolean? = null,

    @field:SerializedName("overlap_duration_in_ms")
    val overlapDurationInMs: Int? = null,

    @field:SerializedName("should_mute_audio_reason")
    val shouldMuteAudioReason: String? = null,

    @field:SerializedName("should_render_soundwave")
    val shouldRenderSoundwave: Boolean? = null,

    @field:SerializedName("derived_content_id")
    val derivedContentId: Any? = null,

    @field:SerializedName("is_trending_in_clips")
    val isTrendingInClips: Boolean? = null,

    @field:SerializedName("audio_asset_start_time_in_ms")
    val audioAssetStartTimeInMs: Int? = null,

    @field:SerializedName("should_mute_audio")
    val shouldMuteAudio: Boolean? = null,

    @field:SerializedName("audio_muting_info")
    val audioMutingInfo: AudioMutingInfo? = null,

    @field:SerializedName("audio_filter_infos")
    val audioFilterInfos: List<Any?>? = null,

    @field:SerializedName("contains_lyrics")
    val containsLyrics: Any? = null,

    @field:SerializedName("should_mute_audio_reason_type")
    val shouldMuteAudioReasonType: Any? = null,

    @field:SerializedName("ig_artist")
    val igArtist: IgArtist? = null,

    @field:SerializedName("is_bookmarked")
    val isBookmarked: Boolean? = null,

    @field:SerializedName("trend_rank")
    val trendRank: Any? = null,

    @field:SerializedName("allow_media_creation_with_music")
    val allowMediaCreationWithMusic: Boolean? = null,

    @field:SerializedName("formatted_clips_media_count")
    val formattedClipsMediaCount: Any? = null,

    @field:SerializedName("placeholder_profile_pic_url")
    val placeholderProfilePicUrl: String? = null
)

data class SectionalItemsItem(

    @field:SerializedName("layout_content")
    val layoutContent: LayoutContent? = null,

    @field:SerializedName("explore_item_info")
    val exploreItemInfo: ExploreItemInfo? = null,

//    @field:SerializedName("layout_type")
//    val layoutType: String? = null,

//    @field:SerializedName("feed_type")
//    val feedType: String? = null
)

data class IgArtist(

    @field:SerializedName("is_private")
    val isPrivate: Boolean? = null,

    @field:SerializedName("pk_id")
    val pkId: String? = null,

    @field:SerializedName("full_name")
    val fullName: String? = null,

    @field:SerializedName("profile_pic_id")
    val profilePicId: String? = null,

    @field:SerializedName("pk")
    val pk: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("is_verified")
    val isVerified: Boolean? = null,

    @field:SerializedName("profile_pic_url")
    val profilePicUrl: String? = null,

    @field:SerializedName("username")
    val username: String? = null,

    @field:SerializedName("strong_id__")
    val strongId: String? = null
)

@Parcelize
data class Caption(

//    @field:SerializedName("private_reply_status")
//    val privateReplyStatus: Int? = null,

//    @field:SerializedName("share_enabled")
//    val shareEnabled: Boolean? = null,

//    @field:SerializedName("created_at")
//    val createdAt: Int? = null,

//    @field:SerializedName("type")
//    val type: Int? = null,

//    @field:SerializedName("is_covered")
//    val isCovered: Boolean? = null,

//    @field:SerializedName("created_at_utc")
//    val createdAtUtc: Int? = null,

//    @field:SerializedName("has_translation")
//    val hasTranslation: Boolean? = null,

//    @field:SerializedName("content_type")
//    val contentType: String? = null,

//    @field:SerializedName("user_id")
//    val userId: String? = null,

//    @field:SerializedName("bit_flags")
//    val bitFlags: Int? = null,

//    @field:SerializedName("media_id")
//    val mediaId: String? = null,

    @field:SerializedName("pk")
    val pk: String? = null,

    @field:SerializedName("text")
    val text: String? = null,

//    @field:SerializedName("did_report_as_spam")
//    val didReportAsSpam: Boolean? = null,

//    @field:SerializedName("user")
//    val user: User? = null,

//    @field:SerializedName("is_ranked_comment")
//    val isRankedComment: Boolean? = null,

//    @field:SerializedName("status")
//    val status: String? = null,

//    @field:SerializedName("strong_id__")
//    val strongId: String? = null
) : Parcelable, Serializable

data class Clips(

    @field:SerializedName("chaining_info")
    val chainingInfo: Any? = null,

    @field:SerializedName("content_source")
    val contentSource: String? = null,

    @field:SerializedName("design")
    val design: String? = null,

    @field:SerializedName("more_available")
    val moreAvailable: Boolean? = null,

    @field:SerializedName("max_id")
    val maxId: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("label")
    val label: String? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("items")
    val items: List<ItemsItem>,

    @field:SerializedName("badge_label")
    val badgeLabel: Any? = null
)

data class ItemsItem(

    @field:SerializedName("media")
    val media: Media
)

data class AudioMutingInfo(

    @field:SerializedName("mute_audio")
    val muteAudio: Boolean? = null,

    @field:SerializedName("allow_audio_editing")
    val allowAudioEditing: Boolean? = null,

    @field:SerializedName("mute_reason_str")
    val muteReasonStr: String? = null,

    @field:SerializedName("show_muted_audio_toast")
    val showMutedAudioToast: Boolean? = null
)

data class MusicInfo(

    @field:SerializedName("music_canonical_id")
    val musicCanonicalId: Any? = null,

    @field:SerializedName("music_asset_info")
    val musicAssetInfo: MusicAssetInfo? = null,

    @field:SerializedName("music_consumption_info")
    val musicConsumptionInfo: MusicConsumptionInfo? = null
)

data class FillItemsItem(

    @field:SerializedName("media")
    val media: Media
)

@Parcelize
data class HdProfilePicUrlInfo(

    @field:SerializedName("width")
    val width: Int? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("height")
    val height: Int? = null
) : Parcelable, Serializable


@Parcelize
data class CarouselMediaItem(

//    @field:SerializedName("carousel_parent_id")
//    val carouselParentId: String? = null,

//    @field:SerializedName("original_width")
//    val originalWidth: Int? = null,

//    @field:SerializedName("preview")
//    val preview: String? = null,

//    @field:SerializedName("accessibility_caption")
//    val accessibilityCaption: String? = null,

//    @field:SerializedName("commerciality_status")
//    val commercialityStatus: String? = null,

    @field:SerializedName("product_type")
    val productType: String? = null,

//    @field:SerializedName("taken_at")
//    val takenAt: Int? = null,

    @field:SerializedName("media_type")
    val mediaType: Int? = null,

    @field:SerializedName("explore_pivot_grid")
    val explorePivotGrid: Boolean? = null,

    @field:SerializedName("image_versions2")
    val imageVersions2: ImageVersions2? = null,

//    @field:SerializedName("original_height")
//    val originalHeight: Int? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("pk")
    val pk: String? = null,

    @field:SerializedName("strong_id__")
    val strongId: String? = null,

    @field:SerializedName("video_versions", alternate = ["video_versions2"])
    val videoVersions: List<VideoVersionsItem?>? = null,

    ) : Parcelable, Serializable

@Parcelize
data class User(

    @field:SerializedName("is_private")
    val isPrivate: Boolean? = null,

    @field:SerializedName("account_type")
    val accountType: Int? = null,

//    @field:SerializedName("pk_id")
//    val pkId: String? = null,

//    @field:SerializedName("is_favorite")
//    val isFavorite: Boolean? = null,

//    @field:SerializedName("show_account_transparency_details")
//    val showAccountTransparencyDetails: Boolean? = null,

//    @field:SerializedName("third_party_downloads_enabled")
//    val thirdPartyDownloadsEnabled: Int? = null,

    @field:SerializedName("fbid_v2")
    val fbidV2: String? = null,

//    @field:SerializedName("transparency_product_enabled")
//    val transparencyProductEnabled: Boolean? = null,

//    @field:SerializedName("hd_profile_pic_url_info")
//    val hdProfilePicUrlInfo: HdProfilePicUrlInfo? = null,


    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("profile_pic_url")
    val profilePicUrl: String? = null,

//    @field:SerializedName("strong_id__")
//    val strongId: String? = null,

    @field:SerializedName("profile_pic_id")
    val profilePicId: String? = null,

//    @field:SerializedName("has_anonymous_profile_picture")
//    val hasAnonymousProfilePicture: Boolean? = null,

    @field:SerializedName("is_verified")
    val isVerified: Boolean? = null,

    @field:SerializedName("hd_profile_pic_versions")
    val hdProfilePicVersions: List<HdProfilePicVersionsItem?>? = null,

    @field:SerializedName("full_name")
    val fullName: String? = null,

//    @field:SerializedName("is_unpublished")
//    val isUnpublished: Boolean? = null,

//    @field:SerializedName("feed_post_reshare_disabled")
//    val feedPostReshareDisabled: Boolean? = null,

    @field:SerializedName("pk")
    val pk: String? = null,

//    @field:SerializedName("latest_reel_media")
//    val latestReelMedia: Int? = null,

    @field:SerializedName("username")
    val username: String? = null
) : Parcelable, Serializable


//@Parcelize
//data class Explore(
//
//    @field:SerializedName("actor_id")
//    val actorId: String? = null,
//
//    @field:SerializedName("explanation")
//    val explanation: String? = null
//) : Parcelable

data class MusicAssetInfo(

    @field:SerializedName("dash_manifest")
    val dashManifest: Any? = null,

    @field:SerializedName("fast_start_progressive_download_url")
    val fastStartProgressiveDownloadUrl: String? = null,

    @field:SerializedName("audio_asset_id")
    val audioAssetId: String? = null,

    @field:SerializedName("cover_artwork_uri")
    val coverArtworkUri: String? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("progressive_download_url")
    val progressiveDownloadUrl: String? = null,

    @field:SerializedName("allows_saving")
    val allowsSaving: Boolean? = null,

    @field:SerializedName("is_eligible_for_audio_effects")
    val isEligibleForAudioEffects: Boolean? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("duration_in_ms")
    val durationInMs: Int? = null,

    @field:SerializedName("is_explicit")
    val isExplicit: Boolean? = null,

    @field:SerializedName("lyrics")
    val lyrics: Any? = null,

    @field:SerializedName("cover_artwork_thumbnail_uri")
    val coverArtworkThumbnailUri: String? = null,

    @field:SerializedName("display_artist")
    val displayArtist: String? = null,

    @field:SerializedName("sanitized_title")
    val sanitizedTitle: Any? = null,

    @field:SerializedName("dark_message")
    val darkMessage: Any? = null,

    @field:SerializedName("artist_id")
    val artistId: String? = null,

    @field:SerializedName("audio_cluster_id")
    val audioClusterId: String? = null,

    @field:SerializedName("is_eligible_for_vinyl_sticker")
    val isEligibleForVinylSticker: Boolean? = null,

    @field:SerializedName("reactive_audio_download_url")
    val reactiveAudioDownloadUrl: Any? = null,

    @field:SerializedName("subtitle")
    val subtitle: String? = null,

    @field:SerializedName("web_30s_preview_download_url")
    val web30sPreviewDownloadUrl: String? = null,

    @field:SerializedName("has_lyrics")
    val hasLyrics: Boolean? = null,

    @field:SerializedName("highlight_start_times_in_ms")
    val highlightStartTimesInMs: List<Int?>? = null,

    @field:SerializedName("ig_username")
    val igUsername: String? = null
)

@Parcelize
data class HdProfilePicVersionsItem(

    @field:SerializedName("width")
    val width: Int? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("height")
    val height: Int? = null
) : Parcelable, Serializable

data class ExploreItemInfo(

    @field:SerializedName("num_columns")
    val numColumns: Int? = null,

    @field:SerializedName("aspect_ratio")
    val aspectRatio: Any? = null,

    @field:SerializedName("total_num_columns")
    val totalNumColumns: Int? = null,

    @field:SerializedName("autoplay")
    val autoplay: Boolean? = null
)

data class OneByTwoItem(

    @field:SerializedName("clips")
    val clips: Clips? = null
)
