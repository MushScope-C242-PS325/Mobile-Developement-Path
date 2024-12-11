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

        // Setup navigation
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)
    }
}