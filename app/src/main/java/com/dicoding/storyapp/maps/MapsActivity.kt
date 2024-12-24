package com.dicoding.storyapp.maps

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dicoding.storyapp.R
import com.dicoding.storyapp.data.UserRepository
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
            MapsRepository(RetrofitInstance.apiService),
            UserRepository.getInstance(userPreference) // Gunakan getInstance()
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
            mapsViewModel.fetchStoriesWithLocation()
        }

        mapsViewModel.stories.observe(this) { stories ->
            Log.d("MapsActivity", "Received ${stories.size} stories.")
            if (isMapReady) {
                if (stories.isEmpty()) {
                    binding.noStoriesTextView.visibility = View.VISIBLE
                } else {
                    addMarkersToMap(stories)
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        isMapReady = true

        mapsViewModel.stories.value?.let { stories ->
            if (stories.isNotEmpty()) {
                addMarkersToMap(stories)
            }
        }
    }

    private fun addMarkersToMap(stories: List<StoryLocation>) {
        mMap.clear()
        val boundsBuilder = LatLngBounds.Builder()
        var hasValidLocation = false

        stories.forEach { story ->
            val lat = story.lat
            val lon = story.lon
            if (lat != null && lon != null) {
                val latLng = LatLng(lat, lon)
                mMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(story.story.name)
                        .snippet(story.story.description)
                )
                boundsBuilder.include(latLng)
                hasValidLocation = true
            }
        }

        if (hasValidLocation) {
            val bounds = boundsBuilder.build()
            val padding = 200
            if (stories.size == 1) {
                val singleLatLng = LatLng(stories[0].lat!!, stories[0].lon!!)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(singleLatLng, 15f))
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
            }
        } else {
            binding.noStoriesTextView.visibility = View.VISIBLE
        }
    }
}
