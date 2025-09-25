package com.instagram.video.downloader.data.remote.instaApi.dto.collections

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable


data class User(

    @field:SerializedName("is_private")
	val isPrivate: Boolean? = null,

    @field:SerializedName("account_type")
	val accountType: Int? = null,

    @field:SerializedName("pk_id")
	val pkId: String? = null,

//    @field:SerializedName("is_favorite")
//	val isFavorite: Boolean? = null,

//    @field:SerializedName("show_account_transparency_details")
//	val showAccountTransparencyDetails: Boolean? = null,

    @field:SerializedName("third_party_downloads_enabled")
	val thirdPartyDownloadsEnabled: Int? = null,

    @field:SerializedName("fbid_v2")
	val fbidV2: String? = null,

//    @field:SerializedName("transparency_product_enabled")
//	val transparencyProductEnabled: Boolean? = null,


    @field:SerializedName("hd_profile_pic_url_info")
	val hdProfilePicUrlInfo: HdProfilePicUrlInfo? = null,

//    @field:SerializedName("friendship_status")
//	val friendshipStatus: FriendshipStatus? = null,

    @field:SerializedName("id")
	val id: String? = null,

    @field:SerializedName("profile_pic_url")
	val profilePicUrl: String? = null,

    @field:SerializedName("strong_id__")
	val strongId: String? = null,

    @field:SerializedName("profile_pic_id")
	val profilePicId: String? = null,

//    @field:SerializedName("has_anonymous_profile_picture")
//	val hasAnonymousProfilePicture: Boolean? = null,

    @field:SerializedName("is_verified")
	val isVerified: Boolean? = null,

    @field:SerializedName("hd_profile_pic_versions")
	val hdProfilePicVersions: List<HdProfilePicVersionsItem?>? = null,

    @field:SerializedName("full_name")
	val fullName: String? = null,

//    @field:SerializedName("eligible_for_text_app_activation_badge")
//	val eligibleForTextAppActivationBadge: Boolean? = null,

//    @field:SerializedName("is_unpublished")
//	val isUnpublished: Boolean? = null,

//    @field:SerializedName("feed_post_reshare_disabled")
//	val feedPostReshareDisabled: Boolean? = null,

    @field:SerializedName("pk")
	val pk: String? = null,

//    @field:SerializedName("latest_reel_media")
//	val latestReelMedia: Int? = null,

//    @field:SerializedName("can_see_quiet_post_attribution")
//	val canSeeQuietPostAttribution: Boolean? = null,

    @field:SerializedName("username")
	val username: String? = null
) : Serializable