package com.mushscope.view.result

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
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

        binding.progressIndicator.visibility = View.VISIBLE
        binding.resultImage.visibility = View.GONE
        binding.resultText.visibility = View.GONE
        binding.btnSave.visibility = View.GONE

        setSupportActionBar(binding.toolbar)

        val imageUri = intent.getStringExtra(EXTRA_IMAGE_URI)?.let { Uri.parse(it) }

        imageUri?.also {
            Log.d("Image URI", "showImage: $it")
            binding.resultImage.setImageURI(it)
            setupImageClassifier(it)
        } ?: showToast("Image URI is null.")
    }

    private fun setupImageClassifier(uriImage: Uri) {
        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    runOnUiThread {
                        binding.progressIndicator.visibility = View.GONE
                        showToast(error)
                    }
                }

                override fun onResults(results: List<Classifications>, inferenceTime: Long) {
                    runOnUiThread {
                        binding.progressIndicator.visibility = View.GONE
                        binding.resultImage.visibility = View.VISIBLE
                        binding.resultText.visibility = View.VISIBLE
                        binding.btnSave.visibility = View.VISIBLE

                        Log.d("ImageClassification", "Inference Time: $inferenceTime ms")

                        if (results.isNotEmpty() && results[0].categories.isNotEmpty()) {
                            val sortedCategories = results[0].categories.sortedByDescending { it.score }
                            val predictedLabel = sortedCategories[0].label
                            val confidenceScore = NumberFormat.getPercentInstance()
                                .format(sortedCategories[0].score)
                                .trim()

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
            },
            onDownloadSuccess = {
                Log.d("ModelDownload", "Model downloaded successfully")
                runOnUiThread {
                    imageClassifierHelper.classifyStaticImage(uriImage)
                }
            },
            onError = { errorMessage ->
                runOnUiThread {
                    binding.progressIndicator.visibility = View.GONE
                    Log.e("ModelDownload", "Model download failed: $errorMessage")
                }
            }
        )
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