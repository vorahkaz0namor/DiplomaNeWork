package ru.sign.conditional.diplomanework.adapter

import androidx.recyclerview.widget.DiffUtil
import ru.sign.conditional.diplomanework.dto.Event
import ru.sign.conditional.diplomanework.dto.FeedItem
import ru.sign.conditional.diplomanework.dto.Payload
import ru.sign.conditional.diplomanework.dto.Post

class FeedItemCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        if (oldItem::class == newItem::class)
            oldItem.id == newItem.id
        else
            false

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        oldItem == newItem

    override fun getChangePayload(oldItem: FeedItem, newItem: FeedItem): Any =
        when (newItem) {
            is Post -> {
                oldItem as Post
                Payload(
                    id = newItem.id,
                    likeOwnerIds = newItem.likeOwnerIds.takeIf { it != oldItem.likeOwnerIds },
                    likedByMe = newItem.likedByMe.takeIf { it != oldItem.likedByMe }
                )
            }
            else -> {
                newItem as Event
                oldItem as Event
                Payload(
                    id = newItem.id,
                    participantsIds = newItem.participantsIds.takeIf { it != oldItem.participantsIds },
                    participatedByMe = newItem.participatedByMe.takeIf { it != oldItem.participatedByMe }
                )
            }
        }
}