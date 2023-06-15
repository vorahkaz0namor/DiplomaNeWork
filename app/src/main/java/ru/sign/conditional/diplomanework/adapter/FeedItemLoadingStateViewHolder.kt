package ru.sign.conditional.diplomanework.adapter

import android.util.Log
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import ru.sign.conditional.diplomanework.databinding.ItemLoadingBinding

typealias RetryListener = () -> Unit

class FeedItemLoadingStateViewHolder(
    private val binding: ItemLoadingBinding,
    private val retryListener: RetryListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(loadState: LoadState) {
        Log.d("INCOMING LOADSTATE", loadState.toString())
        binding.apply {
            progressLoading.isVisible = loadState is LoadState.Loading
            retryLoading.isVisible = loadState is LoadState.Error
            retryLoading.setOnClickListener { retryListener() }
        }
    }
}