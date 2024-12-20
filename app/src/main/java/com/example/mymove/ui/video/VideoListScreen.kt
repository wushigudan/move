package com.example.mymove.ui.video

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mymove.api.RetrofitClient
import com.example.mymove.api.VideoSource
import com.example.mymove.ui.video.VideoCard
import kotlinx.coroutines.launch
import android.util.Log

private const val TAG = "VideoListScreen"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun VideoListScreen(
    categoryId: Int,
    categoryName: String,
    onBackClick: () -> Unit,
    onVideoClick: (VideoSource) -> Unit
) {
    var videos by remember { mutableStateOf<List<VideoSource>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(1) }
    var hasMorePages by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyStaggeredGridState()

    // 加载视频数据
    suspend fun loadVideos(page: Int, isRefresh: Boolean = false) {
        try {
            Log.d(TAG, "开始加载视频列表: page=$page, isRefresh=$isRefresh")
            val response = RetrofitClient.videoApi.getVideoList(
                action = "detail",
                page = page,
                typeId = categoryId,
                apiType = "maccms10"
            )
            Log.d(TAG, "获取到视频列表: size=${response.list.size}")
            response.list.forEach { video ->
                Log.d(TAG, "视频数据: id=${video.id}, name=${video.name}, thumbnail=${video.thumbnail}")
            }
            videos = if (isRefresh) response.list else videos + response.list
            hasMorePages = page < response.pageCount
            error = null
        } catch (e: Exception) {
            Log.e(TAG, "加载视频列表失败", e)
            error = e.message
        } finally {
            isLoading = false
            isRefreshing = false
        }
    }

    // 初始加载
    LaunchedEffect(categoryId) {
        try {
            val response = RetrofitClient.videoApi.getVideoList(
                action = "detail",
                typeId = categoryId,
                apiType = "maccms10"
            )
            videos = response.list
            isLoading = false
        } catch (e: Exception) {
            error = e.message
            isLoading = false
        }
    }

    // 下拉刷新状态
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                currentPage = 1
                loadVideos(1, true)
            }
        }
    )

    // 检测是否需要加载更多
    LaunchedEffect(listState) {
        snapshotFlow { 
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            
            lastVisibleItemIndex > totalItemsNumber - 3
        }.collect { shouldLoadMore ->
            if (shouldLoadMore && !isLoading && hasMorePages) {
                currentPage++
                loadVideos(currentPage)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(categoryName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
                when {
                    error != null && videos.isEmpty() -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("加载失败: $error")
                            Button(onClick = {
                                coroutineScope.launch {
                                    isLoading = true
                                    loadVideos(currentPage, true)
                                }
                            }) {
                                Text("重试")
                            }
                        }
                    }
                    else -> {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalItemSpacing = 4.dp,
                            state = listState
                        ) {
                            items(videos) { video ->
                                VideoCard(
                                    video = video,
                                    onClick = { onVideoClick(video) }
                                )
                            }
                            if (isLoading && videos.isNotEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
            if (isLoading && videos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
