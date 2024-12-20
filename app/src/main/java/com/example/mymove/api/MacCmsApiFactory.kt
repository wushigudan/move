package com.example.mymove.api

/**
 * MacCmsApi工厂类
 * 用于统一管理API实例的创建
 */
object MacCmsApiFactory {
    @Volatile
    private var api: MacCmsApi? = null

    fun create(videoApi: VideoApi): MacCmsApi =
        api ?: synchronized(this) {
            api ?: MacCmsApiImpl(videoApi).also { api = it }
        }

    fun get(): MacCmsApi = api
        ?: throw IllegalStateException("MacCmsApi not initialized. Call create() first.")
}
