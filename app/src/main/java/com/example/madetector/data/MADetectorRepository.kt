package com.example.madetector.data

import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow

interface MADetectorRepository {
    val outputWorkInfo: Flow<WorkInfo>
    fun detectMA(selectedImageUri: String, threshold: Double)
    fun cancelWork()
}