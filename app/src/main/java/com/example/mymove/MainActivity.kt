package com.example.mymove

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.mymove.api.RetrofitClient
import com.example.mymove.api.adapter.CustomVideoApiAdapter
import com.example.mymove.navigation.NavGraph
import com.example.mymove.navigation.Screen
import com.example.mymove.ui.theme.MymoveTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化 Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // 初始化 RetrofitClient
        RetrofitClient.init(this)
        // 设置自定义适配器
        RetrofitClient.setApiAdapter(CustomVideoApiAdapter())
        Timber.d("已设置自定义视频 API 适配器")
        
        setContent {
            MymoveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    // 添加一个状态来触发重新加载
                    var apiUpdateCounter by remember { mutableStateOf(0) }
                    
                    Scaffold(
                        bottomBar = {
                            if (currentDestination?.route in listOf(Screen.Home.route, Screen.Category.route, Screen.Settings.route)) {
                                NavigationBar {
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Home, contentDescription = "首页") },
                                        label = { Text("首页") },
                                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Home.route } == true,
                                        onClick = {
                                            navController.navigate(Screen.Home.route) {
                                                popUpTo(Screen.Home.route) { inclusive = true }
                                            }
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.List, contentDescription = "分类") },
                                        label = { Text("分类") },
                                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Category.route } == true,
                                        onClick = {
                                            navController.navigate(Screen.Category.route) {
                                                popUpTo(Screen.Category.route) { inclusive = true }
                                            }
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Default.Settings, contentDescription = "设置") },
                                        label = { Text("设置") },
                                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Settings.route } == true,
                                        onClick = {
                                            navController.navigate(Screen.Settings.route) {
                                                popUpTo(Screen.Settings.route) { inclusive = true }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(paddingValues)
                        ) {
                            NavGraph(
                                navController = navController,
                                apiUpdateCounter = apiUpdateCounter,
                                onApiUpdated = { apiUpdateCounter++ }
                            )
                        }
                    }
                }
            }
        }
    }
}