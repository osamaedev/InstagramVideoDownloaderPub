package com.instagram.video.downloader.data.remote.api.dto

import com.google.gson.annotations.SerializedName

data class AudioResponse(

    @field:SerializedName("previous_trend_rank")
    val previousTrendRank: Any? = null,

    @field:SerializedName("artist")
    val artist: Artist? = null,

    @field:SerializedName("audio_canonical_id")
    val audioCanonicalId: String? = null,

    @field:SerializedName("duration_in_ms_overlap")
    val durationInMsOverlap: Any? = null,

    @field:SerializedName("original_media_id")
    val originalMediaId: Long? = null,

    @field:SerializedName("is_music_page_restricted")
    val isMusicPageRestricted: Boolean? = null,

    @field:SerializedName("is_trending_in_clips")
    val isTrendingInClips: Boolean? = null,

    @field:SerializedName("title")
    val title: String? = null,

    @field:SerializedName("total_posts")
    val totalPosts: Int? = null,

    @field:SerializedName("attributed_custom_audio_asset_id")
    val attributedCustomAudioAssetId: Any? = null,

    @field:SerializedName("download_url")
    val downloadUrl: String? = null,

    @field:SerializedName("time_created")
    val timeCreated: Long? = null,

    @field:SerializedName("is_original_audio")
    val isOriginalAudio: Boolean? = null,

    @field:SerializedName("is_reuse_disabled")
    val isReuseDisabled: Boolean? = null,

    @field:SerializedName("duration_in_ms")
    val durationInMs: Int? = null,

    @field:SerializedName("total_reels")
    val totalReels: Int? = null,

    @field:SerializedName("audio_id")
    val audioId: String,
)

data class Artist(

    @field:SerializedName("is_private")
    val isPrivate: Boolean? = null,

    @field:SerializedName("full_name")
    val fullName: String? = null,

    @field:SerializedName("profile_pic_id")
    val profilePicId: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("is_verified")
    val isVerified: Boolean? = null,

    @field:SerializedName("profile_pic_url")
    val profilePicUrl: String? = null,

    @field:SerializedName("username")
    val username: String? = null
)
