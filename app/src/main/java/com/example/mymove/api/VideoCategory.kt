package com.example.mymove.api

import com.google.gson.annotations.SerializedName

data class VideoCategory(
    @SerializedName("type_id")
    val id: Int,
    @SerializedName("type_pid")
    val parentId: Int,
    @SerializedName("type_name")
    val name: String
)
