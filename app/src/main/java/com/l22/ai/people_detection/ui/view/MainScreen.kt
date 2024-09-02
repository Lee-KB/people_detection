package com.l22.ai.people_detection.ui.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.camera.view.PreviewView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.l22.ai.people_detection.viewmodel.CameraViewModel
import com.l22.ai.people_detection.viewmodel.DetectionViewModel
import com.l22.ai.people_detection.util.PermissionsUtils.RequestCameraPermission
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.l22.ai.people_detection.util.DetectionUtils

@Composable
fun MainScreen(
    cameraViewModel: CameraViewModel = viewModel(),
    detectionViewModel: DetectionViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasPermission by remember { mutableStateOf(false) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            RequestCameraPermission(
                onPermissionGranted = {
                    hasPermission = true
                    cameraViewModel.setupCamera(context, lifecycleOwner)
                    detectionViewModel.initializeModel(context)
                }
            )
            if (hasPermission) {
                CameraPreviewWithOverlay(cameraViewModel, detectionViewModel)
            }
        }
    }
}

@Composable
fun CameraPreviewWithOverlay(
    cameraViewModel: CameraViewModel,
    detectionViewModel: DetectionViewModel
) {
    val preview = cameraViewModel.preview.value
    val bitmapState = cameraViewModel.bitmapState.value

    Box(modifier = Modifier.fillMaxSize()) {
        var previewView: PreviewView? = null

        AndroidView(
            factory = { context ->
                previewView = PreviewView(context).apply {
                    preview?.setSurfaceProvider(surfaceProvider)
                }
                previewView!!
            },
            modifier = Modifier.fillMaxSize()
        )

        bitmapState?.let { bitmap ->
            // Step 1: Preprocess the image to get a ByteBuffer
            val inputBuffer = detectionViewModel.preprocessImage(bitmap)

            // Step 2: Run inference on the ByteBuffer
            val outputData = detectionViewModel.runInference(inputBuffer)

            // Step 3: Post-process the output data
            val detectionResults = detectionViewModel.postprocessOutput(outputData, bitmap.width, bitmap.height)

            val rotationDegrees = previewView?.display?.rotation ?: 0

            // Draw detection results on Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                DetectionUtils.drawBoundingBoxes(
                    detectionResults = detectionResults,
                    drawScope = this,
                    canvasWidth = size.width,
                    canvasHeight = size.height,
                    bitmapWidth = bitmap.width,
                    bitmapHeight = bitmap.height,
                    rotationDegrees = rotationDegrees
                )
            }
        }
    }
}



