package com.example.madetector.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.delay

@Composable
fun DetectionScreen(
    mADetectorViewModel: MADetectorViewModel,
    uiState: MADetectorUiState,
    imageUiState: ImageUiState,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
            modifier = Modifier.weight(5f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val context = LocalContext.current

            Spacer(modifier = Modifier.weight(1f))
            DisplayImage(
                selectedImageUri = imageUiState.selectedImageUri!!,
                uiState = uiState,
                context = context,
                modifier = Modifier.weight(4f),
                displayResult = imageUiState.isResultShown
            )
            if (!imageUiState.isResultShown) {
                Text(text = "Retina Image", modifier = Modifier.weight(1f))
            } else {
                Text(text = "Result", modifier = Modifier.weight(1f))
            }
        }

        if (imageUiState.isThresholdInputShown) {
            ThresholdInput(imageUiState = imageUiState, mADetectorViewModel = mADetectorViewModel)

        } else if (uiState is MADetectorUiState.Complete) {

            if (!imageUiState.isResultShown) {
                Spacer(
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxSize()
                )
                Text(text = "Detection Complete")
                Button(
                    mADetectorViewModel::onShowResultClick
                ) {
                    Text(text = "Show Result")
                }
            }

        } else if (uiState is MADetectorUiState.Loading) {
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            )
            Text(text = "Detection in progress...")
            KeepScreenOn()
        } else {
            Spacer(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            )
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            horizontalArrangement = Arrangement.Center
        ) {
            Buttons(
                uiState = uiState,
                onSelectImageClick = {
                    launcher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                    mADetectorViewModel.setIsThresholdInputShown(true)
                    mADetectorViewModel.setIsResultShown(false)
                },
                onCancelClick = mADetectorViewModel::cancelWork
            )
        }
    }
}

@Composable
fun Buttons(
    uiState: MADetectorUiState,
    onSelectImageClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    when (uiState) {
        is MADetectorUiState.Complete -> {
            Button(
                onSelectImageClick,
            ) { Text(text = "Select Image", modifier = Modifier) }
        }
        is MADetectorUiState.Default -> {
            Button(
                onSelectImageClick,
            ) { Text(text = "Select Image", modifier = Modifier) }
        }
        is MADetectorUiState.Loading -> {
            FilledTonalButton(onCancelClick) { Text("Cancel") }
            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun ThresholdInput(imageUiState: ImageUiState, mADetectorViewModel: MADetectorViewModel) {
    OutlinedTextField(
        value = imageUiState.threshold,
        onValueChange = {
            mADetectorViewModel.setThreshold(it)
            mADetectorViewModel.validateThreshold(it.toDoubleOrNull() ?: 0.0)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        label = { Text("Threshold") },
        modifier = Modifier,
        enabled = true,
        singleLine = true
    )
    FilledTonalButton(
        onClick = mADetectorViewModel::detectMA,
        enabled = imageUiState.isThresholdValid
    ) { Text(text = "Detect", modifier = Modifier)}
}

@Composable
fun DisplayImage(selectedImageUri: Uri, uiState: MADetectorUiState, context: Context, modifier: Modifier, displayResult: Boolean) {
    if (!displayResult) {
        SelectedImage(imageUri = selectedImageUri, modifier = modifier)
    } else if(uiState is MADetectorUiState.Complete) {
        ResultGifFromImageUris(image1Uri = selectedImageUri, image2Uri = uiState.resultImageUri, context = context, modifier = modifier)
    }
}

@Composable
fun SelectedImage(imageUri: Uri, modifier: Modifier) {
    Image(
        painter = rememberAsyncImagePainter(model = imageUri),
        contentDescription = "Selected Image",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .fillMaxSize()
    )
}

@Composable
fun ResultGifFromImageUris(image1Uri: Uri, image2Uri: String, context: Context, modifier: Modifier) {
    val image1Painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(image1Uri)
            .build()
    )
    val image2Painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(image2Uri)
            .build()
    )

    GifAnimation(image1Painter, image2Painter, modifier)
}

@Composable
fun GifAnimation(image1Painter: Painter, image2Painter: Painter, modifier: Modifier) {
    var currentImageIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(1000) // Adjust delay as needed
            currentImageIndex = (currentImageIndex + 1) % 2
        }
    }


    Image(
        painter = if (currentImageIndex == 0) image1Painter else image2Painter,
        contentDescription = "Result Image",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .fillMaxWidth()
    )
}

@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}