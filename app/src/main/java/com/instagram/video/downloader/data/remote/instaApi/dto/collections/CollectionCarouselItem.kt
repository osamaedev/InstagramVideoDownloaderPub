package com.instagram.video.downloader.data.remote.instaApi.dto.collections

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class CollectionCarouselItem(

//	@field:SerializedName("preview")
//	val preview: String? = null,

	@field:SerializedName("video_duration")
	val videoDuration: Double? = null,


//	@field:SerializedName("has_audio")
//	val hasAudio: Boolean? = null,

//	@field:SerializedName("video_dash_manifest")
//	val videoDashManifest: String? = null,

	@field:SerializedName("media_type")
	val mediaType: Int? = null,

//	@field:SerializedName("original_height")
//	val originalHeight: Int? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("video_versions")
	val videoVersions: List<VideoVersionsItem2?>? = null,

//	@field:SerializedName("strong_id__")
//	val strongId: String? = null,
//
//	@field:SerializedName("carousel_parent_id")
//	val carouselParentId: String? = null,
//
//	@field:SerializedName("original_width")
//	val originalWidth: Int? = null,

//	@field:SerializedName("is_dash_eligible")
//	val isDashEligible: Int? = null,
//
//	@field:SerializedName("number_of_qualities")
//	val numberOfQualities: Int? = null,

//	@field:SerializedName("commerciality_status")
//	val commercialityStatus: String? = null,

	@field:SerializedName("product_type")
	val productType: String? = null,

//	@field:SerializedName("taken_at")
//	val takenAt: Int? = null,

//	@field:SerializedName("explore_pivot_grid")
//	val explorePivotGrid: Boolean? = null,

	@field:SerializedName("image_versions2")
	val imageVersions2: ImageVersions22? = null,

	@field:SerializedName("pk")
	val pk: String? = null
) : Serializable


data class ImageVersions22(

	@field:SerializedName("candidates")
	val candidates: List<CandidatesItem2?>? = null
) : Serializable


data class VideoVersionsItem2(

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
) : Serializable


data class CandidatesItem2(

	@field:SerializedName("scans_profile")
	val scansProfile: String? = null,

	@field:SerializedName("width")
	val width: Int? = null,

	@field:SerializedName("url")
	val url: String? = null,

	@field:SerializedName("height")
	val height: Int? = null
) : Serializable
