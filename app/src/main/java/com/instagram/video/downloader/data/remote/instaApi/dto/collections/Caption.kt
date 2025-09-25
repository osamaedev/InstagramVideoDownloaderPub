package com.instagram.video.downloader.data.remote.instaApi.dto.collections

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable


data class Caption(

//	@field:SerializedName("private_reply_status")
//	val privateReplyStatus: Int? = null,

//	@field:SerializedName("share_enabled")
//	val shareEnabled: Boolean? = null,

//	@field:SerializedName("created_at")
//	val createdAt: Int? = null,

//	@field:SerializedName("type")
//	val type: Int? = null,

//	@field:SerializedName("is_covered")
//	val isCovered: Boolean? = null,

//	@field:SerializedName("created_at_utc")
//	val createdAtUtc: Int? = null,

//	@field:SerializedName("content_type")
//	val contentType: String? = null,

	@field:SerializedName("user_id")
	val userId: String? = null,

//	@field:SerializedName("bit_flags")
//	val bitFlags: Int? = null,

	@field:SerializedName("media_id")
	val mediaId: String? = null,

	@field:SerializedName("pk")
	val pk: String? = null,

	@field:SerializedName("text")
	val text: String? = null,

//	@field:SerializedName("did_report_as_spam")
//	val didReportAsSpam: Boolean? = null,

//	@field:SerializedName("user")
//	val user: User? = null,

//	@field:SerializedName("is_ranked_comment")
//	val isRankedComment: Boolean? = null,

//	@field:SerializedName("status")
//	val status: String? = null,

//	@field:SerializedName("strong_id__")
//	val strongId: String? = null,

//	@field:SerializedName("has_translation")
//	val hasTranslation: Boolean? = null
) : Serializable