package com.instagram.video.downloader.data.remote.api.dto

import com.google.gson.annotations.SerializedName

data class HighlightData(

    @field:SerializedName("cover_media")
    val coverMedia: CoverMedia? = null,

    @field:SerializedName("highlight_reel_type")
    val highlightReelType: String? = null,

    @field:SerializedName("created_at")
    val createdAt: Long? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("latest_reel_media")
    val latestReelMedia: Int? = null,

    @field:SerializedName("reel_type")
    val reelType: String? = null,

    @field:SerializedName("user")
    val user: MediaUser? = null
)

data class HighlightMedia(

    @field:SerializedName("media_type")
    val mediaType: Int? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("media_name")
    val mediaName: String? = null,

    @field:SerializedName("is_video")
    val isVideo: Boolean? = null,

    @field:SerializedName("taken_at")
    val takenAt: Long? = null,

    @field:SerializedName("video_duration")
    val videoDuration: Any? = null,

    @field:SerializedName("code")
    val code: String? = null,

    @field:SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,

    @field:SerializedName("video_versions")
    val videoVersions: List<VideoVersionsItem?>? = null,

    @field:SerializedName("image_versions")
    val imageVersions: ImageVersions? = null,

    @field:SerializedName("owner")
    val owner: Owner? = null,

    @field:SerializedName("sharing_friction_info")
    val sharingFrictionInfo: SharingFrictionInfo? = null,

    @field:SerializedName("can_reply")
    val canReply: Boolean? = null,

    @field:SerializedName("product_type")
    val productType: String? = null,

    @field:SerializedName("fbid")
    val fbid: String? = null,

    @field:SerializedName("pk")
    val pk: Long? = null,
)

data class GenAiDetectionMethod(

    @field:SerializedName("detection_method")
    val detectionMethod: String? = null
)

data class CroppedImageVersion(

    @field:SerializedName("width")
    val width: Int? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("height")
    val height: Int? = null
)

data class AddedToItem(

    @field:SerializedName("reel_id")
    val reelId: String? = null,

    @field:SerializedName("title")
    val title: String? = null
)

data class FundraiserTag(

    @field:SerializedName("has_standalone_fundraiser")
    val hasStandaloneFundraiser: Boolean? = null
)

data class SharingFrictionInfo(

    @field:SerializedName("bloks_app_url")
    val bloksAppUrl: Any? = null,

    @field:SerializedName("should_have_sharing_friction")
    val shouldHaveSharingFriction: Boolean? = null,

    @field:SerializedName("sharing_friction_payload")
    val sharingFrictionPayload: Any? = null
)

data class CoverMedia(

    @field:SerializedName("full_image_version")
    val fullImageVersion: Any? = null,

    @field:SerializedName("cropped_image_version")
    val croppedImageVersion: CroppedImageVersion? = null,

    @field:SerializedName("upload_id")
    val uploadId: Any? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("crop_rect")
    val cropRect: List<Any?>? = null
)

data class HighlightsInfo(

    @field:SerializedName("added_to")
    val addedTo: List<AddedToItem?>? = null
)

data class HighlightResponse(

    @field:SerializedName("additional_data")
    val additionalData: HighlightData? = null,

    @field:SerializedName("count")
    val count: Int? = null,

    @field:SerializedName("items")
    val medias: List<HighlightMedia?>? = null
)
