package com.mushscope.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import com.mushscope.R
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import java.io.FileInputStream
import java.nio.channels.FileChannel

class ImageClassifierHelper(
    private var threshold: Float = 0.4f,
    private var maxResult: Int = 3,
    private val modelName: String = "cancer.tflite",
    val context: Context,
    val classifierListener: ClassifierListener
) {
    private var imageClassifier: ImageClassifier? = null

    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setMaxResults(maxResult)
            .setScoreThreshold(threshold)
        val baseOptions = BaseOptions.builder().setNumThreads(4).build()
        optionsBuilder.setBaseOptions(baseOptions)

        try {
            val assetFileDescriptor = context.assets.openFd(modelName)
            FileInputStream(assetFileDescriptor.fileDescriptor).use { fileInputStream ->
                imageClassifier = ImageClassifier.createFromBufferAndOptions(
                    fileInputStream.channel.map(
                        FileChannel.MapMode.READ_ONLY,
                        assetFileDescriptor.startOffset,
                        assetFileDescriptor.declaredLength
                    ),
                    optionsBuilder.build()
                )
            }
        } catch (e: Exception) {
            Log.e("ImageClassifierHelper", "Error setting up image classifier: ${e.message}", e)
            classifierListener.onError(context.getString(R.string.image_classifier_failed))
        }
    }

    fun classifyStaticImage(imageUri: Uri) {
        if (imageClassifier == null) setupImageClassifier()

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(CastOp(DataType.FLOAT32))
            .add(NormalizeOp(127.5f, 127.5f)) // MobileNetV3 specific normalization
            .build()

        try {
            // Konversi Uri menjadi Bitmap
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri))
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }

            // Pastikan bitmap dalam format yang sesuai
            val processedBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
            Log.d("ImageClassifierHelper", "Processing image: $imageUri")

            val tensorImage = imageProcessor.process(TensorImage.fromBitmap(processedBitmap))
            val inferenceStart = SystemClock.uptimeMillis()
            val results = imageClassifier?.classify(tensorImage)
            val inferenceTime = SystemClock.uptimeMillis() - inferenceStart

            if (!results.isNullOrEmpty()) {
                Log.d("ImageClassifierHelper", "Inference time: $inferenceTime ms")
                results.forEach { classification ->
                    classification.categories.forEach { category ->
                        Log.d("ImageClassifierHelper", "Label: ${category.label}, Score: ${category.score}")
                    }
                }
                classifierListener.onResults(results, inferenceTime)
            } else {
                Log.d("ImageClassifierHelper", "No classification results found.")
                classifierListener.onError(context.getString(R.string.image_classifier_failed))
            }
        } catch (e: Exception) {
            Log.e("ImageClassifierHelper", "Error classifying image: ${e.message}", e)
            classifierListener.onError(context.getString(R.string.image_classifier_failed))
        }
    }

    interface ClassifierListener {
        fun onError(error: String)
        fun onResults(results: List<Classifications>, inferenceTime: Long)
    }
}
