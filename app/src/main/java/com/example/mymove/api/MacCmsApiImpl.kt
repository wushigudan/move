package com.example.mymove.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

private const val TAG = "MacCmsApiImpl"

class MacCmsApiImpl(private val videoApi: VideoApi) : MacCmsApi {
    // 基础API调用方法
    private suspend fun getVideoList(
        action: String = "list",
        page: Int = 1,
        typeId: Int? = null,
        keyword: String? = null,
        hours: Int? = null,
        apiType: String = "json"
    ): ApiResponse = videoApi.getVideoList(action, page, typeId, keyword, hours, apiType)

    private suspend fun getCategories(
        action: String = "class",
        apiType: String = "json"
    ): ApiResponse = videoApi.getCategories(action, apiType)

    // 新接口实现
    override suspend fun getAllCategories(): ApiResult<TypeInfo> = withContext(Dispatchers.IO) {
        try {
            val response = getCategories()
            ApiResult.fromJson(response.toMap()) { json ->
                TypeInfo.fromJson(json)
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取分类失败", e)
            ApiResult(
                code = -1,
                msg = "获取分类失败: ${e.message}"
            )
        }
    }

    // 保留旧接口实现
    override suspend fun getParentCategories(
        action: String,
        apiType: String
    ): ApiResponse = getCategories(action, apiType).let { response ->
        ApiResponse(
            code = response.code,
            msg = response.msg,
            page = response.page,
            pageCount = response.pageCount,
            limit = response.limit,
            total = response.total,
            list = response.list,
            categories = response.categories.filter { it.parentId == 0 }
        )
    }

    override suspend fun getChildCategories(
        parentId: Int,
        action: String,
        apiType: String
    ): ApiResponse = getCategories(action, apiType).let { response ->
        ApiResponse(
            code = response.code,
            msg = response.msg,
            page = response.page,
            pageCount = response.pageCount,
            limit = response.limit,
            total = response.total,
            list = response.list,
            categories = response.categories.filter { it.parentId == parentId }
        )
    }

    override suspend fun getCategoryVideos(
        typeId: Int,
        page: Int,
        limit: Int,
        order: String
    ): ApiResult<VodInfo> = withContext(Dispatchers.IO) {
        val response = getVideoList(
            action = "list",
            page = page,
            typeId = typeId
        )
        ApiResult.fromJson(response.toMap()) { json ->
            VodInfo.fromJson(json)
        }
    }

    override suspend fun getVideoDetail(vodId: Int): ApiResult<VodInfo> = withContext(Dispatchers.IO) {
        val response = videoApi.getVideoDetail(
            action = "detail",
            ids = vodId.toString(),
            apiType = "json"
        )
        ApiResult.fromJson(response.toMap()) { json ->
            VodInfo.fromJson(json)
        }
    }

    // 旧的视频详情接口实现
    override suspend fun getVideoDetail(
        ids: String,
        action: String,
        apiType: String
    ): ApiResponse = videoApi.getVideoDetail(action, ids, apiType)

    override suspend fun searchVideos(
        keyword: String,
        page: Int,
        limit: Int
    ): ApiResult<VodInfo> = withContext(Dispatchers.IO) {
        try {
            val response = getVideoList(
                action = "list",
                page = page,
                keyword = keyword
            )
            
            if (response.code != 1) {
                return@withContext ApiResult(
                    code = response.code,
                    msg = response.msg ?: "搜索失败"
                )
            }
            
            ApiResult.fromJson(response.toMap()) { json ->
                VodInfo.fromJson(json)
            }
        } catch (e: Exception) {
            ApiResult(
                code = -1,
                msg = "搜索出错: ${e.message}"
            )
        }
    }

    override suspend fun getRecentVideos(
        page: Int,
        limit: Int,
        hours: Int?
    ): ApiResult<VodInfo> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "获取最新视频: page=$page, limit=$limit, hours=$hours")
            val response = getVideoList(
                action = "list",
                page = page,
                hours = hours
            )
            
            if (response.code != 1) {
                Log.w(TAG, "获取最新视频失败: ${response.msg}")
                return@withContext ApiResult(
                    code = response.code,
                    msg = response.msg ?: "获取最新视频失败"
                )
            }
            
            ApiResult.fromJson(response.toMap()) { json ->
                VodInfo.fromJson(json)
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取最新视频出错", e)
            ApiResult(
                code = -1,
                msg = "获取最新视频失败: ${e.message}"
            )
        }
    }

    override suspend fun filterVideos(
        filters: Map<String, String>,
        page: Int,
        limit: Int
    ): ApiResult<VodInfo> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "筛选视频: filters=$filters, page=$page, limit=$limit")
            
            // 构建筛选参数
            val typeId = filters["type"]?.toIntOrNull()
            val year = filters["year"]
            val area = filters["area"]
            
            // 调用基础API
            val response = getVideoList(
                action = "list",
                page = page,
                typeId = typeId
            )
            
            if (response.code != 1) {
                Log.w(TAG, "筛选视频失败: ${response.msg}")
                return@withContext ApiResult(
                    code = response.code,
                    msg = response.msg ?: "筛选视频失败"
                )
            }
            
            // 转换为 VodInfo
            val result = ApiResult.fromJson(response.toMap()) { json ->
                VodInfo.fromJson(json)
            }
            
            // 在内存中进行额外的筛选
            if (year != null || area != null) {
                val filteredList = result.list.filter { vod ->
                    (year == null || vod.vodYear == year) &&
                    (area == null || vod.vodArea == area)
                }
                
                result.copy(
                    list = filteredList,
                    total = filteredList.size
                )
            } else {
                result
            }
        } catch (e: Exception) {
            Log.e(TAG, "筛选视频出错", e)
            ApiResult(
                code = -1,
                msg = "筛选视频失败: ${e.message}"
            )
        }
    }

    // 工具方法
    private fun ApiResponse.toMap(): Map<String, Any?> = mapOf(
        "code" to code,
        "msg" to msg,
        "page" to page,
        "pagecount" to pageCount,
        "limit" to limit,
        "total" to total,
        "list" to list,
        "type" to categories
    )

    companion object {
        @Volatile
        private var instance: MacCmsApiImpl? = null

        fun getInstance(videoApi: VideoApi): MacCmsApiImpl =
            instance ?: synchronized(this) {
                instance ?: MacCmsApiImpl(videoApi).also { instance = it }
            }
    }
}
