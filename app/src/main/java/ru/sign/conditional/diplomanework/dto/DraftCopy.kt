package ru.sign.conditional.diplomanework.dto

data class DraftCopy(
    val postId: Int = 0,
    val content: String? = null
) {
    override fun toString(): String =
        "postId = $postId\ndraftContent = $content"
}