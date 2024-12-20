package com.example.mymove.ui.video

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mymove.api.RetrofitClient
import com.example.mymove.api.VideoSource
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.CardDefaults

private const val TAG = "VideoDetailScreen"

private data class Episode(
    val name: String,
    val url: String
)

private fun parseEpisodes(playUrl: String): List<Episode> {
    Log.d(TAG, "Parsing episodes from playUrl: $playUrl")
    return playUrl.split("#").mapNotNull { episodeStr ->
        Log.d(TAG, "Processing episode string: $episodeStr")
        val parts = episodeStr.split("$")
        if (parts.size >= 2) {
            val episode = Episode(parts[0], parts[1].trim())
            Log.d(TAG, "Created episode: name=${episode.name}, url=${episode.url}")
            // 验证 URL 格式
            if (!episode.url.startsWith("http://") && !episode.url.startsWith("https://")) {
                Log.w(TAG, "Invalid URL format: ${episode.url}")
                null
            } else {
                episode
            }
        } else {
            Log.w(TAG, "Invalid episode format: $episodeStr")
            null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailScreen(
    videoId: Int,
    videoName: String,
    typeId: Int,
    onBackClick: () -> Unit
) {
    var errorState by remember { mutableStateOf<String?>(null) }
    var videoState by remember { mutableStateOf<VideoSource?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var episodes by remember { mutableStateOf<List<Episode>>(emptyList()) }
    var currentEpisode by remember { mutableStateOf<Episode?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    LaunchedEffect(videoId) {
        try {
            if (videoId == 0) {
                throw IllegalArgumentException("无效的视频ID")
            }
            isLoading = true
            Log.d(TAG, "Loading video details for ID: $videoId")
            val response = RetrofitClient.videoApi.getVideoDetail(
                action = "detail",
                ids = videoId.toString(),
                apiType = "maccms10"
            ).also { response ->
                Log.d(TAG, "API Response: $response")
            }
            
            if (response.code == 1 && response.list.isNotEmpty()) {
                val adaptedResponse = RetrofitClient.videoApiAdapter.adaptResponse(response)
                videoState = adaptedResponse.list.firstOrNull()
                if (videoState == null) {
                    throw IllegalStateException("视频信息为空")
                }
                Log.d(TAG, "Successfully loaded video: ${videoState?.name}")
            } else {
                throw IllegalStateException("加载视频详情失败: ${response.msg}")
            }
        } catch (e: Exception) {
            errorState = "加载失败: ${e.message}"
            Log.e(TAG, "Error loading video details", e)
            Toast.makeText(context, errorState, Toast.LENGTH_LONG).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(videoName) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
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
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                errorState != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorState ?: "未知错误",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onBackClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("返回")
                        }
                    }
                }
                videoState == null -> {
                    Text(
                        text = "无法加载视频信息",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    val video = videoState!!
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (isPlaying && currentEpisode != null) {
                            VideoPlayer(
                                url = currentEpisode!!.url,
                                onFullscreenChange = { isFullscreen ->
                                    // 如果需要，这里可以处理全屏状态变化
                                },
                                onBackClick = {
                                    isPlaying = false
                                }
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))

                                // 视频信息卡片
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        // 视频封面和播放按钮
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(16f / 9f)
                                                .clip(RoundedCornerShape(8.dp))
                                        ) {
                                            AsyncImage(
                                                model = video.thumbnail,
                                                contentDescription = video.name,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            
                                            // 播放按钮
                                            IconButton(
                                                onClick = {
                                                    Log.d(TAG, "Play button clicked")
                                                    if (video.playUrl != null) {
                                                        Log.d(TAG, "Video playUrl: ${video.playUrl}")
                                                        episodes = parseEpisodes(video.playUrl)
                                                        Log.d(TAG, "Parsed ${episodes.size} episodes")
                                                        if (episodes.isNotEmpty()) {
                                                            currentEpisode = episodes.first()
                                                            Log.d(TAG, "Playing first episode: name=${currentEpisode?.name}, url=${currentEpisode?.url}")
                                                            try {
                                                                isPlaying = true
                                                                Log.d(TAG, "Started playing video")
                                                            } catch (e: Exception) {
                                                                Log.e(TAG, "Error playing video", e)
                                                                isPlaying = false
                                                            }
                                                        } else {
                                                            Log.w(TAG, "No valid episodes found")
                                                            Toast.makeText(context, "没有可播放的剧集", Toast.LENGTH_SHORT).show()
                                                        }
                                                    } else {
                                                        Log.w(TAG, "Video playUrl is null")
                                                        Toast.makeText(context, "视频地址不可用", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .size(64.dp)
                                                    .background(
                                                        color = Color.Black.copy(alpha = 0.6f),
                                                        shape = RoundedCornerShape(32.dp)
                                                    )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "播放",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // 标题
                                        Text(
                                            text = video.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // 评分和质量标签
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // 评分
                                            video["rating"]?.toString()?.let { rating ->
                                                Text(
                                                    text = "评分：$rating",
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        color = Color(0xFFFFA000),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                )
                                            }
                                            
                                            // 质量标签
                                            video["qualityTag"]?.toString()?.let { qualityTag ->
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            color = when (qualityTag) {
                                                                "超清4K" -> Color(0xFF2196F3)
                                                                "高清" -> Color(0xFF4CAF50)
                                                                else -> Color(0xFF9E9E9E)
                                                            },
                                                            shape = RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text(
                                                        text = qualityTag,
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.labelMedium
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // 剧集列表
                                        if (video.playUrl != null) {
                                            val episodeList = parseEpisodes(video.playUrl)
                                            if (episodeList.isNotEmpty()) {
                                                Text(
                                                    text = "选集",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    modifier = Modifier.padding(bottom = 8.dp)
                                                )
                                                
                                                Column(
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    // 将剧集列表分组，每组5个
                                                    episodeList.chunked(5).forEach { rowEpisodes ->
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 4.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            rowEpisodes.forEach { episode ->
                                                                OutlinedCard(
                                                                    onClick = {
                                                                        currentEpisode = episode
                                                                        isPlaying = true
                                                                    },
                                                                    modifier = Modifier
                                                                        .weight(1f)
                                                                        .aspectRatio(1.5f), // 设置宽高比
                                                                    colors = CardDefaults.outlinedCardColors(
                                                                        containerColor = if (episode == currentEpisode)
                                                                            MaterialTheme.colorScheme.primaryContainer
                                                                        else
                                                                            MaterialTheme.colorScheme.surface
                                                                    ),
                                                                    border = BorderStroke(
                                                                        width = 1.dp,
                                                                        color = if (episode == currentEpisode)
                                                                            MaterialTheme.colorScheme.primary
                                                                        else
                                                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                                    )
                                                                ) {
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .fillMaxSize()
                                                                            .padding(4.dp),
                                                                        contentAlignment = Alignment.Center
                                                                    ) {
                                                                        Text(
                                                                            text = episode.name,
                                                                            style = MaterialTheme.typography.bodyMedium,
                                                                            color = if (episode == currentEpisode)
                                                                                MaterialTheme.colorScheme.primary
                                                                            else
                                                                                MaterialTheme.colorScheme.onSurface,
                                                                            maxLines = 1
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                            // 如果这一行不满5个，添加空的weight来保持对齐
                                                            repeat(5 - rowEpisodes.size) {
                                                                Spacer(modifier = Modifier
                                                                    .weight(1f)
                                                                    .aspectRatio(1.5f))
                                                            }
                                                        }
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.height(16.dp))
                                            }
                                        }

                                        // 视频信息
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            video["formattedDuration"]?.toString()?.let { duration ->
                                                InfoItem("时长", duration)
                                            }
                                            video.typeName?.let { type ->
                                                InfoItem("类型", type)
                                            }
                                            video["formattedPubTime"]?.toString()?.let { time ->
                                                InfoItem("更新时间", time)
                                            }
                                            video.remarks?.let { remarks ->
                                                InfoItem("备注", remarks)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
