package com.instagram.video.downloader.data.remote.api.dto

import com.google.gson.annotations.SerializedName

data class PlanDto(

    @field:SerializedName("duration")
    val duration: String,

    @field:SerializedName("unique_id")
    val uniqueId: String,

    @field:SerializedName("tag_name")
    val tagName: String,

    @field:SerializedName("google_play_id")
    val googlePlayId: String,

    @field:SerializedName("base_plan_google_id")
    val basePlanGoogleId: String,

    @field:SerializedName("price")
    val price: Float,

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("discount")
    val discount: Float,

    @field:SerializedName("is_trial")
    val isTrial: Boolean,
)

fun PlanDto.toPlan(): Plan {
    return Plan(
        uniqueId,
        duration,
        tagName,
        googlePlayId,
        basePlanGoogleId,
        price,
        name,
        discount,
        isTrial,
    )
}
