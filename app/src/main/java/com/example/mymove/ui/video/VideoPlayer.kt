package com.example.mymove.ui.video

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.util.Rational
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlin.math.abs

@Composable
fun VideoPlayer(
    url: String,
    onFullscreenChange: (Boolean) -> Unit = {},
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember { context as Activity }
    var isFullscreen by remember { mutableStateOf(false) }
    var lastClickTime by remember { mutableStateOf(0L) }
    var lastClickX by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var dragDelta by remember { mutableStateOf(0f) }
    var initialPosition by remember { mutableStateOf(0L) }
    
    // 音量和亮度控制
    var isVolumeControl by remember { mutableStateOf(false) }
    var isBrightnessControl by remember { mutableStateOf(false) }
    var initialVolume by remember { mutableStateOf(0) }
    var initialBrightness by remember { mutableStateOf(0f) }
    
    // 播放速度控制
    var showSpeedMenu by remember { mutableStateOf(false) }
    var currentSpeed by remember { mutableStateOf(1f) }
    val speedOptions = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)

    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }
    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    // 检查是否支持画中画
    val supportsPiP = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            activity.packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        } else false
    }

    // 进入画中画模式
    fun enterPiPMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val params = PictureInPictureParams.Builder()
                .setAspectRatio(Rational(16, 9))
                .build()
            activity.enterPictureInPictureMode(params)
        }
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val dataSourceFactory = DefaultDataSource.Factory(context)
            val mediaSource = when {
                url.endsWith(".m3u8", ignoreCase = true) -> {
                    HlsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(url))
                }
                else -> {
                    ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(url))
                }
            }
            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            showSystemUI(activity)
        }
    }

    // 处理双击事件
    fun handleDoubleClick(x: Float, width: Float) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < 300) { // 双击间隔小于300ms
            val section = width / 3
            when {
                x < section -> {
                    // 左侧双击，快退10秒
                    val newPosition = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
                    exoPlayer.seekTo(newPosition)
                }
                x > section * 2 -> {
                    // 右侧双击，快进10秒
                    val newPosition = (exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration)
                    exoPlayer.seekTo(newPosition)
                }
                else -> {
                    // 中间双击，切换播放/暂停
                    if (exoPlayer.isPlaying) {
                        exoPlayer.pause()
                    } else {
                        exoPlayer.play()
                    }
                }
            }
        }
        lastClickTime = currentTime
        lastClickX = x
    }

    // 计算快进/快退的时间
    fun calculateSeekTime(dragAmount: Float): Long {
        // 每个像素代表的时间（毫秒）
        val pixelToMs = 100L // 可以调整这个值来改变灵敏度
        return (dragAmount * pixelToMs).toLong()
    }

    // 格式化时间显示
    fun formatTime(timeMs: Long): String {
        val totalSeconds = timeMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // 处理垂直滑动
    fun handleVerticalDrag(dragX: Float, dragY: Float) {
        val isLeftSide = dragX < activity.window.decorView.width / 2
        
        if (isLeftSide) {
            // 左侧控制亮度
            val brightnessChange = -dragY / 1000 // 调整灵敏度
            val newBrightness = (initialBrightness + brightnessChange).coerceIn(0.01f, 1f)
            
            val layoutParams = activity.window.attributes
            layoutParams.screenBrightness = newBrightness
            activity.window.attributes = layoutParams
            
            isBrightnessControl = true
            isVolumeControl = false
        } else {
            // 右侧控制音量
            val volumeChange = -dragY / 100 // 调整灵敏度
            val newVolume = (initialVolume + volumeChange.toInt()).coerceIn(0, maxVolume)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
            
            isVolumeControl = true
            isBrightnessControl = false
        }
    }

    if (isFullscreen) {
        Dialog(
            onDismissRequest = {
                isFullscreen = false
                onFullscreenChange(false)
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                showSystemUI(activity)
            },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset -> 
                                isDragging = true
                                initialPosition = exoPlayer.currentPosition
                                
                                // 记录初始音量和亮度
                                initialVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                initialBrightness = activity.window.attributes.screenBrightness
                                if (initialBrightness < 0) initialBrightness = 0.5f
                            },
                            onDragEnd = {
                                if (isDragging) {
                                    val seekTime = calculateSeekTime(dragDelta)
                                    val newPosition = (initialPosition + seekTime)
                                        .coerceIn(0, exoPlayer.duration)
                                    exoPlayer.seekTo(newPosition)
                                }
                                isDragging = false
                                dragDelta = 0f
                                isVolumeControl = false
                                isBrightnessControl = false
                            },
                            onDragCancel = {
                                isDragging = false
                                dragDelta = 0f
                                isVolumeControl = false
                                isBrightnessControl = false
                            },
                            onDrag = { change: PointerInputChange, dragAmount: Offset ->
                                val (x, y) = dragAmount
                                if (abs(x) > abs(y)) {
                                    // 水平滑动
                                    dragDelta += x
                                    isVolumeControl = false
                                    isBrightnessControl = false
                                } else {
                                    // 垂直滑动
                                    handleVerticalDrag(change.position.x, y)
                                }
                            }
                        )
                    }
            ) {
                LaunchedEffect(Unit) {
                    hideSystemUI(activity)
                }
                
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = true
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                            setShowNextButton(false)
                            setShowPreviousButton(false)
                            layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                            
                            setOnClickListener { view ->
                                handleDoubleClick(lastClickX, view.width.toFloat())
                            }

                            setOnTouchListener { v, event ->
                                lastClickX = event.x
                                false // 返回false以允许点击事件继续传递
                            }
                            
                            setFullscreenButtonClickListener { isFullScreen ->
                                if (!isFullScreen) {
                                    isFullscreen = false
                                    onFullscreenChange(false)
                                    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                                }
                            }
                            findViewById<View>(androidx.media3.ui.R.id.exo_fullscreen).performClick()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 全屏模式下的返回按钮
                IconButton(
                    onClick = {
                        isFullscreen = false
                        onFullscreenChange(false)
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "退出全屏",
                        tint = Color.White
                    )
                }

                // 画中画按钮
                if (supportsPiP) {
                    IconButton(
                        onClick = { 
                            enterPiPMode()
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureInPicture,
                            contentDescription = "画中画模式",
                            tint = Color.White
                        )
                    }
                }

                // 播放速度控制按钮和菜单
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    IconButton(
                        onClick = { showSpeedMenu = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = "播放速度",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showSpeedMenu,
                        onDismissRequest = { showSpeedMenu = false }
                    ) {
                        speedOptions.forEach { speed ->
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = when (speed) {
                                            1f -> "正常"
                                            else -> "${speed}x"
                                        }
                                    )
                                },
                                onClick = {
                                    currentSpeed = speed
                                    exoPlayer.setPlaybackSpeed(speed)
                                    showSpeedMenu = false
                                },
                                leadingIcon = if (speed == currentSpeed) {
                                    { Icon(Icons.Default.Speed, null) }
                                } else null
                            )
                        }
                    }
                }

                // 显示拖动进度提示
                if (isDragging && abs(dragDelta) > 5f && !isVolumeControl && !isBrightnessControl) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(16.dp)
                    ) {
                        val seekTime = calculateSeekTime(dragDelta)
                        val newPosition = (initialPosition + seekTime).coerceIn(0, exoPlayer.duration)
                        Text(
                            text = if (dragDelta > 0) "快进至 ${formatTime(newPosition)}"
                                  else "快退至 ${formatTime(newPosition)}",
                            color = Color.White
                        )
                    }
                }

                // 显示音量或亮度调节提示
                if (isDragging && (isVolumeControl || isBrightnessControl)) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isVolumeControl) Icons.Default.VolumeUp 
                                            else Icons.Default.BrightnessHigh,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Text(
                                text = if (isVolumeControl) 
                                    "${(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / maxVolume)}%"
                                else 
                                    "${(activity.window.attributes.screenBrightness * 100).toInt()}%",
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(Color.Black)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset -> 
                            isDragging = true
                            initialPosition = exoPlayer.currentPosition
                            
                            // 记录初始音量和亮度
                            initialVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            initialBrightness = activity.window.attributes.screenBrightness
                            if (initialBrightness < 0) initialBrightness = 0.5f
                        },
                        onDragEnd = {
                            if (isDragging) {
                                val seekTime = calculateSeekTime(dragDelta)
                                val newPosition = (initialPosition + seekTime)
                                    .coerceIn(0, exoPlayer.duration)
                                exoPlayer.seekTo(newPosition)
                            }
                            isDragging = false
                            dragDelta = 0f
                            isVolumeControl = false
                            isBrightnessControl = false
                        },
                        onDragCancel = {
                            isDragging = false
                            dragDelta = 0f
                            isVolumeControl = false
                            isBrightnessControl = false
                        },
                        onDrag = { change: PointerInputChange, dragAmount: Offset ->
                            val (x, y) = dragAmount
                            if (abs(x) > abs(y)) {
                                // 水平滑动
                                dragDelta += x
                                isVolumeControl = false
                                isBrightnessControl = false
                            } else {
                                // 垂直滑动
                                handleVerticalDrag(change.position.x, y)
                            }
                        }
                    )
                }
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        setShowNextButton(false)
                        setShowPreviousButton(false)
                        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)

                        setOnClickListener { view ->
                            handleDoubleClick(lastClickX, view.width.toFloat())
                        }

                        setOnTouchListener { v, event ->
                            lastClickX = event.x
                            false // 返回false以允许点击事件继续传递
                        }
                        
                        setFullscreenButtonClickListener { isFullScreen ->
                            if (isFullScreen) {
                                isFullscreen = true
                                onFullscreenChange(true)
                                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
            )
            
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回",
                    tint = Color.White
                )
            }

            // 画中画按钮
            if (supportsPiP) {
                IconButton(
                    onClick = { 
                        enterPiPMode()
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureInPicture,
                        contentDescription = "画中画模式",
                        tint = Color.White
                    )
                }
            }

            // 播放速度控制按钮和菜单
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                IconButton(
                    onClick = { showSpeedMenu = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "播放速度",
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = showSpeedMenu,
                    onDismissRequest = { showSpeedMenu = false }
                ) {
                    speedOptions.forEach { speed ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = when (speed) {
                                        1f -> "正常"
                                        else -> "${speed}x"
                                    }
                                )
                            },
                            onClick = {
                                currentSpeed = speed
                                exoPlayer.setPlaybackSpeed(speed)
                                showSpeedMenu = false
                            },
                            leadingIcon = if (speed == currentSpeed) {
                                { Icon(Icons.Default.Speed, null) }
                            } else null
                        )
                    }
                }
            }

            // 显示拖动进度提示
            if (isDragging && abs(dragDelta) > 5f && !isVolumeControl && !isBrightnessControl) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(16.dp)
                ) {
                    val seekTime = calculateSeekTime(dragDelta)
                    val newPosition = (initialPosition + seekTime).coerceIn(0, exoPlayer.duration)
                    Text(
                        text = if (dragDelta > 0) "快进至 ${formatTime(newPosition)}"
                              else "快退至 ${formatTime(newPosition)}",
                        color = Color.White
                    )
                }
            }

            // 显示音量或亮度调节提示
            if (isDragging && (isVolumeControl || isBrightnessControl)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isVolumeControl) Icons.Default.VolumeUp 
                                        else Icons.Default.BrightnessHigh,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = if (isVolumeControl) 
                                "${(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) * 100 / maxVolume)}%"
                            else 
                                "${(activity.window.attributes.screenBrightness * 100).toInt()}%",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun hideSystemUI(activity: Activity) {
    WindowCompat.setDecorFitsSystemWindows(activity.window, false)
    WindowInsetsControllerCompat(activity.window, activity.window.decorView).let { controller ->
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

private fun showSystemUI(activity: Activity) {
    WindowCompat.setDecorFitsSystemWindows(activity.window, true)
    WindowInsetsControllerCompat(activity.window, activity.window.decorView).show(WindowInsetsCompat.Type.systemBars())
}
