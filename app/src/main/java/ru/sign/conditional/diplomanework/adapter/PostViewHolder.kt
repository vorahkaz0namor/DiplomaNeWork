package ru.sign.conditional.diplomanework.adapter

import android.view.View
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ru.sign.conditional.diplomanework.R
import ru.sign.conditional.diplomanework.databinding.CardPostBinding
import ru.sign.conditional.diplomanework.dto.AttachmentType
import ru.sign.conditional.diplomanework.dto.Post
import ru.sign.conditional.diplomanework.util.NeWorkHelper.likesCount
import ru.sign.conditional.diplomanework.util.NeWorkHelper.loadImage
import ru.sign.conditional.diplomanework.util.NeWorkHelper.timeCustomRepresentation

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onInteractionListener: OnInteractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        fillingCardPost(post)
        setupListeners(post)
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
            published.text = timeCustomRepresentation(post.published)
            menu.isVisible = post.ownedByMe
            content.text = post.content
            val attachment = post.attachment
            if (attachment != null) {
                val imageValidation = attachment.type == AttachmentType.IMAGE
                postAttachment.isVisible = imageValidation
                if (imageValidation) {
                    postAttachment.loadImage(
                        url = attachment.url,
                        type = attachment.type.name
                    )
                }
                mediaType.isVisible = !imageValidation
                if (attachment.type == AttachmentType.VIDEO)
                    mediaType.setImageResource(R.drawable.ic_video_attachment_48)
                if (attachment.type == AttachmentType.AUDIO)
                    mediaType.setImageResource(R.drawable.ic_audio_attachment_48)
            } else {
                postAttachment.isVisible = false
                mediaType.isVisible = false
            }
            likes.isChecked = post.likedByMe
            likes.text = likesCount(post.likeOwnerIds.size)
            link.text = post.link ?: ""
        }
    }

    private fun setupListeners(post: Post) {
        binding.apply {
            setCustomOnClickListener(root) {
                onInteractionListener.onShowSinglePost(post)
            }
            setCustomOnClickListener(likes) {
                onInteractionListener.onLike(post)
            }
            setCustomOnClickListener(postAttachment, mediaType) {
                onInteractionListener.onShowAttachment(post)
            }
            menu.setOnClickListener { view ->
                PopupMenu(view.context, view).apply {
                    inflate(R.menu.post_options)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.edit_post -> {
                                onInteractionListener.onEdit(post)
                                true
                            }
                            R.id.remove_post -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }
        }
    }

    private fun setCustomOnClickListener(vararg view: View, action: () -> Unit) {
        view.map {
            onInteractionListener.apply {
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