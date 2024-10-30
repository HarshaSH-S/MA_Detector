package com.example.madetector.workers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.madetector.KEY_THRESHOLD
import com.example.madetector.SEGMENT_RESULT_URI
import com.example.madetector.SELECTED_IMAGE_URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private const val TAG = "MADetectorWorker"

class MADetectorWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        makeStatusNotification(
            "Detecting MA",
            applicationContext
        )

        val selectedImageUri = inputData.getString(SELECTED_IMAGE_URI)
        val threshold = inputData.getDouble(KEY_THRESHOLD, 0.6)

        return withContext(Dispatchers.IO) {
            return@withContext try {
                val bitmap = applyClahe(selectedImageUri, threshold, applicationContext)

                val outputUri = writeBitmapToFile("segment_result", applicationContext, bitmap)

                val outputData = workDataOf(SEGMENT_RESULT_URI to outputUri.toString())

                Result.success(outputData)
            } catch (throwable: Throwable) {
                Log.e(
                    TAG,
                    "Error Detecting MAs",
                    throwable
                )
                Result.failure()
            }
        }


    }



    private fun applyClahe(selectedImageUri: String?, threshold: Double, context: Context) : Bitmap {
        require(!selectedImageUri.isNullOrBlank()) {
            val errorMessage = "Invalid input uri"
            Log.e(TAG, errorMessage)
            errorMessage
        }
        //  Create the Bitmap of the selected image
        val resolver = applicationContext.contentResolver

        val bitmap = BitmapFactory.decodeStream(
            resolver.openInputStream(Uri.parse(selectedImageUri))
        )

        // convert the Bitmap to a byteArray to pass it to python code
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }

        // from the apply_clahe module call the function clahe_image with the byteArray as argument
        val python = Python.getInstance()
        val clahe = python.getModule("apply_clahe")
        val bytes = clahe.callAttr("clahe_image", byteArray, 1200, threshold).toJava(ByteArray::class.java)

        // convert the byteArray returned from python back to a Bitmap
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}

