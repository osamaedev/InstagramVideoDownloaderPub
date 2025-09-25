package com.instagram.video.downloader.data.remote.instaApi.dto.collections

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable


data class ImageVersions2(
    @field:SerializedName("candidates")
	val candidates: List<CandidatesItem?>? = null,
) : Serializable