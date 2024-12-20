package com.dicoding.storyapp.view.ListStory

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.di.dataStore
import com.dicoding.storyapp.maps.MapsActivity
import com.dicoding.storyapp.view.ListStory.Adapter.LoadingStateAdapter
import com.dicoding.storyapp.view.ListStory.ViewModel.StoryViewModel
import com.dicoding.storyapp.view.ViewModelFactory
import com.dicoding.storyapp.view.add_story.AddStoryActivity
import com.dicoding.storyapp.view.welcome.WelcomeActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class StoryActivity : AppCompatActivity() {

    private val storyAdapter = StoryAdapter()
    private val storyViewModel: StoryViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    private var userId: String? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        // Setup SharedPreferences
        sharedPreferences = getSharedPreferences("user_pref", MODE_PRIVATE)

        userId = intent.getStringExtra("USER_ID") ?: sharedPreferences.getString("user_id", null)

        if (userId.isNullOrEmpty()) {
            Log.e("StoryActivity", "USER_ID is null or empty.")
            Toast.makeText(this, "Gagal mendapatkan USER_ID. Silakan login kembali.", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        Log.d("StoryActivity", "Using USER_ID: $userId")
        setupUI()
        observePagedStories()
    }

    private fun setupUI() {
        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dicoding Story"

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.ac_stories)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = storyAdapter.withLoadStateFooter(
            footer = LoadingStateAdapter { storyAdapter.retry() }
        )

        storyAdapter.addLoadStateListener { loadState ->
            if (loadState.refresh is LoadState.Loading) {
                Toast.makeText(this, "Memuat cerita...", Toast.LENGTH_SHORT).show()
            } else if (loadState.refresh is LoadState.Error) {
                val errorMessage = (loadState.refresh as LoadState.Error).error.localizedMessage
                Toast.makeText(this, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }

        // Setup Floating Action Button
        val addStoryButton = findViewById<FloatingActionButton>(R.id.fab_add)
        addStoryButton.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            intent.putExtra("USER_ID", userId)
            startActivity(intent)
        }
    }

    private fun observePagedStories() {
        lifecycleScope.launch {
            storyViewModel.getPagedStories().collectLatest { pagingData ->
                storyAdapter.submitData(pagingData)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_story, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                handleLogout()
                true
            }
            R.id.action_open_maps -> {
                openMapsActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun openMapsActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        startActivity(intent)
    }

    private fun handleLogout() {
        lifecycleScope.launch {
            val userPreference = UserPreference.getInstance(dataStore)

            // Menghapus sesi dari DataStore
            userPreference.logout()

            // Menghapus sesi dari SharedPreferences
            sharedPreferences.edit().clear().apply()

            // Navigasi ke WelcomeActivity setelah logout
            val intent = Intent(this@StoryActivity, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

            Log.d("StoryActivity", "User logged out successfully.")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
