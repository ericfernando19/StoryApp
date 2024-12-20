package com.dicoding.storyapp.view.ListStory.Detail

import ListStoryItem
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.storyapp.R

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val storyItem: ListStoryItem? = intent.getParcelableExtra("STORY")

        val ivPhoto: ImageView = findViewById(R.id.iv_detail_photo)
        val tvName: TextView = findViewById(R.id.tv_detail_name)
        val tvDescription: TextView = findViewById(R.id.tv_detail_description)

        storyItem?.let {
            tvName.text = it.name
            tvDescription.text = it.description
            Glide.with(this).load(it.photoUrl).into(ivPhoto)
        } ?: run {
            tvName.text = getString(R.string.error_message)
            tvDescription.text = getString(R.string.error_message)
        }
    }
}
