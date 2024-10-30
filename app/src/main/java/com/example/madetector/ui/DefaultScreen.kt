package com.example.madetector.ui

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun DefaultScreen(
    mADetectorViewModel: MADetectorViewModel,
    modifier: Modifier = Modifier
) {

    // image selection launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                mADetectorViewModel.setSelectedImageUri(uri)
                Log.d("PhotoPicker", "Selected URI: $uri")
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(
            modifier = Modifier
                .weight(4.5f)
                .fillMaxSize()
        )
        Text(
            text = "No image selected",
            fontSize = 24.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .weight(1f)
                .fillMaxSize(),
            textAlign = TextAlign.Center
        )
        Spacer(
            modifier = Modifier
                .weight(2.5f)
                .fillMaxSize()
        )
        Button(
            onClick = {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
        ) {
            Text(
                text = "Select Image",
                modifier = Modifier,
            )
        }
        Spacer(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxSize()
        )
    }
}