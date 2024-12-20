package com.example.mymove.api

import com.google.gson.annotations.SerializedName

data class VideoDetail(
    @SerializedName("vod_id")
    val id: Int,
    @SerializedName("vod_name")
    val name: String,
    @SerializedName("type_id")
    val typeId: Int,
    @SerializedName("type_name")
    val typeName: String,
    @SerializedName("vod_pic")
    val coverUrl: String,
    @SerializedName("vod_blurb")
    val blurb: String,
    @SerializedName("vod_content")
    val content: String,
    @SerializedName("vod_year")
    val year: String,
    @SerializedName("vod_area")
    val area: String,
    @SerializedName("vod_lang")
    val language: String,
    @SerializedName("vod_duration")
    val duration: String,
    @SerializedName("vod_score")
    val score: String,
    @SerializedName("vod_time")
    val updateTime: String,
    @SerializedName("vod_play_from")
    val playFrom: String,
    @SerializedName("vod_play_url")
    val playUrl: String,
    @SerializedName("vod_hits")
    val hits: Int = 0,
    @SerializedName("vod_hits_day")
    val hitsDay: Int = 0,
    @SerializedName("vod_hits_week")
    val hitsWeek: Int = 0,
    @SerializedName("vod_hits_month")
    val hitsMonth: Int = 0
)
