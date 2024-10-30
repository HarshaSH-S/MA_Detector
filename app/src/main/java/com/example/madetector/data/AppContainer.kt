package com.example.madetector.data

import android.content.Context

interface AppContainer {
    val mADetectorRepository: MADetectorRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    override val mADetectorRepository = WorkManagerMADetectorRepository(context)
}
