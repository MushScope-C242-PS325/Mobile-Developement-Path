package com.mushscope.view.analyzer

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mushscope.utils.ImageClassifierHelper
import com.mushscope.view.result.ResultActivity
import org.tensorflow.lite.task.vision.classifier.Classifications

class AnalyzerViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentImgUri = MutableLiveData<Uri?>()
    val currentImgUri: LiveData<Uri?> = _currentImgUri

    private val _isModelDownloading = MutableLiveData<Boolean>()
    val isModelDownloading: LiveData<Boolean> = _isModelDownloading

    private lateinit var imageUri: Uri
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    fun setCurrentImage(uri: Uri?) {
        _currentImgUri.value = uri
    }

    fun prepareModelAndMoveToResult(uri: Uri) {
        imageUri = uri
        _isModelDownloading.value = true

        imageClassifierHelper = ImageClassifierHelper(
            context = getApplication(),
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    _isModelDownloading.value = false
                }

                override fun onResults(results: List<Classifications>, inferenceTime: Long) {
                    _isModelDownloading.value = false
                    moveToResultActivity()
                }
            },
            onDownloadSuccess = {
                _isModelDownloading.value = false
                moveToResultActivity()
            },
            onError = {
                _isModelDownloading.value = false
            }
        )
    }

    private fun moveToResultActivity() {
        val intent = Intent(getApplication(), ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_IMAGE_URI, imageUri.toString())
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        getApplication<Application>().startActivity(intent)
    }
}