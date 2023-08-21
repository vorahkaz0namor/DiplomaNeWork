package ru.sign.conditional.diplomanework.dto

data class Post(
    override val id: Int,
    val idFromServer: Int = 0,
    val authorId: Int = 0,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String,
    val published: String,
    val link: String? = null,
    val likeOwnerIds: List<Int> = emptyList(),
    val mentionIds: List<Int> = emptyList(),
    val likedByMe: Boolean = false,
    override val attachment: Attachment? = null,
    val ownedByMe: Boolean = false,
    val users: Map<String, UserPreview> = emptyMap()
) : FeedItem