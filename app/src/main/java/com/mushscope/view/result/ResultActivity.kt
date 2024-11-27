package com.mushscope.view.result

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mushscope.R
import com.mushscope.databinding.ActivityResultBinding
import com.mushscope.utils.ImageClassifierHelper
import java.text.NumberFormat

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUri = intent.getStringExtra(EXTRA_IMAGE_URI)?.let { Uri.parse(it) }

        imageUri?.also {
            Log.d("Image URI", "showImage: $it")
            binding.resultImage.setImageURI(it)
            analyzeImage(it)
        } ?: showToast("Image URI is null.")
    }

    private fun analyzeImage(uriImage: Uri) {
        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    runOnUiThread {
                        binding.resultText.text = error
                        binding.btnSave.isEnabled = false
                    }
                }

                override fun onResults(classification: String, probability: Float, inferenceTime: Long) {
                    runOnUiThread {
                        val confidenceScore = NumberFormat.getPercentInstance()
                            .format(probability)
                            .trim()

                        binding.resultText.text = getString(
                            R.string.classification_result,  // Pastikan string resource ini ada
                            classification,  // Kategori yang diprediksi
                            confidenceScore  // Skor
                        )
                        binding.btnSave.isEnabled = true
                    }
                }
            }
        )

        // Panggil metode untuk klasifikasi gambar
        imageClassifierHelper.classifyStaticImage(uriImage)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
    }
}
