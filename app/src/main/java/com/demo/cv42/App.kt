package com.demo.cv42

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication

/**
 * Created by wangzy on 2020-04-22
 * description:
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        app = this
    }

//    override fun attachBaseContext(base: Context) {
//        MultiDex.install(this)
//    }

    companion object {
        var app: App? = null
        var tag = "cv4"
    }
}