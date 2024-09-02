package com.l22.ai.people_detection.model

import android.graphics.RectF

data class DetectionResult(
    val label: String,
    val score: Float,
    val rect: RectF
)
