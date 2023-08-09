package ru.sign.conditional.diplomanework.dto

data class Payload(
    override val id: Int,
    val likeOwnerIds: List<Int>? = null,
    val likedByMe: Boolean? = null,
    val participantsIds: List<Int>? = null,
    val participatedByMe: Boolean? = null,
    override val attachment: Attachment? = null
): FeedItem
