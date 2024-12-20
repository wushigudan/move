package com.example.mymove.api

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.POST
import retrofit2.http.Body

interface VideoApi {
    @GET(".")
    suspend fun getVideoList(
        @Query("ac") action: String = "list",
        @Query("pg") page: Int = 1,
        @Query("t") typeId: Int? = null,
        @Query("wd") keyword: String? = null,
        @Query("h") hours: Int? = null,
        @Query("at") apiType: String = "json"
    ): ApiResponse

    @GET(".")
    suspend fun getVideoDetail(
        @Query("ac") action: String = "detail",
        @Query("ids") ids: String,
        @Query("at") apiType: String = "json"
    ): ApiResponse

    @GET(".")
    suspend fun getCategories(
        @Query("ac") action: String = "class",
        @Query("at") apiType: String = "json"
    ): ApiResponse
}
