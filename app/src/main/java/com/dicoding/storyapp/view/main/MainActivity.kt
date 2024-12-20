package com.dicoding.storyapp.view.main

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivityMainBinding
import com.dicoding.storyapp.view.ViewModelFactory
import com.dicoding.storyapp.view.welcome.WelcomeActivity
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.di.dataStore
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding

    // Menginisialisasi UserPreference untuk mengakses session data
    private val userPreference: UserPreference by lazy {
        UserPreference.getInstance(dataStore)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.getSession().observe(this) { user ->
            Log.d("MainActivity", "User session: email=${user.email}, token=${user.token}")

            if (user.token.isNullOrEmpty()) {
                Log.d("MainActivity", "User is not logged in, navigating to WelcomeActivity")
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                Log.d("MainActivity", "User is logged in, navigating to StoryFragment")
            }
        }

        setupView()

        setupAction()
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

    private fun setupAction() {
        binding.logoutButton.setOnClickListener {
            Log.d("MainActivity", "Logging out...")

            lifecycleScope.launch {
                userPreference.logout()
            }

            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
    }

}
