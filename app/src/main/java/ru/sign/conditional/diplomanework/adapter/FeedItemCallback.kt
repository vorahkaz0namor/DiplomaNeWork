package ru.sign.conditional.diplomanework.adapter

import androidx.recyclerview.widget.DiffUtil
import ru.sign.conditional.diplomanework.dto.FeedItem

class FeedItemCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        if (oldItem::class == newItem::class)
            oldItem.id == newItem.id
        else
            false

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean =
        oldItem == newItem
}