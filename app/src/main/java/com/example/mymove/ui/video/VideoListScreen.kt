package com.example.mymove.ui.video

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mymove.api.RetrofitClient
import com.example.mymove.api.VideoSource
import kotlinx.coroutines.launch
import android.util.Log

private const val TAG = "VideoListScreen"

// 保存最后浏览状态的数据类
private data class LastViewState(
    val page: Int = 1,
    val scrollIndex: Int = 0,
    val scrollOffset: Int = 0
)

// 使用 remember 保存最后的浏览状态
private val lastViewStates = mutableMapOf<Int, LastViewState>()

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
    // 从上次状态恢复页码，如果没有则使用1
    var currentPage by remember { mutableStateOf(lastViewStates[categoryId]?.page ?: 1) }
    var pageCount by remember { mutableStateOf(1) }
    var hasMorePages by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showPagePicker by remember { mutableStateOf(false) }
    var pageInputError by remember { mutableStateOf<String?>(null) }
    var pageInput by remember { mutableStateOf("") }
    
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyStaggeredGridState(
        initialFirstVisibleItemIndex = lastViewStates[categoryId]?.scrollIndex ?: 0,
        initialFirstVisibleItemScrollOffset = lastViewStates[categoryId]?.scrollOffset ?: 0
    )

    // 加载视频数据
    suspend fun loadVideos(page: Int, isRefresh: Boolean = false) {
        try {
            if (isRefresh) {
                videos = emptyList()
            }
            
            Log.d(TAG, "开始加载视频列表: page=$page, isRefresh=$isRefresh")
            val response = RetrofitClient.videoApi.getVideoList(
                action = "detail",
                page = page,
                typeId = categoryId,
                apiType = "maccms10"
            )
            Log.d(TAG, "获取到视频列表: size=${response.list.size}")
            
            videos = if (isRefresh) response.list else videos + response.list
            pageCount = response.pageCount
            hasMorePages = page < pageCount
            currentPage = page
            error = null
        } catch (e: Exception) {
            Log.e(TAG, "加载视频列表失败", e)
            error = e.message
        } finally {
            isLoading = false
            isRefreshing = false
        }
    }

    // 保存浏览状态
    fun saveViewState() {
        lastViewStates[categoryId] = LastViewState(
            page = currentPage,
            scrollIndex = listState.firstVisibleItemIndex,
            scrollOffset = listState.firstVisibleItemScrollOffset
        )
    }

    // 在视频点击时保存状态
    val onVideoClickWithState: (VideoSource) -> Unit = { video ->
        saveViewState()
        onVideoClick(video)
    }

    // 初始加载
    LaunchedEffect(categoryId) {
        // 如果有保存的状态，加载对应页码
        val savedPage = lastViewStates[categoryId]?.page ?: 1
        loadVideos(savedPage, true)
    }

    // 下拉刷新状态
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
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
                loadVideos(currentPage + 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(categoryName)
                        Text(
                            text = "第 $currentPage/$pageCount 页",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 上一页按钮
                    IconButton(
                        onClick = {
                            if (currentPage > 1) {
                                coroutineScope.launch {
                                    isLoading = true
                                    loadVideos(currentPage - 1, true)
                                }
                            }
                        },
                        enabled = !isLoading && currentPage > 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "上一页"
                        )
                    }
                    
                    // 页码选择按钮
                    IconButton(
                        onClick = { showPagePicker = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "选择页码"
                        )
                    }
                    
                    // 下一页按钮
                    IconButton(
                        onClick = {
                            if (currentPage < pageCount) {
                                coroutineScope.launch {
                                    isLoading = true
                                    loadVideos(currentPage + 1, true)
                                }
                            }
                        },
                        enabled = !isLoading && currentPage < pageCount
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "下一页"
                        )
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
                            items(
                                count = videos.size,
                                key = { index -> videos[index].id }
                            ) { index ->
                                VideoCard(
                                    video = videos[index],
                                    onClick = { onVideoClickWithState(videos[index]) }
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
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp)
                                        )
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

    // 页码选择对话框
    if (showPagePicker) {
        AlertDialog(
            onDismissRequest = { 
                showPagePicker = false 
                pageInputError = null
                pageInput = ""
            },
            title = { Text("跳转到指定页码") },
            text = {
                Column {
                    if (pageInputError != null) {
                        Text(
                            text = pageInputError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    OutlinedTextField(
                        value = pageInput,
                        onValueChange = { 
                            pageInput = it.trim()
                            pageInputError = null
                        },
                        label = { Text("页码 (1-$pageCount)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                val page = pageInput.toIntOrNull()
                                when {
                                    page == null -> pageInputError = "请输入有效数字"
                                    page < 1 || page > pageCount -> pageInputError = "页码范围: 1-$pageCount"
                                    else -> {
                                        coroutineScope.launch {
                                            isLoading = true
                                            loadVideos(page, true)
                                            showPagePicker = false
                                            pageInput = ""
                                        }
                                    }
                                }
                            }
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val page = pageInput.toIntOrNull()
                        when {
                            page == null -> pageInputError = "请输入有效数字"
                            page < 1 || page > pageCount -> pageInputError = "页码范围: 1-$pageCount"
                            else -> {
                                coroutineScope.launch {
                                    isLoading = true
                                    loadVideos(page, true)
                                    showPagePicker = false
                                    pageInput = ""
                                }
                            }
                        }
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showPagePicker = false
                        pageInputError = null
                        pageInput = ""
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}
