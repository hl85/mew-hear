package com.mewhear.app

import android.app.Application

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContext.context = applicationContext
    }
}
