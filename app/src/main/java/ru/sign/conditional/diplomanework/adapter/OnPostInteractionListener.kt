package ru.sign.conditional.diplomanework.adapter

import ru.sign.conditional.diplomanework.dto.Post

interface OnPostInteractionListener : OnInteractionListener {
    fun onLike(post: Post)
    fun onEdit(post: Post)
    fun onRepeatSave(post: Post)
    fun onRemove(post: Post)
    fun onShowAttachment(post: Post)
}