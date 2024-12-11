package com.mushscope.view.analyzer

import android.app.Activity.RESULT_OK
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mushscope.R
import com.mushscope.databinding.FragmentAnalyzerBinding
import com.mushscope.utils.getImageUri
import com.mushscope.view.animation.animateButton
import com.yalantis.ucrop.UCrop
import java.io.File

class AnalyzerFragment : Fragment() {

    private var _binding: FragmentAnalyzerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AnalyzerViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnalyzerBinding.inflate(inflater, container, false)
        val toolbar: Toolbar = binding.root.findViewById(R.id.toolbar)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.title_analyzer)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.currentImgUri.observe(viewLifecycleOwner) { uri ->
            uri?.let {
                showImage(it)
            }
        }

        viewModel.isModelDownloading.observe(viewLifecycleOwner) { isDownloading ->
            binding.progressIndicator.visibility = if (isDownloading) View.VISIBLE else View.GONE
        }

        binding.btnGallery.setOnClickListener {
            animateButton(binding.btnGallery)
            startGallery()
        }
        binding.btnCamera.setOnClickListener {
            animateButton(binding.btnCamera)
            startCamera()
        }
        binding.btnAnalyze.setOnClickListener {
            animateButton(binding.btnAnalyze)
            viewModel.currentImgUri.value?.let { uri ->
                viewModel.prepareModelAndMoveToResult(uri)
            } ?: showToast("Please select an image first.")
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            launchUcrop(uri)
        } else {
            Log.d("Photo Picker", "No media selected")
            showToast(getString(R.string.no_image_selected))
        }
    }

    private val launcherUCrop = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let {
                val croppedUri = UCrop.getOutput(it)
                croppedUri?.let { uri ->
                    viewModel.setCurrentImage(uri)
                }
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            UCrop.getError(result.data!!)?.let { error ->
                showToast(error.message.toString())
            }
        }
    }

    private fun startCamera() {
        val uri = getImageUri(requireContext())
        Log.d("Camera URI", "URI created: $uri")
        viewModel.setCurrentImage(uri)
        launcherIntentCamera.launch(uri)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            viewModel.currentImgUri.value?.let { uri ->
                launchUcrop(uri)
            }
        } else {
            viewModel.setCurrentImage(null)
        }
    }

    private fun launchUcrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File.createTempFile("cropped_", ".jpg", requireActivity().cacheDir))
        val uCropIntent = UCrop.of(uri, destinationUri)
            .getIntent(requireActivity())
        launcherUCrop.launch(uCropIntent)
    }

    private fun showImage(uri: Uri) {
        Log.d("Image URI", "showImage: $uri")
        binding.imgPreview.setImageURI(uri)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}