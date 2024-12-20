package com.dicoding.storyapp.view.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.data.api.RetrofitInstance
import com.dicoding.storyapp.data.auth.AuthRepository
import com.dicoding.storyapp.databinding.ActivityLoginBinding
import com.dicoding.storyapp.di.dataStore
import com.dicoding.storyapp.ui.auth.AuthViewModel
import com.dicoding.storyapp.ui.auth.AuthViewModelFactory
import com.dicoding.storyapp.view.ListStory.StoryActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var authViewModel: AuthViewModel
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("user_pref", MODE_PRIVATE)

        val token = sharedPreferences.getString("auth_token", null)
        val userId = sharedPreferences.getString("user_id", null)
        if (!token.isNullOrEmpty() && !userId.isNullOrEmpty()) {
            navigateToStoryActivity(userId)
            finish()
            return
        }

        RetrofitInstance.initializeSharedPreferences(sharedPreferences)

        val authRepository = AuthRepository(RetrofitInstance.apiService)
        val factory = AuthViewModelFactory(authRepository, dataStore)
        authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        authViewModel.loginResponse.observe(this, Observer { response ->
            binding.progressBar.visibility = android.view.View.GONE
            if (response != null) {
                if (!response.error) {
                    Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()

                    val token = response.loginResult?.token
                    val userId = response.loginResult?.userId

                    if (!token.isNullOrEmpty() && !userId.isNullOrEmpty()) {
                        saveUserCredentials(token, userId)
                        navigateToStoryActivity(userId)
                        finish()
                    } else {
                        Toast.makeText(this, "Token atau UserId tidak tersedia", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Login gagal: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Terjadi kesalahan. Coba lagi.", Toast.LENGTH_SHORT).show()
            }
        })

        setupAction()
    }

    private fun setupAction() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBar.visibility = android.view.View.VISIBLE
            authViewModel.login(email, password)
        }
    }

    private fun saveUserCredentials(token: String, userId: String) {
        val editor = sharedPreferences.edit()
        editor.putString("auth_token", token)
        editor.putString("user_id", userId)
        editor.apply()
    }

    private fun navigateToStoryActivity(userId: String) {
        val intent = Intent(this, StoryActivity::class.java)
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
    }
}
