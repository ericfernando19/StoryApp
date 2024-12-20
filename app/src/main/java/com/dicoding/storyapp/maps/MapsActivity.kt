package com.dicoding.storyapp.maps

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.pref.dataStore
import com.dicoding.storyapp.data.api.RetrofitInstance
import com.dicoding.storyapp.data.pref.UserPreference
import com.dicoding.storyapp.data.model.StoryLocation
import com.dicoding.storyapp.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val userPreference by lazy { UserPreference.getInstance(applicationContext.dataStore) }

    private val mapsViewModel: MapsViewModel by viewModels {
        MapsViewModelFactory(
            MapsRepository(RetrofitInstance.apiService)
        )
    }

    private var isMapReady = false // Menandakan apakah peta sudah siap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        lifecycleScope.launch {
            val token = userPreference.getUserToken()
            if (token != null) {
                mapsViewModel.fetchStoriesWithLocation(token)
            } else {
                Log.e("MapsActivity", "Token is null!")
            }
        }

        // Mengamati perubahan data stories setelah peta siap
        mapsViewModel.stories.observe(this) { stories ->
            Log.d("MapsActivity", "Received ${stories.size} stories.")
            if (isMapReady) {
                if (stories.isEmpty()) {
                    Log.e("MapsActivity", "No stories with location returned.")
                    binding.noStoriesTextView.visibility = View.VISIBLE
                } else {
                    Log.d("MapsActivity", "Found ${stories.size} stories with location.")
                    addMarkersToMap(stories)
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        Log.d("MapsActivity", "GoogleMap is ready!")
        mMap.uiSettings.isZoomControlsEnabled = true

        isMapReady = true // Menandakan peta sudah siap

        // Mengamati stories setelah peta siap
        mapsViewModel.stories.value?.let { stories ->
            if (stories.isNotEmpty()) {
                addMarkersToMap(stories)
            }
        }
    }

    private fun addMarkersToMap(stories: List<StoryLocation>) {
        mMap.clear() // Menghapus marker yang sudah ada sebelumnya
        val builder = LatLngBounds.Builder()
        var hasValidPoint = false

        stories.forEach { storyLocation ->
            val lat = storyLocation.lat
            val lon = storyLocation.lon
            if (lat != null && lon != null) {
                val latLng = LatLng(lat, lon)
                mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(storyLocation.story.name)
                        .snippet(storyLocation.story.description) // Menambahkan deskripsi sebagai snippet
                )
                builder.include(latLng)
                hasValidPoint = true
            }
        }

        if (hasValidPoint) {
            val bounds = builder.build()
            val padding = 200

            if (stories.size == 1) {
                // Jika hanya ada 1 marker, gunakan CameraUpdateFactory.newLatLng
                val singleLatLng = LatLng(stories[0].lat!!, stories[0].lon!!)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(singleLatLng, 15f))
            } else {
                // Jika ada lebih dari 1 marker, gunakan bounds
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                mMap.moveCamera(cameraUpdate)
            }
        } else {
            Log.e("MapsActivity", "No valid locations to display on the map.")
            binding.noStoriesTextView.visibility = View.VISIBLE
        }
    }
}



