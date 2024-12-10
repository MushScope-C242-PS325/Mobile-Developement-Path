package com.mushscope.view.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.ScaleAnimation
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.mushscope.R
import com.mushscope.data.pref.ThemePreference
import com.mushscope.data.pref.themeDataStore
import com.mushscope.databinding.ActivityMainBinding
import com.mushscope.utils.ViewModelFactory
import com.mushscope.view.welcome.WelcomeActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    private var lastSelectedItem: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set theme based on preferences
        runBlocking {
            val isDarkModeActive = ThemePreference.getInstance(this@MainActivity.themeDataStore)
                .getThemeSetting()
                .first()
            AppCompatDelegate.setDefaultNightMode(
                if (isDarkModeActive) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        super.onCreate(savedInstanceState)

        // Inflate binding and set content view
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
                return@observe
            }
        }

        // Observe theme settings for changes
        viewModel.getThemeSettings().observe(this) { isDarkMode ->
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Setup navigation with animation
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        navView.setOnItemSelectedListener { menuItem ->
            // Reset the previous selected item's animation
            lastSelectedItem?.let { resetBottomNavItem(it) }

            // Animate the currently selected item
            val clickedView: View = navView.findViewById(menuItem.itemId)
            animateBottomNavItem(clickedView)
            lastSelectedItem = clickedView

            // Navigate to the selected destination
            navController.navigate(menuItem.itemId)
            true
        }
    }

    private fun animateBottomNavItem(view: View) {
        val scaleAnimation = ScaleAnimation(
            1f, 1.2f,  // Skala horizontal: 100% ke 120%
            1f, 1.2f,  // Skala vertikal: 100% ke 120%
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f, // Titik pusat horizontal
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f  // Titik pusat vertikal
        ).apply {
            duration = 150 // Durasi animasi
            fillAfter = true // Tetap pada posisi akhir setelah animasi
        }
        view.startAnimation(scaleAnimation)
    }

    private fun resetBottomNavItem(view: View) {
        val scaleAnimation = ScaleAnimation(
            1.2f, 1f,  // Skala horizontal: 120% kembali ke 100%
            1.2f, 1f,  // Skala vertikal: 120% kembali ke 100%
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f, // Titik pusat horizontal
            ScaleAnimation.RELATIVE_TO_SELF, 0.5f  // Titik pusat vertikal
        ).apply {
            duration = 150 // Durasi animasi
            fillAfter = true // Tetap pada posisi akhir setelah animasi
        }
        view.startAnimation(scaleAnimation)
    }
}