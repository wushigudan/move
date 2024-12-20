package com.example.mymove.api

interface MacCmsApi {
    /**
     * 获取所有分类（包含父分类和子分类）
     */
    suspend fun getAllCategories(): ApiResult<TypeInfo>

    /**
     * 获取父分类列表
     * 
     * @deprecated 使用 getAllCategories() 替代，将在后续版本移除
     */
    @Deprecated("使用 getAllCategories() 替代，将在后续版本移除")
    suspend fun getParentCategories(
        action: String = "class",
        apiType: String = "json"
    ): ApiResponse

    /**
     * 获取指定父分类下的子分类列表
     * @param parentId 父分类ID
     * 
     * @deprecated 使用 getAllCategories() 替代，将在后续版本移除
     */
    @Deprecated("使用 getAllCategories() 替代，将在后续版本移除")
    suspend fun getChildCategories(
        parentId: Int,
        action: String = "class",
        apiType: String = "json"
    ): ApiResponse

    /**
     * 获取分类下的视频列表
     * @param typeId 分类ID
     * @param page 页码
     * @param limit 每页数量
     * @param order 排序方式：time(时间), hits(点击), score(评分)
     */
    suspend fun getCategoryVideos(
        typeId: Int,
        page: Int = 1,
        limit: Int = 20,
        order: String = ORDER_BY_TIME
    ): ApiResult<VodInfo>

    /**
     * 获取视频详情
     * @param vodId 视频ID
     */
    suspend fun getVideoDetail(vodId: Int): ApiResult<VodInfo>

    /**
     * 搜索视频
     * @param keyword 搜索关键词
     * @param page 页码
     * @param limit 每页数量
     */
    suspend fun searchVideos(
        keyword: String,
        page: Int = 1,
        limit: Int = 20
    ): ApiResult<VodInfo>

    /**
     * 获取最新更新的视频
     * @param page 页码
     * @param limit 每页数量
     * @param hours 最近小时数，如 24 表示最近24小时更新的视频
     */
    suspend fun getRecentVideos(
        page: Int = 1,
        limit: Int = 20,
        hours: Int? = null
    ): ApiResult<VodInfo>

    /**
     * 按条件筛选视频
     * @param filters 筛选条件，如地区、年份、类型等
     * @param page 页码
     * @param limit 每页数量
     */
    suspend fun filterVideos(
        filters: Map<String, String>,
        page: Int = 1,
        limit: Int = 20
    ): ApiResult<VodInfo>

    /**
     * 获取视频详情
     * @param ids 视频ID
     * 
     * @deprecated 使用 getVideoDetail(vodId: Int) 替代，将在后续版本移除
     */
    @Deprecated("使用 getVideoDetail(vodId: Int) 替代，将在后续版本移除")
    suspend fun getVideoDetail(
        ids: String,
        action: String = "detail",
        apiType: String = "json"
    ): ApiResponse

    companion object {
        // 排序方式常量
        const val ORDER_BY_TIME = "time"   // 按时间排序
        const val ORDER_BY_HITS = "hits"   // 按点击排序
        const val ORDER_BY_SCORE = "score" // 按评分排序

        // 常用筛选字段
        const val FILTER_AREA = "area"     // 地区
        const val FILTER_YEAR = "year"     // 年份
        const val FILTER_TYPE = "type"     // 类型
        const val FILTER_LANG = "lang"     // 语言
    }
}
