package com.example.mymove.api.adapter

import com.example.mymove.api.ApiResponse

interface VideoApiAdapter {
    suspend fun adaptResponse(response: ApiResponse): ApiResponse
}

// 默认适配器实现
class DefaultVideoApiAdapter : VideoApiAdapter {
    override suspend fun adaptResponse(response: ApiResponse): ApiResponse = response
}
