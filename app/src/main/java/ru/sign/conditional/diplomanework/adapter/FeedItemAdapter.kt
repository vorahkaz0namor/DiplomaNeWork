package ru.sign.conditional.diplomanework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.databinding.CardEventBinding
import ru.sign.conditional.diplomanework.databinding.CardPostBinding
import ru.sign.conditional.diplomanework.dto.Event
import ru.sign.conditional.diplomanework.dto.FeedItem
import ru.sign.conditional.diplomanework.dto.Payload
import ru.sign.conditional.diplomanework.dto.Post

class FeedItemAdapter(
    private val onInteractionListener: OnInteractionListener
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(FeedItemCallback()) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Post -> R.layout.card_post
            is Event -> R.layout.card_event
            is Payload -> super.getItemViewType(position)
            null -> error("Unknown item type")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Post -> (holder as? PostViewHolder)?.bind(item)
            is Event -> (holder as? EventViewHolder)?.bind(item)
            is Payload -> Unit
            null -> getItemViewType(position)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()) {
            payloads.map {
                (it as? Payload)?.let { payload ->
                    (holder as? PostViewHolder)?.let { postViewHolder ->
                        val updatedPost = (getItem(position) as? Post)?.let { post ->
                            post.copy(
                                likeOwnerIds = payload.likeOwnerIds ?: post.likeOwnerIds,
                                likedByMe = payload.likedByMe ?: post.likedByMe
                            )
                        }
                        postViewHolder.bind(payload, updatedPost)
                    }
                        ?: (holder as? EventViewHolder)?.let { eventViewHolder ->
                            val updatedEvent = (getItem(position) as? Event)?.let { event ->
                                event.copy(
                                    participantsIds = payload.participantsIds ?: event.participantsIds,
                                    participatedByMe = payload.participatedByMe ?: event.participatedByMe
                                )
                            }
                            eventViewHolder.bind(payload, updatedEvent)
                        }
                }
            }
        } else
            onBindViewHolder(
                holder = holder,
                position = position
            )
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