package ru.sign.conditional.diplomanework.dto

data class DraftCopy(
    val postId: Int = 0,
    val eventId: Int = 0,
    val postContent: String? = null,
    val eventContent: String? = null
)