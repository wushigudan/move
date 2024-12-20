package com.example.mymove.api

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("msg")
    val msg: String,
    @SerializedName("page")
    val page: Int = 1,
    @SerializedName("pagecount")
    val pageCount: Int = 1,
    @SerializedName("limit")
    val limit: String = "20",
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("list")
    val list: List<VideoSource> = emptyList(),
    @SerializedName("class")
    val categories: List<VideoCategory> = emptyList()
)
