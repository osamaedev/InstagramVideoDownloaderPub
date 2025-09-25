package com.instagram.video.downloader.data.remote.instaApi.dto.userInfo

import com.google.gson.annotations.SerializedName

data class User(

	@field:SerializedName("enable_add_school_in_edit_profile")
	val enableAddSchoolInEditProfile: Boolean? = null,

	@field:SerializedName("account_type")
	val accountType: Int? = null,

	@field:SerializedName("show_account_transparency_details")
	val showAccountTransparencyDetails: Boolean? = null,

	@field:SerializedName("third_party_downloads_enabled")
	val thirdPartyDownloadsEnabled: Int? = null,

	@field:SerializedName("account_category")
	val accountCategory: String? = null,

	@field:SerializedName("show_fb_page_link_on_profile")
	val showFbPageLinkOnProfile: Boolean? = null,

	@field:SerializedName("fbid_v2")
	val fbidV2: String? = null,

	@field:SerializedName("fan_club_info")
	val fanClubInfo: FanClubInfo? = null,

	@field:SerializedName("displayed_action_button_type")
	val displayedActionButtonType: Any? = null,

	@field:SerializedName("hd_profile_pic_url_info")
	val hdProfilePicUrlInfo: HdProfilePicUrlInfo? = null,

	@field:SerializedName("adjusted_banners_order")
	val adjustedBannersOrder: List<Any?>? = null,

	@field:SerializedName("is_memorialized")
	val isMemorialized: Boolean? = null,

	@field:SerializedName("show_schools_badge")
	val showSchoolsBadge: Any? = null,

	@field:SerializedName("is_category_tappable")
	val isCategoryTappable: Boolean? = null,

	@field:SerializedName("num_of_admined_pages")
	val numOfAdminedPages: Any? = null,

	@field:SerializedName("can_add_fb_group_link_on_profile")
	val canAddFbGroupLinkOnProfile: Boolean? = null,

	@field:SerializedName("total_igtv_videos")
	val totalIgtvVideos: Int? = null,

	@field:SerializedName("is_secondary_account_creation")
	val isSecondaryAccountCreation: Boolean? = null,

	@field:SerializedName("latest_besties_reel_media")
	val latestBestiesReelMedia: Int? = null,

	@field:SerializedName("is_regulated_news_in_viewer_location")
	val isRegulatedNewsInViewerLocation: Boolean? = null,

	@field:SerializedName("can_use_paid_partnership_messaging_as_creator")
	val canUsePaidPartnershipMessagingAsCreator: Boolean? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("is_bestie")
	val isBestie: Boolean? = null,

	@field:SerializedName("is_eligible_for_meta_verified_links_in_reels")
	val isEligibleForMetaVerifiedLinksInReels: Boolean? = null,

	@field:SerializedName("is_eligible_for_meta_verified_enhanced_link_sheet_consumption")
	val isEligibleForMetaVerifiedEnhancedLinkSheetConsumption: Boolean? = null,

	@field:SerializedName("has_chains")
	val hasChains: Boolean? = null,

	@field:SerializedName("open_external_url_with_in_app_browser")
	val openExternalUrlWithInAppBrowser: Boolean? = null,

	@field:SerializedName("is_eligible_for_post_boost_mv_upsell")
	val isEligibleForPostBoostMvUpsell: Boolean? = null,

	@field:SerializedName("has_collab_collections")
	val hasCollabCollections: Boolean? = null,

	@field:SerializedName("can_use_branded_content_discovery_as_creator")
	val canUseBrandedContentDiscoveryAsCreator: Boolean? = null,

	@field:SerializedName("has_anonymous_profile_picture")
	val hasAnonymousProfilePicture: Boolean? = null,

	@field:SerializedName("has_fan_club_subscriptions")
	val hasFanClubSubscriptions: Boolean? = null,

	@field:SerializedName("show_post_insights_entry_point")
	val showPostInsightsEntryPoint: Boolean? = null,

	@field:SerializedName("has_videos")
	val hasVideos: Boolean? = null,

	@field:SerializedName("full_name")
	val fullName: String? = null,

	@field:SerializedName("professional_conversion_suggested_account_type")
	val professionalConversionSuggestedAccountType: Int? = null,

	@field:SerializedName("has_music_on_profile")
	val hasMusicOnProfile: Boolean? = null,

	@field:SerializedName("latest_reel_media")
	val latestReelMedia: Int? = null,

	@field:SerializedName("account_badges")
	val accountBadges: List<Any?>? = null,

	@field:SerializedName("auto_expand_chaining")
	val autoExpandChaining: Any? = null,

	@field:SerializedName("is_eligible_for_meta_verified_related_accounts")
	val isEligibleForMetaVerifiedRelatedAccounts: Boolean? = null,

	@field:SerializedName("can_use_affiliate_partnership_messaging_as_brand")
	val canUseAffiliatePartnershipMessagingAsBrand: Boolean? = null,

	@field:SerializedName("show_fb_link_on_profile")
	val showFbLinkOnProfile: Boolean? = null,

	@field:SerializedName("mini_shop_seller_onboarding_status")
	val miniShopSellerOnboardingStatus: Any? = null,

	@field:SerializedName("is_eligible_for_request_message")
	val isEligibleForRequestMessage: Boolean? = null,

	@field:SerializedName("is_eligible_for_meta_verified_multiple_addresses_consumption")
	val isEligibleForMetaVerifiedMultipleAddressesConsumption: Boolean? = null,

	@field:SerializedName("chaining_upsell_cards")
	val chainingUpsellCards: List<Any?>? = null,

	@field:SerializedName("is_open_to_collab")
	val isOpenToCollab: Boolean? = null,

	@field:SerializedName("is_remix_setting_enabled_for_posts")
	val isRemixSettingEnabledForPosts: Boolean? = null,

	@field:SerializedName("bio_links")
	val bioLinks: List<Any?>? = null,

	@field:SerializedName("has_ever_selected_topics")
	val hasEverSelectedTopics: Boolean? = null,

	@field:SerializedName("is_opal_enabled")
	val isOpalEnabled: Boolean? = null,

	@field:SerializedName("total_clips_count")
	val totalClipsCount: Int? = null,

	@field:SerializedName("biography")
	val biography: String? = null,

	@field:SerializedName("additional_business_addresses")
	val additionalBusinessAddresses: List<Any?>? = null,

	@field:SerializedName("has_gen_ai_personas_for_profile_banner")
	val hasGenAiPersonasForProfileBanner: Boolean? = null,

	@field:SerializedName("is_whatsapp_linked")
	val isWhatsappLinked: Boolean? = null,

	@field:SerializedName("is_profile_broadcast_sharing_enabled")
	val isProfileBroadcastSharingEnabled: Boolean? = null,

	@field:SerializedName("is_verified")
	val isVerified: Boolean? = null,

	@field:SerializedName("recs_from_friends")
	val recsFromFriends: RecsFromFriends? = null,

	@field:SerializedName("profile_context_links_with_user_ids")
	val profileContextLinksWithUserIds: List<Any?>? = null,

	@field:SerializedName("profile_context")
	val profileContext: String? = null,

	@field:SerializedName("total_ar_effects")
	val totalArEffects: Int? = null,

	@field:SerializedName("profile_context_facepile_users")
	val profileContextFacepileUsers: List<Any?>? = null,

	@field:SerializedName("highlight_reshare_disabled")
	val highlightReshareDisabled: Boolean? = null,

	@field:SerializedName("has_private_collections")
	val hasPrivateCollections: Boolean? = null,

	@field:SerializedName("is_eligible_for_meta_verified_enhanced_link_sheet")
	val isEligibleForMetaVerifiedEnhancedLinkSheet: Boolean? = null,

	@field:SerializedName("can_hide_category")
	val canHideCategory: Boolean? = null,

	@field:SerializedName("is_remix_setting_enabled_for_reels")
	val isRemixSettingEnabledForReels: Boolean? = null,

	@field:SerializedName("has_ig_profile")
	val hasIgProfile: Boolean? = null,

	@field:SerializedName("existing_user_age_collection_enabled")
	val existingUserAgeCollectionEnabled: Boolean? = null,

	@field:SerializedName("is_stories_teaser_muted")
	val isStoriesTeaserMuted: Boolean? = null,

	@field:SerializedName("pk_id")
	val pkId: String? = null,

	@field:SerializedName("media_count")
	val mediaCount: Int? = null,

	@field:SerializedName("is_supervision_features_enabled")
	val isSupervisionFeaturesEnabled: Boolean? = null,

	@field:SerializedName("text_app_last_visited_time")
	val textAppLastVisitedTime: Any? = null,

	@field:SerializedName("transparency_product_enabled")
	val transparencyProductEnabled: Boolean? = null,

	@field:SerializedName("mutual_followers_count")
	val mutualFollowersCount: Int? = null,

	@field:SerializedName("is_direct_roll_call_enabled")
	val isDirectRollCallEnabled: Boolean? = null,

	@field:SerializedName("nametag")
	val nametag: Nametag? = null,

	@field:SerializedName("is_profile_picture_expansion_enabled")
	val isProfilePictureExpansionEnabled: Boolean? = null,

	@field:SerializedName("is_eligible_for_meta_verified_multiple_addresses_creation")
	val isEligibleForMetaVerifiedMultipleAddressesCreation: Boolean? = null,

	@field:SerializedName("external_url")
	val externalUrl: String? = null,

	@field:SerializedName("current_catalog_id")
	val currentCatalogId: Any? = null,

	@field:SerializedName("smb_support_partner")
	val smbSupportPartner: Any? = null,

	@field:SerializedName("pinned_channels_info")
	val pinnedChannelsInfo: PinnedChannelsInfo? = null,

	@field:SerializedName("page_name")
	val pageName: Any? = null,

	@field:SerializedName("biography_with_entities")
	val biographyWithEntities: BiographyWithEntities? = null,

	@field:SerializedName("shopping_post_onboard_nux_type")
	val shoppingPostOnboardNuxType: Any? = null,

	@field:SerializedName("meta_verified_related_accounts_count")
	val metaVerifiedRelatedAccountsCount: Int? = null,

	@field:SerializedName("profile_pic_url")
	val profilePicUrl: String? = null,

	@field:SerializedName("primary_profile_link_type")
	val primaryProfileLinkType: Int? = null,

	@field:SerializedName("follow_friction_type")
	val followFrictionType: Int? = null,

	@field:SerializedName("relevant_news_regulation_locations")
	val relevantNewsRegulationLocations: List<Any?>? = null,

	@field:SerializedName("is_new_to_instagram")
	val isNewToInstagram: Boolean? = null,

	@field:SerializedName("hd_profile_pic_versions")
	val hdProfilePicVersions: List<HdProfilePicVersionsItem?>? = null,

	@field:SerializedName("is_eligible_for_meta_verified_label")
	val isEligibleForMetaVerifiedLabel: Boolean? = null,

	@field:SerializedName("has_exclusive_feed_content")
	val hasExclusiveFeedContent: Boolean? = null,

	@field:SerializedName("is_regulated_c18")
	val isRegulatedC18: Boolean? = null,

	@field:SerializedName("can_use_affiliate_partnership_messaging_as_creator")
	val canUseAffiliatePartnershipMessagingAsCreator: Boolean? = null,

	@field:SerializedName("following_count")
	val followingCount: Int? = null,

	@field:SerializedName("remove_message_entrypoint")
	val removeMessageEntrypoint: Boolean? = null,

	@field:SerializedName("pronouns")
	val pronouns: List<Any?>? = null,

	@field:SerializedName("feed_post_reshare_disabled")
	val feedPostReshareDisabled: Boolean? = null,

	@field:SerializedName("is_private")
	val isPrivate: Boolean? = null,

	@field:SerializedName("is_favorite")
	val isFavorite: Boolean? = null,

	@field:SerializedName("interop_messaging_user_fbid")
	val interopMessagingUserFbid: String? = null,

	@field:SerializedName("is_in_canada")
	val isInCanada: Boolean? = null,

	@field:SerializedName("has_highlight_reels")
	val hasHighlightReels: Boolean? = null,

	@field:SerializedName("is_meta_verified_related_accounts_display_enabled")
	val isMetaVerifiedRelatedAccountsDisplayEnabled: Boolean? = null,

	@field:SerializedName("follower_count")
	val followerCount: Int? = null,

	@field:SerializedName("birthday_today_visibility_for_viewer")
	val birthdayTodayVisibilityForViewer: String? = null,

	@field:SerializedName("live_subscription_status")
	val liveSubscriptionStatus: String? = null,

	@field:SerializedName("page_id")
	val pageId: Any? = null,

	@field:SerializedName("include_direct_blacklist_status")
	val includeDirectBlacklistStatus: Boolean? = null,

	@field:SerializedName("has_chaining")
	val hasChaining: Boolean? = null,

	@field:SerializedName("smb_support_delivery_partner")
	val smbSupportDeliveryPartner: Any? = null,

	@field:SerializedName("is_oregon_custom_gender_consented")
	val isOregonCustomGenderConsented: Boolean? = null,

	@field:SerializedName("is_creator_agent_enabled")
	val isCreatorAgentEnabled: Boolean? = null,

	@field:SerializedName("spam_follower_setting_enabled")
	val spamFollowerSettingEnabled: Any? = null,

	@field:SerializedName("strong_id__")
	val strongId: String? = null,

	@field:SerializedName("ads_page_name")
	val adsPageName: Any? = null,

	@field:SerializedName("is_business")
	val isBusiness: Boolean? = null,

	@field:SerializedName("profile_type")
	val profileType: Int? = null,

	@field:SerializedName("highlights_tray_type")
	val highlightsTrayType: String? = null,

	@field:SerializedName("smb_delivery_partner")
	val smbDeliveryPartner: Any? = null,

	@field:SerializedName("displayed_action_button_partner")
	val displayedActionButtonPartner: Any? = null,

	@field:SerializedName("ads_incentive_expiration_date")
	val adsIncentiveExpirationDate: Any? = null,

	@field:SerializedName("has_public_tab_threads")
	val hasPublicTabThreads: Boolean? = null,

	@field:SerializedName("is_recon_ad_cta_on_profile_eligible_with_viewer")
	val isReconAdCtaOnProfileEligibleWithViewer: Boolean? = null,

	@field:SerializedName("is_call_to_action_enabled")
	val isCallToActionEnabled: Any? = null,

	@field:SerializedName("is_potential_business")
	val isPotentialBusiness: Boolean? = null,

	@field:SerializedName("is_interest_account")
	val isInterestAccount: Boolean? = null,

	@field:SerializedName("eligible_for_text_app_activation_badge")
	val eligibleForTextAppActivationBadge: Boolean? = null,

	@field:SerializedName("pk")
	val pk: String? = null,

	@field:SerializedName("can_use_branded_content_discovery_as_brand")
	val canUseBrandedContentDiscoveryAsBrand: Boolean? = null,

	@field:SerializedName("has_guides")
	val hasGuides: Boolean? = null,

	@field:SerializedName("is_parenting_account")
	val isParentingAccount: Boolean? = null,

	@field:SerializedName("category")
	val category: Any? = null,

	@field:SerializedName("ads_page_id")
	val adsPageId: Any? = null,

	@field:SerializedName("creator_shopping_info")
	val creatorShoppingInfo: CreatorShoppingInfo? = null,

	@field:SerializedName("username")
	val username: String? = null
)