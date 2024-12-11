package com.mushscope.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import com.google.android.gms.tflite.client.TfLiteInitializationOptions
import com.google.android.gms.tflite.gpu.support.TfLiteGpu
import com.google.android.gms.tflite.java.TfLite
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import com.mushscope.R
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import java.io.IOException

class ImageClassifierHelper(
    private var threshold: Float = 0.4f,
    private var maxResult: Int = 3,
    private val modelName: String = "Mushscope.tflite",
    val context: Context,
    val classifierListener: ClassifierListener,
    private val onDownloadSuccess: () -> Unit,
    private val onError: (String) -> Unit
) {
    private var imageClassifier: ImageClassifier? = null
    private var modelLoadedFromAssets = false
    private var modelLoadedFromFirebase = false

    init {
        TfLiteGpu.isGpuDelegateAvailable(context).onSuccessTask { gpuAvailable ->
            val optionsBuilder = TfLiteInitializationOptions.builder()
            if (gpuAvailable) {
                optionsBuilder.setEnableGpuDelegateSupport(true)
            }
            TfLite.initialize(context, optionsBuilder.build())
        }.addOnSuccessListener {
            loadAppropriateModel()
        }.addOnFailureListener {
            onError(context.getString(R.string.tflite_is_not_initialized_yet))
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    private fun loadAppropriateModel() {
        if (isNetworkAvailable()) {
            downloadFirebaseModel()
        } else {
            loadAssetsModel()
        }
    }

    private fun downloadFirebaseModel() {
        val conditions = CustomModelDownloadConditions.Builder().build()

        FirebaseModelDownloader.getInstance()
            .getModel("Mushscope", DownloadType.LATEST_MODEL, conditions)
            .addOnSuccessListener { model: CustomModel ->
                try {
                    setupImageClassifier(model)
                    modelLoadedFromFirebase = true
                    Log.d("ImageClassifierHelper", "Model successfully downloaded from Firebase")
                    onDownloadSuccess()
                } catch (e: Exception) {
                    Log.e("ImageClassifierHelper", "Failed to setup Firebase model: ${e.message}")
                    loadAssetsModel()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("ImageClassifierHelper", "Firebase model download failed: ${exception.message}")
                loadAssetsModel()
            }
    }

    private fun loadAssetsModel() {
        try {
            val assetManager = context.assets
            val modelInputStream = assetManager.open(modelName)
            modelInputStream.close()

            setupImageClassifier()
            modelLoadedFromAssets = true
            Log.d("ImageClassifierHelper", "Model successfully loaded from assets")
            onDownloadSuccess()
        } catch (e: IOException) {
            Log.e("ImageClassifierHelper", "Model not found in assets: ${e.message}")
            onError(context.getString(R.string.model_load_failed))
        }
    }

    private fun setupImageClassifier(model: CustomModel? = null) {
        val optionsBuilder = ImageClassifier.ImageClassifierOptions.builder()
            .setMaxResults(maxResult)
            .setScoreThreshold(threshold)
        val baseOptions = BaseOptions.builder().setNumThreads(4).build()
        optionsBuilder.setBaseOptions(baseOptions)

        try {
            imageClassifier = if (model != null && model.file != null && model.file!!.exists()) {
                ImageClassifier.createFromFileAndOptions(
                    context,
                    model.file!!.absolutePath,
                    optionsBuilder.build()
                )
            } else {
                ImageClassifier.createFromFileAndOptions(
                    context,
                    modelName,
                    optionsBuilder.build()
                )
            }
        } catch (e: Exception) {
            Log.e("ImageClassifierHelper", "Model setup failed: ${e.message}")
            onError(context.getString(R.string.image_classifier_failed))
            imageClassifier = null
        }
    }

    fun classifyStaticImage(imageUri: Uri) {
        if (imageClassifier == null) setupImageClassifier()

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(CastOp(DataType.FLOAT32))
            .build()

        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri))
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }

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
