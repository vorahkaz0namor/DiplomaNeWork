package ru.sign.conditional.diplomanework.dto

data class Event(
    override val id: Int,
    val idFromServer: Int = 0,
    val authorId: Int = 0,
    val author: String,
    val authorAvatar: String? = null,
    val authorJob: String? = null,
    val content: String,
    val datetime: String,
    val published: String,
    val type: EventType = EventType.ONLINE,
    val likeOwnerIds: List<Int> = emptyList(),
    val likedByMe: Boolean = false,
    val speakerIds: List<Int> = emptyList(),
    val participantsIds: List<Int> = emptyList(),
    val participatedByMe: Boolean = false,
    override val attachment: Attachment? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false,
    val users: Map<String, UserPreview> = emptyMap()
) : FeedItem {
    override fun toString(): String =
                "id = $id, " +
                "idFromServer = $idFromServer, " +
                "authorId = $authorId, " +
                "author = $author\n" +
                "authorAvatar = $authorAvatar\n" +
                "authorJob = $authorJob, " +
                "content = $content\n" +
                "datetime = $datetime\n" +
                "published = $published, " +
                "type = ${type.name}\n" +
                "likes count = ${likeOwnerIds.size}, " +
                "likedByMe = ${likedByMe.toString().uppercase()}, " +
                "speakers count = ${speakerIds.size}\n" +
                "participants = $participantsIds, " +
                "participatedByMe = ${participatedByMe.toString().uppercase()}\n" +
                "attachment url = ${attachment?.url}\n" +
                "link = $link, " +
                "ownedByMe = ${ownedByMe.toString().uppercase()}, " +
                "users count = ${users.size}"
}