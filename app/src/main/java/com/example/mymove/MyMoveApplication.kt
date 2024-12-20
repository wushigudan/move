package com.example.mymove

import android.app.Application
import com.example.mymove.api.RetrofitClient
import com.example.mymove.api.adapter.CustomVideoApiAdapter
import timber.log.Timber
import com.example.mymove.BuildConfig

class MyMoveApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 初始化 Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // 初始化 RetrofitClient 并设置自定义适配器
        RetrofitClient.init(this)
        RetrofitClient.setApiAdapter(CustomVideoApiAdapter())
        Timber.d("已设置自定义视频 API 适配器")
    }
}
