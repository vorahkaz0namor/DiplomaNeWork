package ru.sign.conditional.diplomanework.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.databinding.CardEventBinding
import ru.sign.conditional.diplomanework.databinding.CardPostBinding
import ru.sign.conditional.diplomanework.dto.Event
import ru.sign.conditional.diplomanework.dto.FeedItem
import ru.sign.conditional.diplomanework.dto.Post

class FeedItemAdapter(
    private val onInteractionListener: OnInteractionListener
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(FeedItemCallback()) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Post -> R.layout.card_post
            is Event -> R.layout.card_event
            null -> error("Unknown item type")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Post -> (holder as? PostViewHolder)?.bind(item)
            is Event -> (holder as? EventViewHolder)?.bind(item)
            null -> getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        fun listenerWontCast(): Nothing =
            error("Unknown interactor type: ${onInteractionListener::class.javaObjectType}")
        return when (viewType) {
            R.layout.card_post -> {
                PostViewHolder(
                    CardPostBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    (onInteractionListener as? OnPostInteractionListener?)
                        ?: listenerWontCast()
                )
            }
            R.layout.card_event -> {
                EventViewHolder(
                    CardEventBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    ),
                    (onInteractionListener as? OnEventInteractionListener?)
                        ?: listenerWontCast()
                )
            }
            else -> error("Unknown view type: $viewType")
        }
    }
}