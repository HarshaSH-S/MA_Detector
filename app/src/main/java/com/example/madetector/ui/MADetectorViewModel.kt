package com.example.madetector.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.work.WorkInfo
import com.example.madetector.KEY_IMAGE_URI
import com.example.madetector.MADetectorApplication
import com.example.madetector.data.MADetectorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MADetectorViewModel(private val mADetectorRepository: MADetectorRepository) : ViewModel() {
    private val _imageUiState = MutableStateFlow(ImageUiState())
    val imageUiState: StateFlow<ImageUiState> = _imageUiState.asStateFlow()

    val mADetectorUiState: StateFlow<MADetectorUiState> = mADetectorRepository.outputWorkInfo
        .map {info ->
            val resultImageUri = info.outputData.getString(KEY_IMAGE_URI)
            val selectedImageUri = _imageUiState.value.selectedImageUri.toString()
            when {
                info.state.isFinished && !resultImageUri.isNullOrEmpty() -> {
                    MADetectorUiState.Complete(selectedImageUri = selectedImageUri, resultImageUri = resultImageUri)
                }
                info.state == WorkInfo.State.CANCELLED -> {
                    MADetectorUiState.Default
                }
                else -> {
                    setIsResultShown(false)
                    setIsThresholdInputShown(false)
                    MADetectorUiState.Loading
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            MADetectorUiState.Default
        )

    fun setSelectedImageUri(uri: Uri) {
        _imageUiState.value = _imageUiState.value.copy(selectedImageUri = uri)
    }

    fun setThreshold(threshold: String) {
        _imageUiState.value = _imageUiState.value.copy(threshold = threshold)
    }

    fun setIsThresholdInputShown(isShown: Boolean) {
        _imageUiState.value = _imageUiState.value.copy(isThresholdInputShown = isShown)
    }

    fun validateThreshold(threshold: Double) {
        _imageUiState.value = _imageUiState.value.copy(isThresholdValid = (0.0 < threshold) && (threshold < 1.0) )
    }

    fun setIsResultShown(isShown: Boolean) {
        _imageUiState.value = _imageUiState.value.copy(isResultShown = isShown)
    }

    fun onShowResultClick() {
        setIsResultShown(true)
        setIsThresholdInputShown(false)

    }

    fun detectMA() {
        _imageUiState.value.let {
            mADetectorRepository.detectMA(it.selectedImageUri.toString(), it.threshold.toDouble())
        }
    }

    fun cancelWork() {
        mADetectorRepository.cancelWork()
        setIsThresholdInputShown(true)
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val mADetectorRepository =
                    (this[APPLICATION_KEY] as MADetectorApplication).container.mADetectorRepository
                MADetectorViewModel(
                    mADetectorRepository = mADetectorRepository
                )
            }
        }
    }
}

data class ImageUiState(
    val selectedImageUri: Uri? = null,
    val threshold: String = "0.6",
    val resultImageUri: Uri? = null,
    val isThresholdInputShown: Boolean = true,
    val isResultShown: Boolean = false,
    val isThresholdValid: Boolean = true,
)

sealed interface MADetectorUiState {
    object Default : MADetectorUiState
    object Loading : MADetectorUiState
    data class Complete(
        val selectedImageUri: String,
        val resultImageUri: String
    ) : MADetectorUiState
}