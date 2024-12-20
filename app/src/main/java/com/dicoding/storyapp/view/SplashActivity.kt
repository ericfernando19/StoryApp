package com.dicoding.storyapp.view

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.storyapp.view.ListStory.StoryActivity
import com.dicoding.storyapp.view.welcome.WelcomeActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = getSharedPreferences("user_pref", MODE_PRIVATE)

        val token = sharedPreferences.getString("auth_token", null)

        if (!token.isNullOrEmpty()) {
            val intent = Intent(this, StoryActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, WelcomeActivity::class.java)
            startActivity(intent)
        }

        finish()
    }
}