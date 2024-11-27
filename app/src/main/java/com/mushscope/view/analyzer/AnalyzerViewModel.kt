package com.mushscope.view.analyzer

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AnalyzerViewModel : ViewModel() {

    private val _currentImgUri = MutableLiveData<Uri?>()
    val currentImgUri: LiveData<Uri?> get() = _currentImgUri

    fun setCurrentImage(uri: Uri?) {
        _currentImgUri.value = uri
    }
}