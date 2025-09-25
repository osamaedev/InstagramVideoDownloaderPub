package com.instagram.video.downloader.data.remote.instaApi.dto.extraction

import com.google.gson.annotations.SerializedName
import com.instagram.video.downloader.data.remote.api.dto.CoverMedia
import com.instagram.video.downloader.data.remote.api.dto.CroppedImageVersion
import com.instagram.video.downloader.data.remote.api.dto.HighlightData
import com.instagram.video.downloader.data.remote.api.dto.HighlightMedia
import com.instagram.video.downloader.data.remote.api.dto.ImageVersions
import com.instagram.video.downloader.data.remote.api.dto.ItemsItem
import com.instagram.video.downloader.data.remote.api.dto.MediaUser
import com.instagram.video.downloader.data.remote.api.dto.VideoVersionsItem

data class HighlightExtractionResponse(

    @field:SerializedName("extensions")
    val extensions: Extensions? = null,

    @field:SerializedName("data")
    val data: Data? = null,

    @field:SerializedName("status")
    val status: String? = null
)

data class Owner(

    @field:SerializedName("__typename")
    val typename: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("requested_by_viewer")
    val requestedByViewer: Boolean? = null,

    @field:SerializedName("profile_pic_url")
    val profilePicUrl: String? = null,

    @field:SerializedName("followed_by_viewer")
    val followedByViewer: Boolean? = null,

    @field:SerializedName("username")
    val username: String? = null
)

data class Data(

    @field:SerializedName("reels_media")
    val reelsMedia: List<ReelsMediaItem?>? = null
)

data class ReelsMediaItem(

    @field:SerializedName("owner")
    val owner: Owner? = null,

    @field:SerializedName("can_reply")
    val canReply: Boolean? = null,

    @field:SerializedName("__typename")
    val typename: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("latest_reel_media")
    val latestReelMedia: Any? = null,

    @field:SerializedName("items")
    val items: List<HighlightItem?>? = null
)

data class Dimensions(

    @field:SerializedName("width")
    val width: Int? = null,

    @field:SerializedName("height")
    val height: Int? = null
)

fun HighlightItem.toHighlightData(reelsMediaItem: ReelsMediaItem): HighlightData {
    return HighlightData(
        id = reelsMediaItem.id!!,
        coverMedia = CoverMedia(null, CroppedImageVersion(null, this.owner?.profilePicUrl!!, null)),
        reelType = "highlight_reel",                // by default
        createdAt = this.takenAtTimestamp!!,
        user = MediaUser(
            id = this.owner.id!!,
            fullName = this.owner.username!!,
            username = this.owner.username,
            profilePicUrl = this.owner.profilePicUrl,
            profilePicId = "",
        )
    )
}

fun HighlightItem.toHighlightMedia() : HighlightMedia {
    return HighlightMedia(
        mediaType = if (this.isVideo == true) 2 else 1,
        id = this.id!!,
        isVideo = this.isVideo,
        takenAt = this.takenAtTimestamp!!,
        thumbnailUrl = this.displayUrl!!,
        videoVersions = arrayListOf<VideoVersionsItem>(VideoVersionsItem(null, null, null, this.displayResources?.first()?.src!!, null)),
        imageVersions = ImageVersions(null, arrayListOf(ItemsItem(url = this.displayResources.first()?.src!!))),
    )
}

data class HighlightItem(

    @field:SerializedName("owner")
    val owner: Owner? = null,

    @field:SerializedName("display_url")
    val displayUrl: String? = null,

    @field:SerializedName("expiring_at_timestamp")
    val expiringAtTimestamp: Int? = null,

    @field:SerializedName("tracking_token")
    val trackingToken: String? = null,

    @field:SerializedName("taken_at_timestamp")
    val takenAtTimestamp: Long? = null,

    @field:SerializedName("__typename")
    val typename: String? = null,

    @field:SerializedName("story_view_count")
    val storyViewCount: Any? = null,

    @field:SerializedName("story_cta_url")
    val storyCtaUrl: Any? = null,

    @field:SerializedName("display_resources", ["video_resources"])
    val displayResources: List<DisplayResourcesItem?>? = null,

    @field:SerializedName("tappable_objects")
    val tappableObjects: List<Any?>? = null,

    @field:SerializedName("is_video")
    val isVideo: Boolean? = null,

    @field:SerializedName("should_log_client_event")
    val shouldLogClientEvent: Boolean? = null,

    @field:SerializedName("media_preview")
    val mediaPreview: String? = null,

    @field:SerializedName("gating_info")
    val gatingInfo: Any? = null,

    @field:SerializedName("story_app_attribution")
    val storyAppAttribution: Any? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("dimensions")
    val dimensions: Dimensions? = null
)

data class Extensions(

    @field:SerializedName("is_final")
    val isFinal: Boolean? = null
)

data class DisplayResourcesItem(

    @field:SerializedName("src")
    val src: String? = null,
)
