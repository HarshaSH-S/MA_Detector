package com.example.madetector

// Notification Channel constants

// Name of Notification Channel for verbose notifications of background work
val VERBOSE_NOTIFICATION_CHANNEL_NAME: CharSequence =
    "Verbose WorkManager Notifications"
const val VERBOSE_NOTIFICATION_CHANNEL_DESCRIPTION =
    "Shows notifications whenever work starts"
val NOTIFICATION_TITLE: CharSequence = "WorkRequest Starting"
const val CHANNEL_ID = "VERBOSE_NOTIFICATION"
const val NOTIFICATION_ID = 1

// The name of the image manipulation work
const val MA_DETECTION_WORK_NAME = "ma_detection_work"

// Other keys
const val OUTPUT_PATH = "segment_result_images"
const val SELECTED_IMAGE_URI = "selected_image_uri"
const val SEGMENT_RESULT_URI = "segment_result_uri"
const val KEY_IMAGE_URI = "KEY_IMAGE_URI"
const val TAG_OUTPUT = "OUTPUT"
const val KEY_THRESHOLD = "KEY_THRESHOLD"

const val DELAY_TIME_MILLIS: Long = 3000
