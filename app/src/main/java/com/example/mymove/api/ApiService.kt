package com.example.mymove.api

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api.php/provide/vod/")
    suspend fun getVideoDetail(
        @Query("ac") ac: String = "detail",
        @Query("ids") ids: Int
    ): VideoDetailResponse
}

data class VideoDetailResponse(
    val code: Int,
    val msg: String,
    val list: List<VideoSource>
)
