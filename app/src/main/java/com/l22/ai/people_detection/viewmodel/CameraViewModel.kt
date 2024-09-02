package com.l22.ai.people_detection.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.l22.ai.people_detection.util.CameraUtils
import com.l22.ai.people_detection.util.CameraUtils.imageProxyToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {
    private val cameraProvider = mutableStateOf<ProcessCameraProvider?>(null)
    val preview = mutableStateOf<Preview?>(null)
    val bitmapState = mutableStateOf<Bitmap?>(null) // State to hold the latest Bitmap

    fun setupCamera(context: Context, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context).get()
                cameraProviderFuture.unbindAll()

                val previewUseCase = Preview.Builder().build()

                // Set up ImageAnalysis for real-time image processing
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalyzer.setAnalyzer(Dispatchers.Default.asExecutor()) { imageProxy ->
                    val bitmap = imageProxyToBitmap(imageProxy)
                    bitmap?.let {
                        bitmapState.value = it
                    }
                    imageProxy.close()
                }

                cameraProviderFuture.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    previewUseCase,
                    imageAnalyzer
                )

                previewUseCase.setSurfaceProvider(PreviewView(context).surfaceProvider)
                preview.value = previewUseCase
                cameraProvider.value = cameraProviderFuture

                Log.d("CameraViewModel", "Camera setup complete")

            } catch (e: Exception) {
                Log.e("CameraViewModel", "Error initializing camera", e)
            }
        }
    }
}
