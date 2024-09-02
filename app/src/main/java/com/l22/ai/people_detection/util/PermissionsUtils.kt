package com.l22.ai.people_detection.util

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

object PermissionsUtils {
    @Composable
    fun RequestCameraPermission(onPermissionGranted: () -> Unit) {
        val context = LocalContext.current

        // Activity result launcher for requesting permissions
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    // If permission is granted, proceed to setup the camera
                    onPermissionGranted()
                } else {
                    // Show a message if permission is denied
                    Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
                }
            }
        )

        // Request permission when the Composable is first launched
        LaunchedEffect(Unit) {
            when (PackageManager.PERMISSION_GRANTED) {
                // Check if the camera permission has already been granted
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) -> {
                    onPermissionGranted()
                }
                else -> {
                    // Request the camera permission
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }
}