package com.dicoding.storyapp.view.add_story

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dicoding.storyapp.databinding.ActivityAddStoryBinding
import com.dicoding.storyapp.view.ListStory.StoryActivity
import com.dicoding.storyapp.view.ViewModelFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private var currentImageUri: Uri? = null
    private lateinit var addStoryViewModel: AddStoryViewModel
    private var userId: String? = null

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            currentImageUri = null
        }
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
            Toast.makeText(this, "Tidak ada gambar yang dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModelFactory = ViewModelFactory.getInstance(applicationContext)
        addStoryViewModel = ViewModelProvider(this, viewModelFactory).get(AddStoryViewModel::class.java)

        userId = intent.getStringExtra("USER_ID")
        Log.d("AddStoryActivity", "Received USER_ID: $userId")

        if (userId.isNullOrEmpty()) {
            Log.e("AddStoryActivity", "USER_ID is null or empty. Please check the source of the intent.")
            Toast.makeText(this, "User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
        }

        binding.galleryButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startGalleryForAndroid14()
            } else {
                startGallery()
            }
        }

        binding.cameraButton.setOnClickListener {
            if (checkPermissions()) {
                startCamera()
            } else {
                requestPermissions()
            }
        }

        binding.uploadButton.setOnClickListener { uploadImage() }

        addStoryViewModel.uploadStatus.observe(this, Observer { success ->
            if (success) {
                Toast.makeText(this, "Story uploaded successfully", Toast.LENGTH_SHORT).show()
                navigateToListStory()
            } else {
                Toast.makeText(this, "Failed to upload story", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startGalleryForAndroid14() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun startCamera() {
        currentImageUri =getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private fun getImageUriForCamera(): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Image")
            put(MediaStore.Images.Media.DESCRIPTION, "Image from Camera")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/StoryApp")
            }
        }
        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun uploadImage() {
        val description = binding.descriptionEditText.text.toString().trim()

        if (description.isEmpty()) {
            Toast.makeText(this, "Deskripsi tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentImageUri != null && userId != null) {
            try {
                val file = resizeImage(currentImageUri!!)

                if (file != null && file.exists()) {
                    val requestBody: RequestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val body: MultipartBody.Part = MultipartBody.Part.createFormData("photo", file.name, requestBody)

                    addStoryViewModel.uploadStory(description, body, this)
                } else {
                    Toast.makeText(this, "File tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("Upload Image", "Error uploading image: ${e.message}")
                Toast.makeText(this, "Gagal mengunggah gambar", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resizeImage(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true)
            val file = File.createTempFile("resized", ".jpg", cacheDir)
            val outputStream = FileOutputStream(file)
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()
            file
        } catch (e: Exception) {
            Log.e("Resize Image", "Error resizing image: ${e.message}")
            null
        }
    }

    private fun checkPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val storagePermission = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        } else true
        return cameraPermission == PackageManager.PERMISSION_GRANTED && storagePermission
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>().apply {
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 100)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startCamera()
        } else {
            Toast.makeText(this, "Izin diperlukan untuk mengakses kamera", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToListStory() {
        val intent = Intent(this, StoryActivity::class.java)
        intent.putExtra("USER_ID", userId)
        startActivity(intent)
        finish()
    }
}
