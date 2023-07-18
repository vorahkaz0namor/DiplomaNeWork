package ru.sign.conditional.diplomanework.adapter

import android.util.Log
import android.view.View
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.databinding.CardPostBinding
import ru.sign.conditional.diplomanework.dto.AttachmentType
import ru.sign.conditional.diplomanework.dto.Payload
import ru.sign.conditional.diplomanework.dto.Post
import ru.sign.conditional.diplomanework.util.NeWorkHelper.itemsCount
import ru.sign.conditional.diplomanework.util.NeWorkHelper.loadImage
import ru.sign.conditional.diplomanework.util.NeWorkHelper.publishedCustomRepresentation

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onPostInteractionListener: OnPostInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        fillingCardPost(post)
        setupListeners(post)
    }

    fun bind(payload: Payload, post: Post?) {
        fillingCardPost(payload)
        if (post != null)
            setupListenersGivenPayload(post)
    }

    private fun fillingCardPost(post: Post) {
        binding.apply {
            avatar.apply {
                if (!post.authorAvatar.isNullOrBlank()) {
                    loadImage(post.authorAvatar)
                }
                else {
                    setImageResource(R.drawable.localuser)
                }
            }
            author.text = post.author
            published.text = publishedCustomRepresentation(post.published)
            menu.isVisible = post.ownedByMe
            content.text = post.content
            val attachment = post.attachment
            if (attachment != null) {
                val imageValidation = attachment.type == AttachmentType.IMAGE
                postAttachment.isVisible = imageValidation
                mediaType.isVisible = !imageValidation
                if (imageValidation) {
                    postAttachment.loadImage(
                        url = attachment.url,
                        type = attachment.type.name
                    )
                } else {
                    mediaType.setImageResource(
                        if (attachment.type == AttachmentType.VIDEO)
                            R.drawable.ic_video_attachment_48
                        else
                            R.drawable.ic_audio_attachment_48
                    )
                }
            } else {
                postAttachment.isVisible = false
                mediaType.isVisible = false
            }
            likes.apply {
                isVisible = post.isOnServer
                isChecked = post.likedByMe
                text = itemsCount(post.likeOwnerIds.size)
            }
            repeatSavePost.isVisible = !post.isOnServer
            link.text = post.link ?: ""
        }
    }

    private fun fillingCardPost(payload: Payload) {
        payload.apply {
            isOnServer?.let {
                binding.likes.isVisible = it
                binding.repeatSavePost.isVisible = !it
            }
            likedByMe?.let {
                binding.likes.isChecked = it
            }
            likeOwnerIds?.let {
                binding.likes.text = itemsCount(it.size)
            }
        }
    }

    private fun setupListeners(post: Post) {
        binding.apply {
            setCustomOnClickListener(likes) {
                onPostInteractionListener.onLike(post)
            }
            setCustomOnClickListener(postAttachment, mediaType) {
                onPostInteractionListener.onShowAttachment(post)
            }
            setCustomOnClickListener(repeatSavePost) {
                onPostInteractionListener.onRepeatSave(post)
            }
            menu.setOnClickListener { view ->
                PopupMenu(view.context, view).apply {
                    inflate(R.menu.feed_item_options)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.edit_feed_item -> {
                                onPostInteractionListener.onEdit(post)
                                true
                            }
                            R.id.remove_feed_item -> {
                                onPostInteractionListener.onRemove(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }

    private fun setupListenersGivenPayload(post: Post) {
        binding.apply {
            setCustomOnClickListener(likes) {
                onPostInteractionListener.onLike(post)
            }
        }
    }

    private fun setCustomOnClickListener(vararg view: View, action: () -> Unit) {
        view.map {
            onPostInteractionListener.apply {
                it.setOnClickListener {
                    checkAuth()
                    if (authorized)
                        action()
                    else
                        if (it == binding.likes)
                            binding.likes.isChecked = false
                }
            }
        }
    }
}