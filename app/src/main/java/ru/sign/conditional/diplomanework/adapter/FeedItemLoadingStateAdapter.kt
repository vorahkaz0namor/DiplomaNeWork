package ru.sign.conditional.diplomanework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import ru.sign.conditional.diplomanework.databinding.ItemLoadingBinding

class FeedItemLoadingStateAdapter(
    private val retryListener: RetryListener
) : LoadStateAdapter<FeedItemLoadingStateViewHolder>() {
    override fun onBindViewHolder(
        holder: FeedItemLoadingStateViewHolder,
        loadState: LoadState
    ) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): FeedItemLoadingStateViewHolder =
        FeedItemLoadingStateViewHolder(
            ItemLoadingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            retryListener
        )
}