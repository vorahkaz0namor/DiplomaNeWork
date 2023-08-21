package ru.sign.conditional.diplomanework.adapter

import android.view.View
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
import ru.sign.conditional.diplomanework.util.NeWorkHelper.setFeedItemMenu

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
            setupOnLikeListener(post)
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
                isChecked = post.likedByMe
                text = itemsCount(post.likeOwnerIds.size)
            }
            link.text = post.link ?: ""
        }
    }

    private fun fillingCardPost(payload: Payload) {
        payload.apply {
            likedByMe?.let {
                binding.likes.isChecked = it
            }
            likeOwnerIds?.let {
                binding.likes.text = itemsCount(it.size)
            }
        }
    }

    private fun setupListeners(post: Post) {
        setupOnLikeListener(post)
        binding.apply {
            setCustomOnClickListener(postAttachment, mediaType) {
                onPostInteractionListener.onShowAttachment(post)
            }
            menu.setOnClickListener { view ->
                view.setFeedItemMenu(
                    actionEdit = { onPostInteractionListener.onEdit(post) },
                    actionRemove = { onPostInteractionListener.onRemove(post) }
                )
            }
        }
    }

    private fun setupOnLikeListener(post: Post) {
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