package com.dicoding.storyapp.view.signup

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.data.api.RetrofitInstance
import com.dicoding.storyapp.databinding.ActivitySignupBinding
import com.dicoding.storyapp.data.auth.AuthRepository
import com.dicoding.storyapp.ui.auth.AuthViewModel
import com.dicoding.storyapp.ui.auth.AuthViewModelFactory
import com.dicoding.storyapp.di.dataStore

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi AuthRepository dengan ApiService
        val authRepository = AuthRepository(RetrofitInstance.apiService) // Pastikan RetrofitInstance sudah ada
        val factory = AuthViewModelFactory(authRepository, dataStore)
        authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        initViewModel()
        observeViewModel()
        setupAction()
        setupEmailValidation()
    }

    private fun initViewModel() {
    }

    private fun observeViewModel() {
        authViewModel.signupResponse.observe(this) { response ->
            if (response != null) {
                if (!response.error) {
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Registrasi gagal: ${response.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Terjadi kesalahan, silakan coba lagi.", Toast.LENGTH_SHORT).show()
            }
        }

        authViewModel.isLoading.observe(this) { isLoading ->
            binding.signupButton.isEnabled = !isLoading
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun setupAction() {
        binding.signupButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (isInputValid(name, email, password)) {
                authViewModel.signup(name, email, password)
            }
        }
    }

    private fun isInputValid(name: String, email: String, password: String): Boolean {
        var isValid = true

        if (name.isEmpty()) {
            binding.nameEditTextLayout.error = "Nama tidak boleh kosong"
            isValid = false
        } else {
            binding.nameEditTextLayout.error = null
        }

        if (email.isEmpty()) {
            binding.emailEditTextLayout.error = "Email tidak boleh kosong"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditTextLayout.error = "Format email tidak valid"
            isValid = false
        } else {
            binding.emailEditTextLayout.error = null
        }

        if (password.isEmpty()) {
            binding.passwordEditTextLayout.error = "Password tidak boleh kosong"
            isValid = false
        } else if (password.length < 8) {
            binding.passwordEditTextLayout.error = "Password harus minimal 8 karakter"
            isValid = false
        } else {
            binding.passwordEditTextLayout.error = null
        }

        return isValid
    }

    private fun setupEmailValidation() {
        binding.emailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = s.toString().trim()
                if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.emailEditTextLayout.error = "Format email tidak valid"
                    binding.emailEditTextLayout.setErrorTextColor(ColorStateList.valueOf(Color.RED))
                    binding.emailEditTextLayout.isErrorEnabled = true
                } else {
                    binding.emailEditTextLayout.error = null
                    binding.emailEditTextLayout.isErrorEnabled = false
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
