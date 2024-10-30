package com.example.madetector.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun MADetectorScreen(mADetectorViewModel: MADetectorViewModel = viewModel(factory = MADetectorViewModel.Factory)) {
    val imageUiState by mADetectorViewModel.imageUiState.collectAsState()
    val uiState by mADetectorViewModel.mADetectorUiState.collectAsStateWithLifecycle()
    val layoutDirection = LocalLayoutDirection.current
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(
                start = WindowInsets.safeDrawing
                    .asPaddingValues()
                    .calculateStartPadding(layoutDirection),
                end = WindowInsets.safeDrawing
                    .asPaddingValues()
                    .calculateEndPadding(layoutDirection)
            )
    ) {
        if (imageUiState.selectedImageUri == null) {
            DefaultScreen(
                mADetectorViewModel = mADetectorViewModel,
                modifier = Modifier
                    .fillMaxSize()
            )
        } else {
            DetectionScreen(
                mADetectorViewModel = mADetectorViewModel,
                uiState = uiState,
                imageUiState = imageUiState,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}