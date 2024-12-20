package com.example.mymove.ui.video

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mymove.api.VideoApi
import com.example.mymove.api.VideoSource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    videoApi: VideoApi,
    onVideoClick: (VideoSource) -> Unit
) {
    var videoList by remember { mutableStateOf<List<VideoSource>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(1) }
    var hasMorePages by remember { mutableStateOf(true) }
    val listState = rememberLazyStaggeredGridState()
    val scope = rememberCoroutineScope()

    // 加载视频数据
    suspend fun loadVideos(page: Int, isRefresh: Boolean = false) {
        try {
            isLoading = true
            Log.d("HomeScreen", "Fetching videos for page $page...")
            val response = videoApi.getVideoList(
                action = "detail",
                page = page,
                hours = 24,
                apiType = "maccms10"
            )
            Log.d("HomeScreen", "Received ${response.list.size} videos")
            response.list.forEach { video ->
                Log.d("HomeScreen", "Video: ${video.name}, Thumbnail: ${video.thumbnail}")
            }
            Log.d("HomeScreen", "Total pages: ${response.pageCount}")
            
            videoList = if (isRefresh) response.list else videoList + response.list
            hasMorePages = page < response.pageCount
            error = null
        } catch (e: Exception) {
            error = e.message
            Log.e("HomeScreen", "Error loading videos: ${e.message}", e)
        } finally {
            isLoading = false
        }
    }

    // 初始加载
    LaunchedEffect(Unit) {
        loadVideos(1, true)
    }

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
                title = { Text("最近更新") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                error != null && videoList.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("加载失败: $error")
                        Button(onClick = {
                            scope.launch {
                                loadVideos(1, true)
                            }
                        }) {
                            Text("重试")
                        }
                    }
                }
                videoList.isEmpty() && !isLoading -> {
                    Text(
                        "暂无视频",
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalItemSpacing = 8.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(videoList) { video ->
                            VideoCard(
                                video = video,
                                onClick = { 
                                    Log.d("HomeScreen", "Video clicked: ${video.name}")
                                    try {
                                        onVideoClick(video)
                                    } catch (e: Exception) {
                                        Log.e("HomeScreen", "Error navigating to video detail: ${e.message}", e)
                                    }
                                }
                            )
                        }
                        
                        if (isLoading && videoList.isNotEmpty()) {
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
        }
    }
}
