package com.l22.ai.people_detection.util

import android.annotation.SuppressLint
import android.graphics.RectF
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.l22.ai.people_detection.model.DetectionResult
import kotlin.math.max
import kotlin.math.min

object DetectionUtils {

    @SuppressLint("DefaultLocale")
    fun drawBoundingBoxes(
        detectionResults: List<DetectionResult>,
        drawScope: DrawScope,
        canvasWidth: Float,
        canvasHeight: Float,
        bitmapWidth: Int,
        bitmapHeight: Int,
        rotationDegrees: Int
    ) {
        // Calculate the scale ratios between the canvas and the bitmap dimensions
        val scaleX = canvasWidth / bitmapWidth.toFloat()
        val scaleY = canvasHeight / bitmapHeight.toFloat()

        // Use the smaller scale to maintain the aspect ratio
        val scale = minOf(scaleX, scaleY)

        // Calculate offsets to center the image if it doesn't fill the entire canvas
        val offsetX = (canvasWidth - bitmapWidth * scale) / 2f
        val offsetY = (canvasHeight - bitmapHeight * scale) / 2f

        detectionResults.forEach { result ->
            // Adjust bounding box coordinates based on scaling and offsets after rotation
            var left = result.rect.left * scale + offsetX
            var top = result.rect.top * scale + offsetY
            var right = result.rect.right * scale + offsetX
            var bottom = result.rect.bottom * scale + offsetY

            // Adjust the coordinates based on rotation
            when (rotationDegrees) {
                90 -> {
                    val tmpLeft = left
                    left = top
                    top = canvasHeight - right
                    right = bottom
                    bottom = canvasHeight - tmpLeft
                }
                180 -> {
                    left = canvasWidth - right
                    top = canvasHeight - bottom
                    right = canvasWidth - left
                    bottom = canvasHeight - top
                }
                270 -> {
                    val tmpLeft = left
                    left = canvasWidth - bottom
                    top = tmpLeft
                    right = canvasWidth - top
                    bottom = right
                }
            }

            // Clip the bounding box to fit within the Canvas bounds
            val clippedLeft = left.coerceIn(0f, canvasWidth)
            val clippedTop = top.coerceIn(0f, canvasHeight)
            val clippedRight = right.coerceIn(0f, canvasWidth)
            val clippedBottom = bottom.coerceIn(0f, canvasHeight)

            // Draw the bounding box only if it's visible within the canvas
            if (clippedRight > clippedLeft && clippedBottom > clippedTop) {
                drawScope.drawRect(
                    color = Color.Red,
                    topLeft = Offset(clippedLeft, clippedTop),
                    size = Size(clippedRight - clippedLeft, clippedBottom - clippedTop),
                    style = Stroke(width = 16F)
                )
                drawScope.drawContext.canvas.nativeCanvas.drawText(
                    "${result.label} ${String.format("%.2f", result.score)}",
                    clippedLeft,
                    clippedTop - 10,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 48f
                    }
                )
            }
        }
    }
}
