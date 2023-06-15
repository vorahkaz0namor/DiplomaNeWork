package ru.sign.conditional.diplomanework.adapter

import ru.sign.conditional.diplomanework.dto.Post

interface OnInteractionListener {
    val authorized: Boolean
    fun checkAuth()
    fun onLike(post: Post)
    fun onEdit(post: Post)
    fun onRepeatSave(post: Post)
    fun onRemove(post: Post)
    fun onShowAttachment(post: Post)
    fun onShowSinglePost(post: Post)
}