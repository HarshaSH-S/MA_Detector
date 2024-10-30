package com.example.madetector

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.madetector.ui.MADetectorScreen
import com.example.madetector.ui.theme.MADetectorTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        setContent {
            MADetectorTheme {
                MADetectorScreen()
            }
        }
    }
}

