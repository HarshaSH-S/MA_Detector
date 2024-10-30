package com.example.madetector.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.madetector.KEY_IMAGE_URI
import com.example.madetector.SEGMENT_RESULT_URI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//Saves the image to a permanent file

private const val TAG = "SaveImageToFileWorker"

class SaveImageToFileWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    private val title = "Segment Result Image"
    private val dateFormatter = SimpleDateFormat(
        "yyyy.MM.dd 'at' HH:mm:ss z",
        Locale.getDefault()
    )

    override suspend fun doWork(): Result {

        makeStatusNotification(
            "Saving image",
            applicationContext
        )

        return withContext(Dispatchers.IO) {

            val resolver = applicationContext.contentResolver
            return@withContext try {
                val resourceUri = inputData.getString(SEGMENT_RESULT_URI)
                val bitmap = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri))
                )
                val imageUrl = MediaStore.Images.Media.insertImage(
                    resolver, bitmap, "$title ", dateFormatter.format(Date())
                )
                if (!imageUrl.isNullOrEmpty()) {
                    val output = workDataOf(KEY_IMAGE_URI to imageUrl)

                    Result.success(output)
                } else {
                    Log.e(
                        TAG,
                        "Writing to MediaStore failed"
                    )
                    Result.failure()
                }
            } catch (exception: Exception) {
                Log.e(
                    TAG,
                    "Error saving image",
                    exception
                )
                Result.failure()
            }
        }
    }
}