package com.instagram.video.downloader.data.remote.instaApi.dto.userInfo

import com.google.gson.annotations.SerializedName

data class FanClubInfo(

	@field:SerializedName("fan_club_id")
	val fanClubId: Any? = null,

	@field:SerializedName("subscriber_count")
	val subscriberCount: Any? = null,

	@field:SerializedName("is_fan_club_referral_eligible")
	val isFanClubReferralEligible: Any? = null,

	@field:SerializedName("fan_consideration_page_revamp_eligiblity")
	val fanConsiderationPageRevampEligiblity: Any? = null,

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