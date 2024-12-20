package com.example.mymove.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mymove.api.RetrofitClient
import com.example.mymove.api.VideoSource
import com.example.mymove.ui.video.VideoCard
import android.util.Log

private const val TAG = "SearchScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    searchQuery: String,
    onBackClick: () -> Unit,
    onVideoClick: (VideoSource) -> Unit
) {
    var videos by remember { mutableStateOf<List<VideoSource>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(1) }
    var hasMorePages by remember { mutableStateOf(true) }
    val listState = rememberLazyStaggeredGridState()

    // 加载搜索结果
    suspend fun loadSearchResults(page: Int, isRefresh: Boolean = false) {
        try {
            Log.d(TAG, "开始搜索视频: query=$searchQuery, page=$page, isRefresh=$isRefresh")
            val response = RetrofitClient.videoApi.getVideoList(
                action = "list",
                page = page,
                keyword = searchQuery,
                apiType = "json"
            )
            Log.d(TAG, "搜索结果: size=${response.list.size}")
            response.list.forEach { video ->
                Log.d(TAG, "搜索结果视频: id=${video.id}, name=${video.name}, thumbnail=${video.thumbnail}")
            }
            videos = if (isRefresh) response.list else videos + response.list
            hasMorePages = page < response.pageCount
            error = null
        } catch (e: Exception) {
            Log.e(TAG, "搜索视频失败", e)
            error = e.message
        } finally {
            isLoading = false
        }
    }

    // 初始加载
    LaunchedEffect(searchQuery) {
        isLoading = true
        loadSearchResults(1, true)
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
                loadSearchResults(currentPage)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("搜索：$searchQuery") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                error != null && videos.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("搜索失败: $error")
                    }
                }
                videos.isEmpty() && !isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("未找到相关视频")
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
            
            if (isLoading && videos.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}
