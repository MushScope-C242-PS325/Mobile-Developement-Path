package com.mushscope.view.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.textfield.TextInputLayout
import com.mushscope.R
import com.mushscope.databinding.ActivitySignupBinding
import com.mushscope.utils.ViewModelFactory
import com.mushscope.data.source.Result
import com.mushscope.utils.uriToFile
import com.mushscope.utils.reduceFileImage
import com.yalantis.ucrop.UCrop
import java.io.File

class SignupActivity : AppCompatActivity() {
    private val viewModel by viewModels<AuthViewModel> {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var binding: ActivitySignupBinding
    private var currentImageFile: File? = null
    private var imageUri: Uri? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = it
            launchUcrop(it)
        }
    }

    private val launcherUCrop = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.let {
                val croppedUri = UCrop.getOutput(it)
                croppedUri?.let { uri ->
                    imageUri = uri
                    binding.imgSignup.setImageURI(uri)
                    currentImageFile = uriToFile(uri, this).reduceFileImage()
                }
            }
        } else if (result.resultCode == UCrop.RESULT_ERROR) {
            UCrop.getError(result.data!!)?.let { error ->
                showError("Crop Error", error.message.toString())
            }
        }
    }

    private fun launchUcrop(uri: Uri) {
        val destinationUri = Uri.fromFile(File.createTempFile("cropped_", ".jpg", cacheDir))
        val uCropIntent = UCrop.of(uri, destinationUri)
            .getIntent(this)
        launcherUCrop.launch(uCropIntent)
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupAction()
        setupImageSelection()
        playAnimation()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupImageSelection() {
        binding.imgSignup.setOnClickListener {
            openGallery()
        }
    }

    private fun setupAction() {
        binding.signupButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmEditText.text.toString()

            resetErrorState()

            when {
                name.isEmpty() -> {
                    showFieldError(binding.nameEditTextLayout, getString(R.string.name_cannot_empty))
                    showError(getString(R.string.unvalid_email), getString(R.string.name_cannot_empty))
                }
                !isEmailValid(email) -> {
                    showFieldError(binding.emailEditTextLayout, getString(R.string.unvalid_email_format))
                    showError(getString(R.string.unvalid_email), getString(R.string.reminder_valid_email))
                }
                !isPasswordValid(password) -> {
                    showFieldError(binding.passwordEditTextLayout, getString(R.string.password_min_8_char))
                    showError(getString(R.string.unvalid_password), getString(R.string.reminder_valid_password))
                }
                !isConfirmPasswordValid(password, confirmPassword) -> {
                    showFieldError(binding.confirmEditTextLayout, getString(R.string.password_not_match))
                    showError(getString(R.string.unvalid_password), getString(R.string.password_not_match))
                }
                currentImageFile == null -> {
                    showError(getString(R.string.image_required), getString(R.string.please_select_image))
                }
                else -> {
                    showLoading(true)
                    val photoUrl = currentImageFile?.path ?: ""
                    viewModel.register(name, email, password, photoUrl).observe(this, Observer { result ->
                        when (result) {
                            is Result.Success -> {
                                showLoading(false)
                                AlertDialog.Builder(this).apply {
                                    setTitle("Yeah!")
                                    setMessage(getString(R.string.account_cretaed_message, email))
                                    setPositiveButton(getString(R.string.enter)) { _, _ ->
                                        finish()
                                    }
                                    create()
                                    show()
                                }
                            }
                            is Result.Error -> {
                                showLoading(false)
                                showError(getString(R.string.registration_failed), result.error)
                            }
                            is Result.Loading -> {
                                showLoading(true)
                            }
                            else -> {
                                showLoading(false)
                                showError("Unknown Error", "An unexpected error occurred")
                            }
                        }
                    })
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.signupButton.isEnabled = !isLoading
    }

    private fun showFieldError(layout: TextInputLayout, error: String) {
        layout.error = error
    }

    private fun resetErrorState() {
        listOf(
            binding.nameEditTextLayout,
            binding.emailEditTextLayout,
            binding.passwordEditTextLayout
        ).forEach { field ->
            field.error = null
        }
    }

    private fun showError(title: String, message: String) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 8 && !password.contains(" ")
    }

    private fun isConfirmPasswordValid(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    private fun playAnimation() {
        ObjectAnimator.ofFloat(binding.imgSignup, View.TRANSLATION_X, -30f, 30f).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.tvTitleSignup, View.ALPHA, 1f).setDuration(100)
        val nameTextView = ObjectAnimator.ofFloat(binding.tvNameSignup, View.ALPHA, 1f).setDuration(100)
        val nameEditTextLayout = ObjectAnimator.ofFloat(binding.nameEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val emailTextView = ObjectAnimator.ofFloat(binding.tvEmailSignup, View.ALPHA, 1f).setDuration(100)
        val emailEditTextLayout = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val passwordTextView = ObjectAnimator.ofFloat(binding.tvPasswordSignup, View.ALPHA, 1f).setDuration(100)
        val passwordEditTextLayout = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val confirmTextView = ObjectAnimator.ofFloat(binding.tvConfirm, View.ALPHA, 1f).setDuration(100)
        val confirmEditTextLayout = ObjectAnimator.ofFloat(binding.confirmEditTextLayout, View.ALPHA, 1f).setDuration(100)
        val signup = ObjectAnimator.ofFloat(binding.signupButton, View.ALPHA, 1f).setDuration(100)

        AnimatorSet().apply {
            playSequentially(
                title,
                nameTextView,
                nameEditTextLayout,
                emailTextView,
                emailEditTextLayout,
                passwordTextView,
                passwordEditTextLayout,
                confirmTextView,
                confirmEditTextLayout,
                signup
            )
            startDelay = 100
        }.start()
    }
}