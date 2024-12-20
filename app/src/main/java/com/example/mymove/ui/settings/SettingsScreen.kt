package com.example.mymove.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mymove.api.RetrofitClient
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import com.example.mymove.data.ApiEndpoint
import com.example.mymove.data.SettingsDataStore
import kotlinx.coroutines.launch
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onApiUpdated: () -> Unit
) {
    var apiName by remember { mutableStateOf("") }
    var apiUrl by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsDataStore = remember { SettingsDataStore(context) }
    val endpoints by settingsDataStore.apiEndpoints.collectAsState(initial = emptyList())
    val currentEndpoint by settingsDataStore.currentApiEndpoint.collectAsState(initial = null)
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "线路设置",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加线路"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // 当前选中的线路
                currentEndpoint?.let { endpoint ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "当前线路",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = endpoint.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = endpoint.url,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // 所有已保存的线路
                Text(
                    text = "所有线路",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn {
                    itemsIndexed(endpoints) { index, endpoint ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = {
                                scope.launch {
                                    try {
                                        settingsDataStore.switchApiEndpoint(index)
                                        RetrofitClient.updateBaseUrl()
                                        onApiUpdated()
                                        snackbarHostState.showSnackbar("已切换到: ${endpoint.name}")
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("切换失败: ${e.message}")
                                    }
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = endpoint.name,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = endpoint.url,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                // 删除按钮
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            settingsDataStore.removeApiEndpoint(index)
                                            snackbarHostState.showSnackbar("已删除线路: ${endpoint.name}")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }

                if (endpoints.isEmpty()) {
                    Text(
                        text = "暂无保存的线路，请点击右上角添加",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            // 添加新线路对话框
            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = { 
                        showAddDialog = false
                        apiName = ""
                        apiUrl = ""
                    },
                    title = { Text("添加新线路") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = apiName,
                                onValueChange = { apiName = it },
                                label = { Text("线路名称") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                            OutlinedTextField(
                                value = apiUrl,
                                onValueChange = { apiUrl = it },
                                label = { Text("API地址") },
                                modifier = Modifier.fillMaxWidth(),
                                supportingText = { 
                                    Text("请输入完整的API地址，包含 /api.php/provide/vod/") 
                                }
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "推荐线路：",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "免责声明：所有线路均为来自互联网与本程序无关，请勿乱用",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Column(
                                modifier = Modifier
                                    .padding(start = 8.dp, top = 4.dp)
                                    .height(200.dp)  // 固定高度
                                    .verticalScroll(rememberScrollState())  // 添加滚动
                            ) {
                                val recommendedUrls = listOf(
                                    Pair("线路1", "https://xxx.com/api.php/provide/vod/"),
                                    Pair("线路2", "https://xxx.com.com/api.php/provide/vod/"),
                                    Pair("线路3", "https://bfzyapi.com/api.php/provide/vod/"),
                                    Pair("线路4", "https://xxx.com.com/api.php/provide/vod/"),
                                    Pair("线路5", "https://xxx.com.com/api.php/provide/vod/")
                                )
                                
                                recommendedUrls.forEachIndexed { index, (description, url) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                apiUrl = url
                                                scope.launch {
                                                    snackbarHostState.showSnackbar("已填入${description}")
                                                }
                                            }
                                            .padding(vertical = 8.dp),  // 增加垂直间距
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = description,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = url,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "复制",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "提示：添加后可以在设置页面随时切换线路by:白衬衫！",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (apiName.isNotBlank() && apiUrl.isNotBlank()) {
                                    scope.launch {
                                        try {
                                            settingsDataStore.addApiEndpoint(apiName, apiUrl)
                                            RetrofitClient.updateBaseUrl()
                                            onApiUpdated()
                                            showAddDialog = false
                                            apiName = ""
                                            apiUrl = ""
                                            snackbarHostState.showSnackbar("已添加新线路: $apiName")
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("添加失败: ${e.message}")
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
                                showAddDialog = false
                                apiName = ""
                                apiUrl = ""
                            }
                        ) {
                            Text("取消")
                        }
                    }
                )
            }
        }
    }
}
