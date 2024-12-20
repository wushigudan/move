package com.example.mymove.data

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private const val TAG = "SettingsDataStore"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class ApiEndpoint(
    val name: String,
    val url: String
)

class SettingsDataStore(private val context: Context) {
    private val gson = Gson()
    private val apiEndpointsKey = stringPreferencesKey("api_endpoints")
    private val currentApiIndexKey = intPreferencesKey("current_api_index")

    // 获取所有API端点
    val apiEndpoints: Flow<List<ApiEndpoint>> = context.dataStore.data
        .catch { exception ->
            Log.e(TAG, "Error reading API endpoints", exception)
            emit(emptyPreferences())
        }
        .map { preferences ->
            val endpointsJson = preferences[apiEndpointsKey] ?: "[]"
            val type = object : TypeToken<List<ApiEndpoint>>() {}.type
            gson.fromJson<List<ApiEndpoint>>(endpointsJson, type)
        }

    // 获取当前选中的API端点
    val currentApiEndpoint: Flow<ApiEndpoint?> = context.dataStore.data
        .catch { exception ->
            Log.e(TAG, "Error reading current API endpoint", exception)
            emit(emptyPreferences())
        }
        .map { preferences ->
            val endpointsJson = preferences[apiEndpointsKey] ?: "[]"
            val currentIndex = preferences[currentApiIndexKey] ?: 0
            val endpoints: List<ApiEndpoint> = gson.fromJson(endpointsJson, object : TypeToken<List<ApiEndpoint>>() {}.type)
            endpoints.getOrNull(currentIndex)
        }

    // 获取当前API的URL
    val apiBaseUrl: Flow<String?> = currentApiEndpoint.map { endpoint ->
        endpoint?.url
    }

    // 添加新的API端点
    suspend fun addApiEndpoint(name: String, url: String) {
        try {
            val normalizedUrl = if (!url.endsWith("/")) "$url/" else url
            context.dataStore.edit { preferences ->
                val endpointsJson = preferences[apiEndpointsKey] ?: "[]"
                val endpoints: MutableList<ApiEndpoint> = gson.fromJson(endpointsJson, object : TypeToken<List<ApiEndpoint>>() {}.type)
                
                // 检查是否已存在相同的URL
                if (endpoints.any { it.url == normalizedUrl }) {
                    throw IllegalArgumentException("该API地址已存在")
                }
                
                endpoints.add(ApiEndpoint(name, normalizedUrl))
                preferences[apiEndpointsKey] = gson.toJson(endpoints)
                
                // 如果是第一个添加的端点，设置为当前选中
                if (endpoints.size == 1) {
                    preferences[currentApiIndexKey] = 0
                }
            }
            Log.d(TAG, "Added new API endpoint: $name - $url")
        } catch (e: Exception) {
            Log.e(TAG, "Error adding API endpoint", e)
            throw e
        }
    }

    // 删除API端点
    suspend fun removeApiEndpoint(index: Int) {
        try {
            context.dataStore.edit { preferences ->
                val endpointsJson = preferences[apiEndpointsKey] ?: "[]"
                val endpoints: MutableList<ApiEndpoint> = gson.fromJson(endpointsJson, object : TypeToken<List<ApiEndpoint>>() {}.type)
                if (index in endpoints.indices) {
                    endpoints.removeAt(index)
                    preferences[apiEndpointsKey] = gson.toJson(endpoints)
                    
                    // 如果删除的是当前选中的端点，重置选中索引
                    val currentIndex = preferences[currentApiIndexKey] ?: 0
                    if (currentIndex >= endpoints.size) {
                        preferences[currentApiIndexKey] = maxOf(0, endpoints.size - 1)
                    }
                }
            }
            Log.d(TAG, "Removed API endpoint at index: $index")
        } catch (e: Exception) {
            Log.e(TAG, "Error removing API endpoint", e)
            throw e
        }
    }

    // 切换当前API端点
    suspend fun switchApiEndpoint(index: Int) {
        try {
            context.dataStore.edit { preferences ->
                val endpointsJson = preferences[apiEndpointsKey] ?: "[]"
                val endpoints: List<ApiEndpoint> = gson.fromJson(endpointsJson, object : TypeToken<List<ApiEndpoint>>() {}.type)
                if (index in endpoints.indices) {
                    preferences[currentApiIndexKey] = index
                    Log.d(TAG, "Switched to API endpoint at index: $index")
                } else {
                    throw IllegalArgumentException("无效的索引")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error switching API endpoint", e)
            throw e
        }
    }

    // 更新API基础URL
    suspend fun updateApiBaseUrl(newUrl: String) {
        try {
            val normalizedUrl = if (!newUrl.endsWith("/")) "$newUrl/" else newUrl
            context.dataStore.edit { preferences ->
                val endpointsJson = preferences[apiEndpointsKey] ?: "[]"
                val endpoints: MutableList<ApiEndpoint> = gson.fromJson(endpointsJson, object : TypeToken<List<ApiEndpoint>>() {}.type)
                val currentIndex = preferences[currentApiIndexKey] ?: 0
                
                if (currentIndex < endpoints.size) {
                    endpoints[currentIndex] = endpoints[currentIndex].copy(url = normalizedUrl)
                    preferences[apiEndpointsKey] = gson.toJson(endpoints)
                    Log.d(TAG, "Updated API base URL to: $normalizedUrl")
                } else {
                    throw IllegalStateException("当前选中的索引无效")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating API base URL", e)
            throw e
        }
    }
}
