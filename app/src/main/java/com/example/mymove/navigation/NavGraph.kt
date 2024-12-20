package com.example.mymove.navigation

import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mymove.api.RetrofitClient
import com.example.mymove.api.VideoSource
import com.example.mymove.data.SettingsDataStore
import com.example.mymove.ui.category.CategoryScreen
import com.example.mymove.ui.search.SearchDialog
import com.example.mymove.ui.search.SearchScreen
import com.example.mymove.ui.video.VideoDetailScreen
import com.example.mymove.ui.video.VideoListScreen
import com.example.mymove.ui.video.HomeScreen
import com.example.mymove.ui.settings.SettingsScreen
import java.net.URLDecoder
import java.net.URLEncoder
import kotlinx.coroutines.flow.first

private const val TAG = "NavGraph"

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    apiUpdateCounter: Int = 0,
    onApiUpdated: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsDataStore = remember { SettingsDataStore(context) }
    val apiBaseUrl by settingsDataStore.apiBaseUrl.collectAsState(initial = null)
    var showSearchDialog by remember { mutableStateOf(false) }

    // 检查API地址是否已设置
    LaunchedEffect(Unit) {
        try {
            val baseUrl = settingsDataStore.apiBaseUrl.first()
            if (baseUrl == null) {
                Log.d(TAG, "API URL not set, navigating to settings")
                navController.navigate(Screen.Settings.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking API URL", e)
        }
    }

    if (showSearchDialog) {
        SearchDialog(
            onDismiss = { showSearchDialog = false },
            onSearch = { query ->
                showSearchDialog = false
                navController.navigate(Screen.Search.createRoute(query))
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            if (apiBaseUrl != null) {
                LaunchedEffect(apiUpdateCounter) {
                    try {
                        if (RetrofitClient.isApiUrlSet) {
                            val response = RetrofitClient.videoApi.getCategories()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching categories", e)
                    }
                }
                HomeScreen(
                    videoApi = RetrofitClient.videoApi,
                    onVideoClick = { video ->
                        navController.navigate(Screen.VideoDetail.createRoute(
                            videoId = video.id ?: 0,
                            videoName = video.name ?: "",
                            typeId = video.typeId ?: 0
                        ))
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            }
        }

        composable(Screen.Category.route) {
            if (apiBaseUrl != null) {
                CategoryScreen(
                    onCategorySelected = { categoryId, categoryName ->
                        navController.navigate(Screen.VideoList.createRoute(categoryId, categoryName))
                    },
                    onSearchClick = { showSearchDialog = true }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(Screen.Category.route) { inclusive = true }
                    }
                }
            }
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    if (RetrofitClient.isApiUrlSet) {
                        navController.popBackStack()
                    }
                },
                onApiUpdated = onApiUpdated
            )
        }

        composable(
            route = Screen.VideoList.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.IntType },
                navArgument("categoryName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getInt("categoryId") ?: 0
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
            if (apiBaseUrl != null) {
                VideoListScreen(
                    categoryId = categoryId,
                    categoryName = categoryName,
                    onBackClick = { navController.popBackStack() },
                    onVideoClick = { video ->
                        navController.navigate(Screen.VideoDetail.createRoute(
                            videoId = video.id ?: 0,
                            videoName = video.name ?: "",
                            typeId = video.typeId ?: 0
                        ))
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(Screen.VideoList.route) { inclusive = true }
                    }
                }
            }
        }

        composable(
            route = Screen.VideoDetail.route,
            arguments = listOf(
                navArgument("videoId") { type = NavType.IntType },
                navArgument("videoName") { type = NavType.StringType },
                navArgument("typeId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getInt("videoId") ?: 0
            val videoName = backStackEntry.arguments?.getString("videoName")?.let {
                URLDecoder.decode(it, "UTF-8")
            } ?: ""
            val typeId = backStackEntry.arguments?.getInt("typeId") ?: 0
            if (apiBaseUrl != null) {
                VideoDetailScreen(
                    videoId = videoId,
                    videoName = videoName,
                    typeId = typeId,
                    onBackClick = { navController.popBackStack() }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(Screen.VideoDetail.route) { inclusive = true }
                    }
                }
            }
        }

        composable(
            route = Screen.Search.route,
            arguments = listOf(
                navArgument("query") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query")?.let {
                URLDecoder.decode(it, "UTF-8")
            } ?: ""
            if (apiBaseUrl != null) {
                SearchScreen(
                    searchQuery = query,
                    onBackClick = { navController.popBackStack() },
                    onVideoClick = { video ->
                        navController.navigate(Screen.VideoDetail.createRoute(
                            videoId = video.id ?: 0,
                            videoName = video.name ?: "",
                            typeId = video.typeId ?: 0
                        ))
                    }
                )
            } else {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Settings.route) {
                        popUpTo(Screen.Search.route) { inclusive = true }
                    }
                }
            }
        }
    }
}
