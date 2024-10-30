package com.example.madetector

import android.app.Application
import com.example.madetector.data.AppContainer
import com.example.madetector.data.DefaultAppContainer

class MADetectorApplication : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }

}