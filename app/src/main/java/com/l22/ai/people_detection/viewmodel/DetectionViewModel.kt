package com.l22.ai.people_detection.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.media.FaceDetector.Face.CONFIDENCE_THRESHOLD
import android.util.Log
import android.view.Surface
import androidx.lifecycle.ViewModel
import com.l22.ai.people_detection.model.DetectionResult
import com.l22.ai.people_detection.util.DetectionUtils
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.Tensor
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DetectionViewModel : ViewModel() {
    private var interpreter: Interpreter? = null
    private lateinit var inputTensor: Tensor
    private lateinit var outputTensor: Tensor
    private val labels = listOf("Person")

    fun initializeModel(context: Context) {
        val modelFile = FileUtil.loadMappedFile(context, "model.tflite")
        interpreter = Interpreter(modelFile)

        // Get input and output tensor information
        inputTensor = interpreter!!.getInputTensor(0)
        outputTensor = interpreter!!.getOutputTensor(0)
    }

    fun preprocessImage(image: Bitmap): ByteBuffer {
        val inputShape = inputTensor.shape() // Get the input tensor shape from the model
        val targetWidth = inputShape[2]
        val targetHeight = inputShape[1]

        // Resize the image and apply padding if necessary
        val resizedImage = Bitmap.createScaledBitmap(image, targetWidth, targetHeight, true)

        val inputBuffer = ByteBuffer.allocateDirect(4 * targetWidth * targetHeight * 3)
        inputBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(targetWidth * targetHeight)
        resizedImage.getPixels(pixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)

        // Normalize pixel values and add them to the input buffer
        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF).toFloat() / 255.0f
            val g = (pixel shr 8 and 0xFF).toFloat() / 255.0f
            val b = (pixel and 0xFF).toFloat() / 255.0f
            inputBuffer.putFloat(r)
            inputBuffer.putFloat(g)
            inputBuffer.putFloat(b)
        }
        return inputBuffer
    }

    fun runInference(inputBuffer: ByteBuffer): TensorBuffer {
        val outputShape = outputTensor.shape()
        val outputBuffer = TensorBuffer.createFixedSize(outputShape, org.tensorflow.lite.DataType.FLOAT32)
        interpreter?.run(inputBuffer, outputBuffer.buffer)
        return outputBuffer
    }

    fun postprocessOutput(output: TensorBuffer, imageWidth: Int, imageHeight: Int): List<DetectionResult> {
        val outputArray = output.floatArray
        val results = mutableListOf<DetectionResult>()
        val numDetections = outputArray.size / 6

        for (i in 0 until numDetections) {
            val offset = i * 6

            // Use center coordinates, width, and height from the model output
            val centerX = outputArray[offset + 1] * imageWidth
            val centerY = outputArray[offset] * imageHeight
            val width = outputArray[offset + 3] * imageWidth
            val height = outputArray[offset + 2] * imageHeight

            // Calculate the top-left and bottom-right corners of the bounding box
            val x1 = centerX - width / 2
            val y1 = centerY - height / 2
            val x2 = centerX + width / 2
            val y2 = centerY + height / 2

            val score = outputArray[offset + 4]
            val labelIndex = outputArray[offset + 5].toInt()

            if (score > CONFIDENCE_THRESHOLD) {
                val boundingBox = RectF(x1, y1, x2, y2) // left, top, right, bottom

                results.add(DetectionResult(
                    label = labels[labelIndex],
                    score = score,
                    rect = boundingBox
                ))
            }
        }

        return results
    }

    override fun onCleared() {
        super.onCleared()
        interpreter?.close()
    }
}
