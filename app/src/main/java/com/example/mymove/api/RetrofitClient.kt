package com.example.mymove.api

import android.content.Context
import android.util.Log
import com.example.mymove.api.adapter.VideoApiAdapter
import com.example.mymove.api.adapter.DefaultVideoApiAdapter
import com.example.mymove.data.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

private const val TAG = "RetrofitClient"

object RetrofitClient {
    private lateinit var settingsDataStore: SettingsDataStore
    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String? = null
    private var _videoApi: VideoApi? = null
    private var apiAdapter: VideoApiAdapter = DefaultVideoApiAdapter()

    fun setApiAdapter(adapter: VideoApiAdapter) {
        apiAdapter = adapter
    }

    val videoApiAdapter: VideoApiAdapter
        get() = apiAdapter

    val videoApi: VideoApi
        get() = _videoApi ?: throw IllegalStateException("API base URL is not set. Please configure it in settings.")

    val isApiUrlSet: Boolean
        get() = currentBaseUrl != null

    fun init(context: Context) {
        Log.d(TAG, "Initializing RetrofitClient")
        settingsDataStore = SettingsDataStore(context)
        runBlocking {
            updateBaseUrl()
        }
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        Log.d(TAG, "Creating Retrofit instance with base URL: $baseUrl")
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })

        val sslContext = SSLContext.getInstance("SSL").apply {
            init(null, trustAllCerts, SecureRandom())
        }

        val client = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    suspend fun updateBaseUrl() {
        try {
            val newBaseUrl = settingsDataStore.currentApiEndpoint.first()?.url
            if (newBaseUrl == null) {
                Log.e(TAG, "API base URL is not set")
                _videoApi = null
                currentBaseUrl = null
                return
            }

            if (newBaseUrl != currentBaseUrl) {
                Log.d(TAG, "Updating base URL from $currentBaseUrl to $newBaseUrl")
                currentBaseUrl = newBaseUrl
                retrofit = createRetrofit(newBaseUrl)
                val realApi = retrofit?.create(VideoApi::class.java)
                _videoApi = if (realApi != null) {
                    // 创建代理对象来处理API响应
                    object : VideoApi {
                        override suspend fun getVideoList(
                            action: String,
                            page: Int,
                            typeId: Int?,
                            keyword: String?,
                            hours: Int?,
                            apiType: String
                        ): ApiResponse {
                            return apiAdapter.adaptResponse(
                                realApi.getVideoList(action, page, typeId, keyword, hours, apiType)
                            )
                        }

                        override suspend fun getVideoDetail(
                            action: String,
                            ids: String,
                            apiType: String
                        ): ApiResponse {
                            return apiAdapter.adaptResponse(
                                realApi.getVideoDetail(action, ids, apiType)
                            )
                        }

                        override suspend fun getCategories(
                            action: String,
                            apiType: String
                        ): ApiResponse {
                            return apiAdapter.adaptResponse(
                                realApi.getCategories(action, apiType)
                            )
                        }
                    }
                } else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating base URL", e)
            throw e
        }
    }
}
