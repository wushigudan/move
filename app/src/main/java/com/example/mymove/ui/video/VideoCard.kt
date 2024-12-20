package com.example.mymove.ui.video

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mymove.api.VideoSource
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import android.widget.Toast

private const val TAG = "VideoCard"

@Composable
fun VideoCard(
    video: VideoSource,
    onClick: () -> Unit
) {
    Log.d(TAG, "开始渲染 VideoCard: video.name=${video.name}")
    Log.d(TAG, "VideoCard 数据: id=${video.id}, typeId=${video.typeId}, typeName=${video.typeName}")
    Log.d(TAG, "VideoCard 可选数据: thumbnail=${video.thumbnail}, rating=${video["rating"]}, duration=${video["formattedDuration"]}")

    val context = LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                Log.d(TAG, "VideoCard 被点击: ${video.name}")
                try {
                    if (video.id == 0 || video.typeId == 0) {
                        // 显示错误提示
                        val errorMsg = "无法获取视频信息: id=${video.id}, typeId=${video.typeId}"
                        Log.e(TAG, errorMsg)
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d(TAG, "正在导航到视频详情页: id=${video.id}, name=${video.name}, typeId=${video.typeId}")
                        onClick()
                    }
                } catch (e: Exception) {
                    val errorMsg = "打开视频详情失败: ${e.message}"
                    Log.e(TAG, errorMsg, e)
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
    ) {
        Box {
            // 视频缩略图
            Log.d(TAG, "加载缩略图: ${video.name}, URL: ${video.thumbnail}")
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(video.thumbnail ?: "https://via.placeholder.com/300x400.png?text=No+Preview")
                    .crossfade(true)
                    .fallback(androidx.core.R.drawable.ic_call_answer)
                    .error(androidx.core.R.drawable.ic_call_answer)
                    .listener(
                        onError = { _, result ->
                            Log.e(TAG, "Error loading thumbnail for ${video.name}: ${result.throwable.message}", result.throwable)
                        },
                        onSuccess = { _, _ ->
                            Log.d(TAG, "Successfully loaded thumbnail for ${video.name}")
                        }
                    )
                    .build(),
                contentDescription = video.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
            )
            
            // 视频质量标签
            video["qualityTag"]?.toString()?.let { qualityTag ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = when (qualityTag) {
                                "超清4K" -> Color(0xFF2196F3)
                                "高清" -> Color(0xFF4CAF50)
                                else -> Color(0xFF9E9E9E)
                            },
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = qualityTag,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
            
            // 时长标签
            video["formattedDuration"]?.toString()?.let { duration ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = duration,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        
        // 视频信息
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // 标题
            Text(
                text = video.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 评分和类型
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = video["rating"]?.toString() ?: "暂无评分",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFA000)
                    ),
                    maxLines = 1
                )
                
                Text(
                    text = video.typeName ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }
            
            Spacer(modifier = Modifier.height(2.dp))
            
            // 更新时间
            Text(
                text = video["formattedPubTime"]?.toString() ?: "",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
