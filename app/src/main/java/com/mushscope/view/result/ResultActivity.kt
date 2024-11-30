package com.mushscope.view.result

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mushscope.R
import com.mushscope.data.local.entity.HistoryEntity
import com.mushscope.databinding.ActivityResultBinding
import com.mushscope.utils.ImageClassifierHelper
import com.mushscope.utils.ViewModelFactory
import com.mushscope.utils.uriToFile
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.text.NumberFormat

class ResultActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResultBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    private val viewModel: ResultViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

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
                    showToast(error)
                }

                override fun onResults(results: List<Classifications>, inferenceTime: Long) {
                    Log.d("ImageClassification", "Inference Time: $inferenceTime ms")

                    if (results.isNotEmpty() && results[0].categories.isNotEmpty()) {
                        // Log all categories with their scores
                        results[0].categories.forEachIndexed { index, category ->
                            Log.d("ImageClassification", "Category $index: Label = ${category.label}, Score = ${category.score}")
                        }

                        val sortedCategories = results[0].categories.sortedByDescending { it.score }
                        val predictedLabel = sortedCategories[0].label
                        val confidenceScore = NumberFormat.getPercentInstance()
                            .format(sortedCategories[0].score)
                            .trim()

                        Log.d("ImageClassification", "Predicted Label: $predictedLabel")
                        Log.d("ImageClassification", "Confidence Score: $confidenceScore")

                        binding.resultText.text = getString(R.string.after_analyze, predictedLabel, confidenceScore)

                        binding.btnSave.setOnClickListener {
                            saveResultToHistory(uriImage, predictedLabel, confidenceScore)
                        }
                    } else {
                        Log.e("ImageClassification", "No classification results found")
                        showToast("No results found.")
                    }
                }
            }
        )
        imageClassifierHelper.classifyStaticImage(uriImage)
    }

    private fun saveResultToHistory(uriImage: Uri, predictedLabel: String, confidenceScore: String) {
        val history = HistoryEntity(
            result = getString(R.string.result_history, predictedLabel),
            confidenceScore = getString(R.string.conf_score_history, confidenceScore),
            imagePath = uriToFile(uriImage, this).toString()
        )
        viewModel.insertHistory(history)
        showToast("Result is saved to History")
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_IMAGE_URI = "extra_image_uri"
    }
}
