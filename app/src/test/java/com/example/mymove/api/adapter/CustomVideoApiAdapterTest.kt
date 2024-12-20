package com.example.mymove.api.adapter

import com.example.mymove.api.ApiResponse
import com.example.mymove.api.VideoSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CustomVideoApiAdapterTest {
    private lateinit var adapter: CustomVideoApiAdapter

    @Before
    fun setup() {
        adapter = CustomVideoApiAdapter()
    }

    @Test
    fun `test duration formatting`() = runBlocking {
        // 创建测试数据
        val video = VideoSource(
            id = 1,
            name = "测试视频",
            typeId = 1,
            typeName = "电影",
            nameEn = null,
            updateTime = null,
            remarks = "125分钟",
            playFrom = null
        )
        val response = ApiResponse(code = 1, msg = "success", list = listOf(video))

        // 使用适配器处理响应
        val adaptedResponse = adapter.adaptResponse(response)
        val adaptedVideo = adaptedResponse.list.first()

        // 验证格式化的时长
        assertEquals("2小时5分钟", adaptedVideo["formattedDuration"])
    }

    @Test
    fun `test rating extraction`() = runBlocking {
        // 创建测试数据
        val video = VideoSource(
            id = 1,
            name = "测试视频",
            typeId = 1,
            typeName = "电影",
            nameEn = null,
            updateTime = null,
            remarks = "这是一个很好的视频 8.5分",
            playFrom = null
        )
        val response = ApiResponse(code = 1, msg = "success", list = listOf(video))

        // 使用适配器处理响应
        val adaptedResponse = adapter.adaptResponse(response)
        val adaptedVideo = adaptedResponse.list.first()

        // 验证提取的评分
        assertEquals("8.5", adaptedVideo["rating"])
    }

    @Test
    fun `test pub time formatting`() = runBlocking {
        // 创建测试数据
        val video = VideoSource(
            id = 1,
            name = "测试视频",
            typeId = 1,
            typeName = "电影",
            nameEn = null,
            updateTime = "2024-12-14",
            remarks = null,
            playFrom = null
        )
        val response = ApiResponse(code = 1, msg = "success", list = listOf(video))

        // 使用适配器处理响应
        val adaptedResponse = adapter.adaptResponse(response)
        val adaptedVideo = adaptedResponse.list.first()

        // 验证格式化的发布时间
        assertEquals("2024-12-14 00:00:00", adaptedVideo["formattedPubTime"])
    }

    @Test
    fun `test quality tag detection`() = runBlocking {
        // 创建测试数据
        val videos = listOf(
            VideoSource(id = 1, name = "测试视频 HD版本", typeId = 1, typeName = "电影", nameEn = null, updateTime = null, remarks = null, playFrom = null),
            VideoSource(id = 2, name = "测试视频 4K版本", typeId = 1, typeName = "电影", nameEn = null, updateTime = null, remarks = null, playFrom = null),
            VideoSource(id = 3, name = "普通测试视频", typeId = 1, typeName = "电影", nameEn = null, updateTime = null, remarks = null, playFrom = null)
        )
        val response = ApiResponse(code = 1, msg = "success", list = videos)

        // 使用适配器处理响应
        val adaptedResponse = adapter.adaptResponse(response)
        val adaptedVideos = adaptedResponse.list

        // 验证质量标签
        assertEquals("高清", adaptedVideos[0]["qualityTag"])
        assertEquals("超清4K", adaptedVideos[1]["qualityTag"])
        assertEquals("标清", adaptedVideos[2]["qualityTag"])
    }

    @Test
    fun `test missing or invalid data handling`() = runBlocking {
        // 创建测试数据
        val video = VideoSource(
            id = 1,
            name = "测试视频",
            typeId = 1,
            typeName = null,
            nameEn = null,
            updateTime = null,
            remarks = null,
            playFrom = null
        )
        val response = ApiResponse(code = 1, msg = "success", list = listOf(video))

        // 使用适配器处理响应
        val adaptedResponse = adapter.adaptResponse(response)
        val adaptedVideo = adaptedResponse.list.first()

        // 验证默认值处理
        assertEquals("未知时长", adaptedVideo["formattedDuration"])
        assertEquals("暂无评分", adaptedVideo["rating"])
        assertEquals("未知时间", adaptedVideo["formattedPubTime"])
        assertEquals("标清", adaptedVideo["qualityTag"])
    }
}
