package ru.sign.conditional.diplomanework.adapter

import android.util.Log
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
                if (!post.authorAvatar.isNullOrBlank())
                    loadImage(post.authorAvatar)
                else
                    loadImage(R.drawable.localuser)
            }
            author.text = post.author
            published.text = timeCustomRepresentation(post.published)
            menu.isVisible = post.ownedByMe
            content.text = post.content
            postAttachment.apply {
                when {
                    post.attachment == null ||
                    post.attachment.type == AttachmentType.AUDIO ->
                        isVisible = false
                    else -> {
                        isVisible = true
                        loadImage(
                            url = post.attachment.url,
                            type = post.attachment.type.name
                        )
                    }
                }
            }
            likes.isChecked = post.likedByMe
            likes.text = likesCount(post.likeOwnerIds.size)
        }
    }

    private fun setupListeners(post: Post) {
        binding.apply {
            onInteractionListener.apply {
                root.setOnClickListener {
                    checkAuth()
                    if (authorized)
                        onShowSinglePost(post)
                }
                likes.setOnClickListener {
                    checkAuth()
                    if (authorized)
                        onLike(post)
                    else
                        likes.isChecked = false
                }
                postAttachment.setOnClickListener {
                    checkAuth()
                    if (authorized)
                        onShowAttachment(post)
                }
                menu.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.post_options)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.edit_post -> {
                                    onEdit(post)
                                    true
                                }
                                R.id.remove_post -> {
                                    onRemove(post)
                                    true
                                }
                                else -> false
                            }
                        }
                    }.show()
                }
            }
        }
    }
}