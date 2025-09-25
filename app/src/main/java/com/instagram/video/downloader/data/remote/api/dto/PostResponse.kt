package com.instagram.video.downloader.data.remote.api.dto

import com.google.gson.annotations.SerializedName

//data class PostResponse(
//
//    @field:SerializedName("data")
//    val data: Data? = null
//)

data class FanClubInfo(

    @field:SerializedName("fan_club_id")
    val fanClubId: Any? = null,

    @field:SerializedName("subscriber_count")
    val subscriberCount: Any? = null,

    @field:SerializedName("fan_consideration_page_revamp_eligiblity")
    val fanConsiderationPageRevampEligiblity: Any? = null,

    @field:SerializedName("is_fan_club_referral_eligible")
    val isFanClubReferralEligible: Any? = null,

    @field:SerializedName("autosave_to_exclusive_highlight")
    val autosaveToExclusiveHighlight: Any? = null,

    @field:SerializedName("connected_member_count")
    val connectedMemberCount: Any? = null,

    @field:SerializedName("fan_club_name")
    val fanClubName: Any? = null,

    @field:SerializedName("has_enough_subscribers_for_ssc")
    val hasEnoughSubscribersForSsc: Any? = null,

    @field:SerializedName("is_fan_club_gifting_eligible")
    val isFanClubGiftingEligible: Any? = null
)

data class CommentInformTreatment(

    @field:SerializedName("action_type")
    val actionType: Any? = null,

    @field:SerializedName("text")
    val text: String? = null,

    @field:SerializedName("url")
    val url: Any? = null,

    @field:SerializedName("should_have_inform_treatment")
    val shouldHaveInformTreatment: Boolean? = null
)

data class AdditionalAudioInfo(

    @field:SerializedName("audio_reattribution_info")
    val audioReattributionInfo: AudioReattributionInfo? = null,

    @field:SerializedName("additional_audio_username")
    val additionalAudioUsername: Any? = null
)

data class FbUserTags(

    @field:SerializedName("in")
    val inItem: List<Any?>? = null
)

data class AdditionalItems(

    @field:SerializedName("smart_frame")
    val smartFrame: Any? = null,

    @field:SerializedName("igtv_first_frame")
    val igtvFirstFrame: IgtvFirstFrame? = null,

    @field:SerializedName("first_frame")
    val firstFrame: FirstFrame? = null
)

data class PostResponse(

    @field:SerializedName("like_count")
    val likeCount: Long? = null,

    @field:SerializedName("comment_count")
    val commentCount: Long? = null,


    @field:SerializedName("carousel_media")
    val carouselMedia: List<CarouselMediaItem?>? = null,

    @field:SerializedName("carousel_media_count")
    val carouselMediaCount: Int? = null,

    @field:SerializedName("carousel_media_ids")
    val carouselMediaIds: List<Long?>? = null,


    @field:SerializedName("invited_coauthor_producers")
    val invitedCoauthorProducers: List<Any?>? = null,

    @field:SerializedName("featured_products")
    val featuredProducts: List<Any?>? = null,

    @field:SerializedName("has_liked")
    val hasLiked: Boolean? = null,

    @field:SerializedName("caption")
    val caption: Caption? = null,

    @field:SerializedName("has_shared_to_fb")
    val hasSharedToFb: Int? = null,

    @field:SerializedName("is_comments_gif_composer_enabled")
    val isCommentsGifComposerEnabled: Boolean? = null,

//    @field:SerializedName("product_suggestions")
//    val productSuggestions: List<Any?>? = null,

    @field:SerializedName("media_type")
    val mediaType: Int? = null,

    @field:SerializedName("has_privately_liked")
    val hasPrivatelyLiked: Boolean? = null,

    @field:SerializedName("comment_threading_enabled")
    val commentThreadingEnabled: Boolean? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("coauthor_producers")
    val coauthorProducers: List<Any?>? = null,

    @field:SerializedName("is_paid_partnership")
    val isPaidPartnership: Boolean? = null,

    @field:SerializedName("boost_unavailable_identifier")
    val boostUnavailableIdentifier: Any? = null,

    @field:SerializedName("is_third_party_downloads_eligible")
    val isThirdPartyDownloadsEligible: Boolean? = null,

    @field:SerializedName("commerciality_status")
    val commercialityStatus: String? = null,

//    @field:SerializedName("is_post_live_clips_media")
//    val isPostLiveClipsMedia: Boolean? = null,

    @field:SerializedName("taken_at")
    val takenAt: Long? = null,

    @field:SerializedName("boost_unavailable_reason_v2")
    val boostUnavailableReasonV2: Any? = null,

    @field:SerializedName("metrics")
    val metrics: Metrics? = null,

    @field:SerializedName("open_carousel_show_follow_button")
    val openCarouselShowFollowButton: Boolean? = null,

//    @field:SerializedName("should_show_author_pog_for_tagged_media_shared_to_profile_grid")
//    val shouldShowAuthorPogForTaggedMediaSharedToProfileGrid: Boolean? = null,

//    @field:SerializedName("fundraiser_tag")
//    val fundraiserTag: FundraiserTag? = null,

    @field:SerializedName("clips_metadata")
    val clipsMetadata: ClipsMetadata? = null,

    @field:SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,

    @field:SerializedName("is_pinned")
    val isPinned: Boolean? = null,

    @field:SerializedName("is_eligible_for_meta_ai_share")
    val isEligibleForMetaAiShare: Boolean? = null,

    @field:SerializedName("max_num_visible_preview_comments")
    val maxNumVisiblePreviewComments: Int? = null,

    @field:SerializedName("filter_type")
    val filterType: Int? = null,

//    @field:SerializedName("is_eligible_for_media_note_recs_nux")
//    val isEligibleForMediaNoteRecsNux: Boolean? = null,

    @field:SerializedName("deleted_reason")
    val deletedReason: Int? = null,

    @field:SerializedName("original_height")
    val originalHeight: Int? = null,

    @field:SerializedName("video_versions", alternate = ["video_versions2"])
    val videoVersions: List<VideoVersionsItem?>? = null,

    @field:SerializedName("fb_aggregated_comment_count")
    val fbAggregatedCommentCount: Int? = null,

    @field:SerializedName("integrity_review_decision")
    val integrityReviewDecision: String? = null,

    @field:SerializedName("are_remixes_crosspostable")
    val areRemixesCrosspostable: Boolean? = null,

    @field:SerializedName("gen_ai_detection_method")
    val genAiDetectionMethod: GenAiDetectionMethod? = null,

    @field:SerializedName("video_subtitles_uri")
    val videoSubtitlesUri: String? = null,

    @field:SerializedName("is_reuse_allowed")
    val isReuseAllowed: Boolean? = null,

    @field:SerializedName("ig_media_sharing_disabled")
    val igMediaSharingDisabled: Boolean? = null,

    @field:SerializedName("subscribe_cta_visible")
    val subscribeCtaVisible: Boolean? = null,

    @field:SerializedName("top_likers")
    val topLikers: List<Any?>? = null,

    @field:SerializedName("sharing_friction_info")
    val sharingFrictionInfo: SharingFrictionInfo? = null,

    @field:SerializedName("igbio_product")
    val igbioProduct: Any? = null,

    @field:SerializedName("boost_unavailable_reason")
    val boostUnavailableReason: Any? = null,

    @field:SerializedName("creator_viewer_insights")
    val creatorViewerInsights: List<Any?>? = null,

    @field:SerializedName("is_open_to_public_submission")
    val isOpenToPublicSubmission: Boolean? = null,

//    @field:SerializedName("media_cropping_info")
//    val mediaCroppingInfo: MediaCroppingInfo? = null,

    @field:SerializedName("has_high_risk_gen_ai_inform_treatment")
    val hasHighRiskGenAiInformTreatment: Boolean? = null,

    @field:SerializedName("coauthor_producer_can_see_organic_insights")
    val coauthorProducerCanSeeOrganicInsights: Boolean? = null,

    @field:SerializedName("inline_composer_display_condition")
    val inlineComposerDisplayCondition: String? = null,

    @field:SerializedName("device_timestamp")
    val deviceTimestamp: Long? = null,

    @field:SerializedName("is_reshare_of_text_post_app_media_in_ig")
    val isReshareOfTextPostAppMediaInIg: Boolean? = null,

    @field:SerializedName("comments_disabled")
    val commentsDisabled: Any? = null,

    @field:SerializedName("fb_user_tags")
    val fbUserTags: FbUserTags? = null,

    @field:SerializedName("media_name")
    val mediaName: String? = null,

    @field:SerializedName("share_count_disabled")
    val shareCountDisabled: Boolean? = null,

    @field:SerializedName("comment_inform_treatment")
    val commentInformTreatment: CommentInformTreatment? = null,

    @field:SerializedName("is_unified_video")
    val isUnifiedVideo: Boolean? = null,

    @field:SerializedName("is_cutout_sticker_allowed")
    val isCutoutStickerAllowed: Boolean? = null,

    @field:SerializedName("accessibility_caption")
    val accessibilityCaption: Any? = null,

    @field:SerializedName("is_dash_eligible")
    val isDashEligible: Int? = null,

    @field:SerializedName("clips_tab_pinned_user_ids")
    val clipsTabPinnedUserIds: List<Any?>? = null,

    @field:SerializedName("number_of_qualities")
    val numberOfQualities: Int? = null,

    @field:SerializedName("preview_comments")
    val previewComments: List<Any?>? = null,

    @field:SerializedName("is_quiet_post")
    val isQuietPost: Boolean? = null,

    @field:SerializedName("media_notes")
    val mediaNotes: MediaNotes? = null,

    @field:SerializedName("tagged_users")
    val taggedUsers: Any? = null,

    @field:SerializedName("is_video")
    val isVideo: Boolean? = null,

    @field:SerializedName("facepile_top_likers")
    val facepileTopLikers: List<Any?>? = null,

    @field:SerializedName("caption_is_edited")
    val captionIsEdited: Boolean? = null,

    @field:SerializedName("can_reshare")
    val canReshare: Boolean? = null,

    @field:SerializedName("video_duration")
    val videoDuration: Any? = null,

    @field:SerializedName("code")
    val code: String? = null,

    @field:SerializedName("is_artist_pick")
    val isArtistPick: Boolean? = null,

    @field:SerializedName("shop_routing_user_id")
    val shopRoutingUserId: Any? = null,

    @field:SerializedName("has_audio")
    val hasAudio: Boolean? = null,

    @field:SerializedName("inline_composer_imp_trigger_time")
    val inlineComposerImpTriggerTime: Int? = null,

    @field:SerializedName("video_url")
    val videoUrl: String? = null,

    @field:SerializedName("video_subtitles_locale")
    val videoSubtitlesLocale: String? = null,

    @field:SerializedName("video_subtitles_confidence")
    val videoSubtitlesConfidence: Any? = null,

    @field:SerializedName("is_social_ufi_disabled")
    val isSocialUfiDisabled: Boolean? = null,

    @field:SerializedName("image_versions", alternate = ["image_versions2"])
    val imageVersions: ImageVersions? = null,

    @field:SerializedName("is_tagged_media_shared_to_viewer_profile_grid")
    val isTaggedMediaSharedToViewerProfileGrid: Boolean? = null,

    @field:SerializedName("is_organic_product_tagging_eligible")
    val isOrganicProductTaggingEligible: Boolean? = null,

    @field:SerializedName("original_width")
    val originalWidth: Int? = null,

    @field:SerializedName("owner")
    val owner: Owner? = null,

    @field:SerializedName("video_codec")
    val videoCodec: String? = null,

    @field:SerializedName("like_and_view_counts_disabled")
    val likeAndViewCountsDisabled: Boolean? = null,

    @field:SerializedName("music_metadata")
    val musicMetadata: Any? = null,

    @field:SerializedName("can_reply")
    val canReply: Boolean? = null,

    @field:SerializedName("product_type")
    val productType: String? = null,

    @field:SerializedName("fbid")
    val fbid: String? = null,

    @field:SerializedName("can_save")
    val canSave: Boolean? = null,

    @field:SerializedName("location")
    val location: Any? = null,

    @field:SerializedName("pk")
    val pk: Long? = null,

    @field:SerializedName("fb_aggregated_like_count")
    val fbAggregatedLikeCount: Int? = null,

    @field:SerializedName("is_in_profile_grid")
    val isInProfileGrid: Boolean? = null,

    @field:SerializedName("has_more_comments")
    val hasMoreComments: Boolean? = null,

    @field:SerializedName("user")
    val user: MediaUser? = null
)

data class MashupInfo(

    @field:SerializedName("formatted_mashups_count")
    val formattedMashupsCount: Any? = null,

    @field:SerializedName("is_light_weight_reuse_allowed_check")
    val isLightWeightReuseAllowedCheck: Boolean? = null,

    @field:SerializedName("mashup_type")
    val mashupType: Any? = null,

    @field:SerializedName("has_been_mashed_up")
    val hasBeenMashedUp: Boolean? = null,

    @field:SerializedName("is_creator_requesting_mashup")
    val isCreatorRequestingMashup: Boolean? = null,

    @field:SerializedName("is_pivot_page_available")
    val isPivotPageAvailable: Boolean? = null,

    @field:SerializedName("mashups_allowed")
    val mashupsAllowed: Boolean? = null,

    @field:SerializedName("is_light_weight_check")
    val isLightWeightCheck: Boolean? = null,

    @field:SerializedName("is_reuse_allowed")
    val isReuseAllowed: Boolean? = null,

    @field:SerializedName("can_toggle_mashups_allowed")
    val canToggleMashupsAllowed: Boolean? = null,

    @field:SerializedName("privacy_filtered_mashups_media_count")
    val privacyFilteredMashupsMediaCount: Any? = null,

    @field:SerializedName("has_nonmimicable_additional_audio")
    val hasNonmimicableAdditionalAudio: Boolean? = null,

    @field:SerializedName("non_privacy_filtered_mashups_media_count")
    val nonPrivacyFilteredMashupsMediaCount: Int? = null,

    @field:SerializedName("original_media")
    val originalMedia: Any? = null
)

data class AudioReattributionInfo(

    @field:SerializedName("should_allow_restore")
    val shouldAllowRestore: Boolean? = null
)

data class MediaCroppingInfo(

    @field:SerializedName("square_crop")
    val squareCrop: SquareCrop? = null
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

data class BrandedContentTagInfo(

    @field:SerializedName("can_add_tag")
    val canAddTag: Boolean? = null
)

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

    @field:SerializedName("derived_content_id")
    val derivedContentId: Any? = null,

    @field:SerializedName("is_trending_in_clips")
    val isTrendingInClips: Boolean? = null,

    @field:SerializedName("audio_asset_start_time_in_ms")
    val audioAssetStartTimeInMs: Int? = null,

    @field:SerializedName("audio_muting_info")
    val audioMutingInfo: AudioMutingInfo? = null,

    @field:SerializedName("should_mute_audio")
    val shouldMuteAudio: Boolean? = null,

    @field:SerializedName("audio_filter_infos")
    val audioFilterInfos: List<Any?>? = null,

    @field:SerializedName("contains_lyrics")
    val containsLyrics: Any? = null,

    @field:SerializedName("should_mute_audio_reason_type")
    val shouldMuteAudioReasonType: Any? = null,

    @field:SerializedName("ig_artist")
    val igArtist: Any? = null,

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

data class Owner(

    @field:SerializedName("account_badges")
    val accountBadges: List<Any?>? = null,

    @field:SerializedName("is_private")
    val isPrivate: Boolean? = null,

    @field:SerializedName("account_type")
    val accountType: Int? = null,

    @field:SerializedName("is_favorite")
    val isFavorite: Boolean? = null,

    @field:SerializedName("show_account_transparency_details")
    val showAccountTransparencyDetails: Boolean? = null,

    @field:SerializedName("profile_pic_id")
    val profilePicId: String? = null,

    @field:SerializedName("third_party_downloads_enabled")
    val thirdPartyDownloadsEnabled: Int? = null,

    @field:SerializedName("fbid_v2")
    val fbidV2: Long? = null,

    @field:SerializedName("transparency_product_enabled")
    val transparencyProductEnabled: Boolean? = null,

    @field:SerializedName("fan_club_info")
    val fanClubInfo: FanClubInfo? = null,

    @field:SerializedName("has_anonymous_profile_picture")
    val hasAnonymousProfilePicture: Boolean? = null,

    @field:SerializedName("is_verified")
    val isVerified: Boolean? = null,

    @field:SerializedName("full_name")
    val fullName: String? = null,

    @field:SerializedName("is_unpublished")
    val isUnpublished: Boolean? = null,

    @field:SerializedName("feed_post_reshare_disabled")
    val feedPostReshareDisabled: Boolean? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("latest_reel_media")
    val latestReelMedia: Int? = null,

    @field:SerializedName("pk")
    val pk: Long? = null,

    @field:SerializedName("can_see_quiet_post_attribution")
    val canSeeQuietPostAttribution: Boolean? = null,

    @field:SerializedName("profile_pic_url")
    val profilePicUrl: String? = null,

    @field:SerializedName("username")
    val username: String? = null
)

data class Metrics(

    @field:SerializedName("like_count")
    val likeCount: Long? = null,

    @field:SerializedName("comment_count")
    val commentCount: Long? = null,

    @field:SerializedName("share_count")
    val shareCount: Int? = null,

    @field:SerializedName("user_media_count")
    val userMediaCount: Any? = null,

    @field:SerializedName("fb_like_count")
    val fbLikeCount: Any? = null,

    @field:SerializedName("save_count")
    val saveCount: Any? = null,

    @field:SerializedName("user_follower_count")
    val userFollowerCount: Any? = null,

    @field:SerializedName("fb_play_count")
    val fbPlayCount: Any? = null,

    @field:SerializedName("play_count")
    val playCount: Int? = null,

    @field:SerializedName("view_count")
    val viewCount: Any? = null
)

data class FirstFrame(

    @field:SerializedName("width")
    val width: Int? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("height")
    val height: Int? = null
)

data class ItemsItem(

    @field:SerializedName("url")
    val url: String? = null,
)

data class ImageVersions(

    @field:SerializedName("additional_items")
    val additionalItems: AdditionalItems? = null,

    @field:SerializedName("items", alternate = ["candidates"])
    val items: List<ItemsItem?>? = null
)

data class IgtvFirstFrame(

    @field:SerializedName("width")
    val width: Int? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("height")
    val height: Int? = null
)

data class MediaUser(

    @field:SerializedName("pk")
    val pk: Long? = null,

    @field:SerializedName("account_badges")
    val accountBadges: List<Any?>? = null,

    @field:SerializedName("is_private")
    val isPrivate: Boolean? = null,

    @field:SerializedName("account_type")
    val accountType: Int? = null,

    @field:SerializedName("profile_pic_id")
    val profilePicId: String? = null,

    @field:SerializedName("fbid_v2")
    val fbidV2: Long? = null,

    @field:SerializedName("is_verified")
    val isVerified: Boolean? = null,

    @field:SerializedName("full_name")
    val fullName: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("latest_reel_media")
    val latestReelMedia: Int? = null,

    @field:SerializedName("profile_pic_url")
    val profilePicUrl: String? = null,

    @field:SerializedName("username")
    val username: String? = null
)

data class AudioRankingInfo(

    @field:SerializedName("best_audio_cluster_id")
    val bestAudioClusterId: String? = null
)

data class MediaNotes(

    @field:SerializedName("items")
    val items: List<Any?>? = null
)

data class MusicInfo(

    @field:SerializedName("music_asset_info")
    val musicAssetInfo: MusicAssetInfo? = null,

    @field:SerializedName("music_consumption_info")
    val musicConsumptionInfo: MusicConsumptionInfo? = null
)

data class SquareCrop(

    @field:SerializedName("crop_bottom")
    val cropBottom: Any? = null,

    @field:SerializedName("crop_left")
    val cropLeft: Int? = null,

    @field:SerializedName("crop_right")
    val cropRight: Int? = null,

    @field:SerializedName("crop_top")
    val cropTop: Any? = null
)

data class VideoVersionsItem(

    @field:SerializedName("width")
    val width: Int? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("type")
    val type: Int? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("height")
    val height: Int? = null
)

data class MusicAssetInfo(

    @field:SerializedName("cover_artwork_thumbnail_uri")
    val coverArtworkThumbnailUri: String? = null,

    @field:SerializedName("display_artist")
    val displayArtist: String? = null,

    @field:SerializedName("sanitized_title")
    val sanitizedTitle: Any? = null,

    @field:SerializedName("fast_start_progressive_download_url")
    val fastStartProgressiveDownloadUrl: String? = null,

    @field:SerializedName("audio_asset_id")
    val audioAssetId: String? = null,

    @field:SerializedName("cover_artwork_uri")
    val coverArtworkUri: String? = null,

    @field:SerializedName("dark_message")
    val darkMessage: Any? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("artist_id")
    val artistId: Any? = null,

    @field:SerializedName("audio_id")
    val audioId: String? = null,

    @field:SerializedName("progressive_download_url")
    val progressiveDownloadUrl: String? = null,

    @field:SerializedName("is_eligible_for_vinyl_sticker")
    val isEligibleForVinylSticker: Boolean? = null,

    @field:SerializedName("allows_saving")
    val allowsSaving: Boolean? = null,

    @field:SerializedName("reactive_audio_download_url")
    val reactiveAudioDownloadUrl: Any? = null,

    @field:SerializedName("subtitle")
    val subtitle: String? = null,

    @field:SerializedName("is_eligible_for_audio_effects")
    val isEligibleForAudioEffects: Boolean? = null,

    @field:SerializedName("web_30s_preview_download_url")
    val web30sPreviewDownloadUrl: Any? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("has_lyrics")
    val hasLyrics: Boolean? = null,

    @field:SerializedName("duration_in_ms")
    val durationInMs: Int? = null,

    @field:SerializedName("highlight_start_times_in_ms")
    val highlightStartTimesInMs: List<Int?>? = null,

    @field:SerializedName("is_explicit")
    val isExplicit: Boolean? = null,

    @field:SerializedName("ig_username")
    val igUsername: Any? = null
)

data class Caption(

    @field:SerializedName("private_reply_status")
    val privateReplyStatus: Int? = null,

    @field:SerializedName("share_enabled")
    val shareEnabled: Boolean? = null,

    @field:SerializedName("hashtags")
    val hashtags: List<Any?>? = null,

    @field:SerializedName("created_at")
    val createdAt: Int? = null,

    @field:SerializedName("type")
    val type: Int? = null,

    @field:SerializedName("is_covered")
    val isCovered: Boolean? = null,

    @field:SerializedName("created_at_utc")
    val createdAtUtc: Int? = null,

    @field:SerializedName("mentions")
    val mentions: List<Any?>? = null,

    @field:SerializedName("id")
    val id: Long? = null,

    @field:SerializedName("pk")
    val pk: String? = null,

    @field:SerializedName("text")
    val text: String? = null,

    @field:SerializedName("did_report_as_spam")
    val didReportAsSpam: Boolean? = null,

    @field:SerializedName("is_ranked_comment")
    val isRankedComment: Boolean? = null
)

data class AchievementsInfo(

    @field:SerializedName("show_achievements")
    val showAchievements: Boolean? = null,

    @field:SerializedName("num_earned_achievements")
    val numEarnedAchievements: Any? = null
)

data class ContentAppreciationInfo(

    @field:SerializedName("entry_point_container")
    val entryPointContainer: Any? = null,

    @field:SerializedName("enabled")
    val enabled: Boolean? = null
)

data class ClipsMetadata(

    @field:SerializedName("asset_recommendation_info")
    val assetRecommendationInfo: Any? = null,

    @field:SerializedName("disable_use_in_clips_client_cache")
    val disableUseInClipsClientCache: Boolean? = null,

    @field:SerializedName("breaking_creator_info")
    val breakingCreatorInfo: Any? = null,

    @field:SerializedName("reels_on_the_rise_info")
    val reelsOnTheRiseInfo: Any? = null,

    @field:SerializedName("original_sound_info")
    val originalSoundInfo: Any? = null,

    @field:SerializedName("reusable_text_attribute_string")
    val reusableTextAttributeString: Any? = null,

    @field:SerializedName("branded_content_tag_info")
    val brandedContentTagInfo: BrandedContentTagInfo? = null,

    @field:SerializedName("merchandising_pill_info")
    val merchandisingPillInfo: Any? = null,

    @field:SerializedName("template_info")
    val templateInfo: Any? = null,

    @field:SerializedName("reusable_text_info")
    val reusableTextInfo: Any? = null,

    @field:SerializedName("breaking_content_info")
    val breakingContentInfo: Any? = null,

    @field:SerializedName("achievements_info")
    val achievementsInfo: AchievementsInfo? = null,

    @field:SerializedName("additional_audio_info")
    val additionalAudioInfo: AdditionalAudioInfo? = null,

    @field:SerializedName("cutout_sticker_info")
    val cutoutStickerInfo: List<Any?>? = null,

    @field:SerializedName("audio_type")
    val audioType: String? = null,

    @field:SerializedName("shopping_info")
    val shoppingInfo: Any? = null,

    @field:SerializedName("is_shared_to_fb")
    val isSharedToFb: Boolean? = null,

    @field:SerializedName("challenge_info")
    val challengeInfo: Any? = null,

    @field:SerializedName("viewer_interaction_settings")
    val viewerInteractionSettings: Any? = null,

    @field:SerializedName("audio_canonical_id")
    val audioCanonicalId: String? = null,

    @field:SerializedName("is_fan_club_promo_video")
    val isFanClubPromoVideo: Boolean? = null,

    @field:SerializedName("content_appreciation_info")
    val contentAppreciationInfo: ContentAppreciationInfo? = null,

    @field:SerializedName("is_public_chat_welcome_video")
    val isPublicChatWelcomeVideo: Boolean? = null,

    @field:SerializedName("clips_creation_entry_point")
    val clipsCreationEntryPoint: String? = null,

    @field:SerializedName("external_media_info")
    val externalMediaInfo: Any? = null,

    @field:SerializedName("featured_label")
    val featuredLabel: Any? = null,

    @field:SerializedName("professional_clips_upsell_type")
    val professionalClipsUpsellType: Int? = null,

    @field:SerializedName("audio_ranking_info")
    val audioRankingInfo: AudioRankingInfo? = null,

    @field:SerializedName("nux_info")
    val nuxInfo: Any? = null,

    @field:SerializedName("contextual_highlight_info")
    val contextualHighlightInfo: Any? = null,

    @field:SerializedName("music_info")
    val musicInfo: MusicInfo? = null,

    @field:SerializedName("show_achievements")
    val showAchievements: Boolean? = null,

    @field:SerializedName("show_tips")
    val showTips: Any? = null,

    @field:SerializedName("mashup_info")
    val mashupInfo: MashupInfo? = null,

    @field:SerializedName("originality_info")
    val originalityInfo: Any? = null
)


// carousel

data class CarouselMediaItem(

    @field:SerializedName("carousel_parent_id")
    val carouselParentId: String? = null,

    @field:SerializedName("original_width")
    val originalWidth: Int? = null,

    @field:SerializedName("featured_products")
    val featuredProducts: List<Any?>? = null,

    @field:SerializedName("shop_routing_user_id")
    val shopRoutingUserId: Any? = null,

    @field:SerializedName("commerciality_status")
    val commercialityStatus: String? = null,

    @field:SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,

    @field:SerializedName("product_suggestions")
    val productSuggestions: List<Any?>? = null,

    @field:SerializedName("sharing_friction_info")
    val sharingFrictionInfo: SharingFrictionInfo? = null,

    @field:SerializedName("is_video")
    val isVideo: Boolean? = null,

    @field:SerializedName("product_type")
    val productType: String? = null,

    @field:SerializedName("media_type")
    val mediaType: Int? = null,

    @field:SerializedName("taken_at")
    val takenAt: Int? = null,

    @field:SerializedName("explore_pivot_grid")
    val explorePivotGrid: Boolean? = null,

    @field:SerializedName("original_height")
    val originalHeight: Int? = null,

    @field:SerializedName("fb_user_tags")
    val fbUserTags: FbUserTags? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("media_name")
    val mediaName: String? = null,

    @field:SerializedName("pk")
    val pk: Long? = null,

    @field:SerializedName("image_versions", alternate = ["image_versions2"])
    val imageVersions: ImageVersions? = null,

    @field:SerializedName("video_versions", alternate = ["video_versions2"])
    val videoVersions: List<VideoVersionsItem?>? = null,
)