package com.dicoding.storyapp.view.ListStory.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.storyapp.R

class LoadingStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<LoadingStateAdapter.LoadStateViewHolder>() {

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loading_state, parent, false)
        return LoadStateViewHolder(view, retry)
    }

    class LoadStateViewHolder(itemView: View, private val retry: () -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)
        private val retryButton: Button = itemView.findViewById(R.id.retry_button)

        init {
            retryButton.setOnClickListener {
                retry() // Memanggil fungsi retry() saat tombol diklik
            }
        }

        fun bind(loadState: LoadState) {
            // Menangani error state
            if (loadState is LoadState.Error) {
                retryButton.visibility = View.VISIBLE
            } else {
                retryButton.visibility = View.GONE
            }

            // Menangani loading state
            progressBar.visibility = if (loadState is LoadState.Loading) View.VISIBLE else View.GONE
        }
    }
}
