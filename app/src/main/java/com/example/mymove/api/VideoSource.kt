package com.example.mymove.api

import com.google.gson.annotations.SerializedName
import androidx.annotation.Keep

@Keep
data class VideoSource(
    @SerializedName("vod_id")
    val id: Int = 0,
    @SerializedName("vod_name")
    val name: String = "",
    @SerializedName("type_id")
    val typeId: Int = 0,
    @SerializedName("type_name")
    val typeName: String = "",
    @SerializedName("vod_en")
    val nameEn: String? = null,
    @SerializedName("vod_time")
    val updateTime: String = "",
    @SerializedName("vod_remarks")
    val remarks: String = "",
    @SerializedName("vod_play_from")
    val playFrom: String = "",
    @SerializedName("vod_pic")
    val thumbnail: String = "",
    @SerializedName("vod_play_url")
    val playUrl: String = "",
    // 以下是可选字段
    @SerializedName("vod_sub")
    val subtitle: String? = null,
    @SerializedName("vod_letter")
    val letter: String? = null,
    @SerializedName("vod_actor")
    val actor: String? = null,
    @SerializedName("vod_director")
    val director: String? = null,
    @SerializedName("vod_blurb")
    val blurb: String? = null,
    @SerializedName("vod_area")
    val area: String? = null,
    @SerializedName("vod_lang")
    val language: String? = null,
    @SerializedName("vod_year")
    val year: String? = null,
    @SerializedName("vod_score")
    val score: String? = null,
    @SerializedName("vod_score_all")
    val scoreAll: String? = null,
    @SerializedName("vod_score_num")
    val scoreNum: String? = null,
    @SerializedName("vod_content")
    val content: String? = null,
    @Transient
    val extraProperties: MutableMap<String, Any?> = mutableMapOf()
) {
    operator fun get(key: String): Any? = when(key) {
        "vod_id" -> id
        "vod_name" -> name
        "type_id" -> typeId
        "type_name" -> typeName
        "vod_time" -> updateTime
        "vod_remarks" -> remarks
        "vod_play_from" -> playFrom
        "vod_pic" -> thumbnail
        "vod_play_url" -> playUrl
        else -> extraProperties[key]
    }

    operator fun set(key: String, value: Any?) {
        extraProperties[key] = value
    }
    
    // 便捷方法，用于链式调用
    fun withProperty(key: String, value: Any?): VideoSource {
        extraProperties[key] = value
        return this
    }
}
