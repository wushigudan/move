package com.example.mymove.ui.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.mymove.api.RetrofitClient
import com.example.mymove.api.VideoCategory
import com.example.mymove.data.SettingsDataStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onCategorySelected: (categoryId: Int, categoryName: String) -> Unit,
    onSearchClick: () -> Unit
) {
    val context = LocalContext.current
    val settingsDataStore = remember { SettingsDataStore(context) }
    val apiBaseUrl by settingsDataStore.apiBaseUrl.collectAsState(initial = null)
    
    var categories by remember { mutableStateOf<List<VideoCategory>?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // 监听 API 源变化
    LaunchedEffect(apiBaseUrl) {
        try {
            isLoading = true
            val response = RetrofitClient.videoApi.getCategories()
            categories = response.categories
            error = null
        } catch (e: Exception) {
            error = e.message
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分类") },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "搜索"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                error != null -> Text("加载失败: $error")
                else -> CategoryGrid(
                    categories = categories ?: emptyList(),
                    onCategorySelected = onCategorySelected
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryGrid(
    categories: List<VideoCategory>,
    onCategorySelected: (categoryId: Int, categoryName: String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 获取所有父分类
        val parentCategories = categories.filter { it.parentId == 0 }
        
        parentCategories.forEach { parentCategory ->
            // 为每个父分类添加标题
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = parentCategory.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // 添加该父分类下的所有子分类
            val childCategories = categories.filter { it.parentId == parentCategory.id }
            items(childCategories) { category ->
                ElevatedCard(
                    onClick = { onCategorySelected(category.id, category.name) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
