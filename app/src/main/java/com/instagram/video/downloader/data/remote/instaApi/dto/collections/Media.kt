package com.instagram.video.downloader.data.remote.instaApi.dto.collections

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class CollectionMedia(

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

//    @field:SerializedName("coauthor_producers")
//	val coauthorProducers: List<Any?>? = null,

//    @field:SerializedName("is_paid_partnership")
//    val isPaidPartnership: Boolean? = null,

//    @field:SerializedName("is_third_party_downloads_eligible")
//    val isThirdPartyDownloadsEligible: Boolean? = null,

//    @field:SerializedName("boost_unavailable_identifier")
//	val boostUnavailableIdentifier: Any? = null,

//    @field:SerializedName("commerciality_status")
//    val commercialityStatus: String? = null,

//    @field:SerializedName("organic_tracking_token")
//    val organicTrackingToken: String? = null,

//    @field:SerializedName("is_post_live_clips_media")
//    val isPostLiveClipsMedia: Boolean? = null,

    @field:SerializedName("taken_at")
    val takenAt: Long? = null,

//    @field:SerializedName("saved_collection_ids")
//	val savedCollectionIds: List<Any?>? = null,
//
//    @field:SerializedName("boost_unavailable_reason_v2")
//	val boostUnavailableReasonV2: List<Any?>? = null,

    @field:SerializedName("image_versions2")
    val imageVersions2: ImageVersions2? = null,

//    @field:SerializedName("should_request_ads")
//    val shouldRequestAds: Boolean? = null,

//    @field:SerializedName("open_carousel_show_follow_button")
//    val openCarouselShowFollowButton: Boolean? = null,

//    @field:SerializedName("should_show_author_pog_for_tagged_media_shared_to_profile_grid")
//    val shouldShowAuthorPogForTaggedMediaSharedToProfileGrid: Boolean? = null,

//    @field:SerializedName("clips_metadata")
//	val clipsMetadata: com.instagram.video.downloader.data.remote.instaApi.dto.collections.ClipsMetadata? = null,

//    @field:SerializedName("profile_grid_control_enabled")
//    val profileGridControlEnabled: Boolean? = null,

//    @field:SerializedName("is_eligible_for_meta_ai_share")
//    val isEligibleForMetaAiShare: Boolean? = null,

//    @field:SerializedName("max_num_visible_preview_comments")
//    val maxNumVisiblePreviewComments: Int? = null,

//    @field:SerializedName("filter_type")
//    val filterType: Int? = null,

//    @field:SerializedName("is_eligible_for_media_note_recs_nux")
//    val isEligibleForMediaNoteRecsNux: Boolean? = null,

//    @field:SerializedName("deleted_reason")
//    val deletedReason: Int? = null,

//    @field:SerializedName("original_height")
//    val originalHeight: Int? = null,

//    @field:SerializedName("fb_play_count")
//    val fbPlayCount: Int? = null,

    @field:SerializedName("video_versions", alternate = ["video_versions2"])
    val videoVersions: List<VideoVersionsItem?>? = null,

//    @field:SerializedName("has_viewer_saved")
//    val hasViewerSaved: Boolean? = null,

//    @field:SerializedName("has_high_risk_gen_ai_inform_treatment")
//    val hasHighRiskGenAiInformTreatment: Boolean? = null,

//    @field:SerializedName("is_visual_reply_commenter_notice_enabled")
//    val isVisualReplyCommenterNoticeEnabled: Boolean? = null,

//    @field:SerializedName("coauthor_producer_can_see_organic_insights")
//    val coauthorProducerCanSeeOrganicInsights: Boolean? = null,

//    @field:SerializedName("inline_composer_display_condition")
//    val inlineComposerDisplayCondition: String? = null,
//
//    @field:SerializedName("video_dash_manifest")
//    val videoDashManifest: String? = null,
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
//    @field:SerializedName("share_count_disabled")
//    val shareCountDisabled: Boolean? = null,

    @field:SerializedName("like_count")
    val likeCount: Long? = null,

//    @field:SerializedName("is_unified_video")
//    val isUnifiedVideo: Boolean? = null,
//
//    @field:SerializedName("is_cutout_sticker_allowed")
//    val isCutoutStickerAllowed: Boolean? = null,
//
//    @field:SerializedName("is_dash_eligible")
//    val isDashEligible: Int? = null,
//
//    @field:SerializedName("number_of_qualities")
//    val numberOfQualities: Int? = null,
//
    @field:SerializedName("play_count")
    val playCount: Int? = null,

//    @field:SerializedName("enable_media_notes_production")
//    val enableMediaNotesProduction: Boolean? = null,
//
//    @field:SerializedName("is_quiet_post")
//    val isQuietPost: Boolean? = null,

//    @field:SerializedName("media_notes")
//	val mediaNotes: com.instagram.video.downloader.data.remote.instaApi.dto.collections.MediaNotes? = null,

//    @field:SerializedName("caption_is_edited")
//    val captionIsEdited: Boolean? = null,
//
//    @field:SerializedName("can_viewer_save")
//    val canViewerSave: Boolean? = null,
//
    @field:SerializedName("video_duration")
    val videoDuration: Double? = null,

    @field:SerializedName("code")
    val code: String? = null,

//    @field:SerializedName("has_audio")
//    val hasAudio: Boolean? = null,
//
//    @field:SerializedName("can_view_more_preview_comments")
//    val canViewMorePreviewComments: Boolean? = null,
//
//    @field:SerializedName("commerce_integrity_review_decision")
//    val commerceIntegrityReviewDecision: String? = null,
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
//    @field:SerializedName("is_organic_product_tagging_eligible")
//    val isOrganicProductTaggingEligible: Boolean? = null,
//
//    @field:SerializedName("like_and_view_counts_disabled")
//    val likeAndViewCountsDisabled: Boolean? = null,

    @field:SerializedName("product_type")
    val productType: String? = null,

//    @field:SerializedName("can_reply")
//    val canReply: Boolean? = null,

    @field:SerializedName("fbid")
    val fbid: String? = null,

    @field:SerializedName("pk")
    val pk: String? = null,

//    @field:SerializedName("is_in_profile_grid")
//    val isInProfileGrid: Boolean? = null,

//    @field:SerializedName("fb_aggregated_like_count")
//    val fbAggregatedLikeCount: Int? = null,

    @field:SerializedName("user")
    val user: User? = null,

//    @field:SerializedName("has_more_comments")
//    val hasMoreComments: Boolean? = null,
//
//    @field:SerializedName("video_subtitles_uri")
//    val videoSubtitlesUri: String? = null,

//    @field:SerializedName("creative_config")
//	val creativeConfig: CreativeConfig? = null,

//    @field:SerializedName("is_artist_pick")
//    val isArtistPick: Boolean? = null,

//    @field:SerializedName("video_subtitles_locale")
//    val videoSubtitlesLocale: String? = null,

//    @field:SerializedName("usertags")
//	val usertags: Usertags? = null,

//    @field:SerializedName("should_open_collab_bottomsheet_on_facepile_tap")
//    val shouldOpenCollabBottomsheetOnFacepileTap: Boolean? = null,

//    @field:SerializedName("photo_of_you")
//    val photoOfYou: Boolean? = null,

//    @field:SerializedName("collab_follow_button_info")
//	val collabFollowButtonInfo: CollabFollowButtonInfo? = null,

    @field:SerializedName("carousel_media")
    val carouselMedias: List<CollectionCarouselItem>? = null,

    ) : Serializable {

    var isDownloaded = false


    var isDownloading = false


    var isChecked = false


    var isCheckable = false

    var downloadProgress = 0.0
}