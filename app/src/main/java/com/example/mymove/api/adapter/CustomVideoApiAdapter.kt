package com.example.mymove.api.adapter

import com.example.mymove.api.ApiResponse
import com.example.mymove.api.VideoSource

class CustomVideoApiAdapter : VideoApiAdapter {
    override suspend fun adaptResponse(response: ApiResponse): ApiResponse {
        return ApiResponse(
            code = response.code,
            msg = response.msg,
            page = response.page,
            pageCount = response.pageCount,
            limit = response.limit,
            total = response.total,
            categories = response.categories ?: emptyList(),
            list = response.list.map { video ->
                video.apply {
                    // 添加格式化的时长
                    val durationInMinutes = remarks.let { 
                        val durationMatch = Regex("""(\d+)分钟""").find(it)
                        durationMatch?.groupValues?.get(1)?.toIntOrNull()
                    } ?: 0
                    
                    val formattedDuration = when {
                        durationInMinutes >= 60 -> "${durationInMinutes / 60}小时${durationInMinutes % 60}分钟"
                        durationInMinutes > 0 -> "${durationInMinutes}分钟"
                        else -> "未知时长"
                    }
                    this["formattedDuration"] = formattedDuration

                    // 添加评分
                    val rating = remarks.let { 
                        val ratingMatch = Regex("""(\d+(\.\d+)?)分""").find(it)
                        ratingMatch?.groupValues?.get(1)?.toFloatOrNull()
                    }
                    this["rating"] = rating?.toString() ?: "暂无评分"

                    // 格式化发布时间
                    this["formattedPubTime"] = when {
                        updateTime.isBlank() -> "未知时间"
                        updateTime.length == 10 -> "$updateTime 00:00:00"
                        else -> updateTime
                    }

                    // 视频质量标签
                    val qualityTag = when {
                        name.contains("HD", ignoreCase = true) -> "高清"
                        name.contains("4K", ignoreCase = true) -> "超清4K"
                        else -> "标清"
                    }
                    this["qualityTag"] = qualityTag
                }
            }
        )
    }
}
