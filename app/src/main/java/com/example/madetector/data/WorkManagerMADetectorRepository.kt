package com.example.madetector.data

import android.content.Context
import android.net.Uri
import androidx.lifecycle.asFlow
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.madetector.KEY_THRESHOLD
import com.example.madetector.MA_DETECTION_WORK_NAME
import com.example.madetector.SELECTED_IMAGE_URI
import com.example.madetector.TAG_OUTPUT
import com.example.madetector.workers.CleanupWorker
import com.example.madetector.workers.MADetectorWorker
import com.example.madetector.workers.SaveImageToFileWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class WorkManagerMADetectorRepository(context: Context) : MADetectorRepository {

    private val workManager = WorkManager.getInstance(context)

    // get work info
    override val outputWorkInfo: Flow<WorkInfo> =
        workManager.getWorkInfosByTagLiveData(TAG_OUTPUT).asFlow().mapNotNull {
            if (it.isNotEmpty()) it.first() else null
        }

    // function to start the segmentation
    override fun detectMA(selectedImageUri: String, threshold: Double) {

        var continuation = workManager.beginUniqueWork(
            MA_DETECTION_WORK_NAME,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequest.Companion.from(CleanupWorker::class.java)
        )

        val detectMABuilder = OneTimeWorkRequestBuilder<MADetectorWorker>()
        detectMABuilder.setInputData(createInputDataForWorkRequest(Uri.parse(selectedImageUri), threshold))

        continuation = continuation.then(detectMABuilder.build())

        val save = OneTimeWorkRequestBuilder<SaveImageToFileWorker>()
            .addTag(TAG_OUTPUT)
            .build()
        continuation = continuation.then(save)

        continuation.enqueue()
    }

    // function to cancel segmentation
    override fun cancelWork() {
        workManager.cancelUniqueWork(MA_DETECTION_WORK_NAME)
    }

    // create input data for segmentation
    private fun createInputDataForWorkRequest(selectedImageUri: Uri, threshold: Double): Data {
        val builder = Data.Builder()
        builder.putString(SELECTED_IMAGE_URI, selectedImageUri.toString()).putDouble(KEY_THRESHOLD, threshold)
        return builder.build()
    }

}
