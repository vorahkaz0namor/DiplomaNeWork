package ru.sign.conditional.diplomanework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.sign.conditional.diplomanework.dto.Attachment
import ru.sign.conditional.diplomanework.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val idFromServer: Int,
    val authorId: Int,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val published: String,
    val link: String?,
    val likeOwnerIds: List<Int> = emptyList(),
    val mentionIds: List<Int> = emptyList(),
    val likedByMe: Boolean,
    @Embedded
    val attachment: Attachment?,
    val ownedByMe: Boolean,
    val usersIds: List<String> = emptyList()
) {
    fun toDto() = Post(
        id = id,
        idFromServer = idFromServer,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar,
        authorJob = authorJob,
        content = content,
        published = published,
        link = link,
        likeOwnerIds = likeOwnerIds,
        mentionIds = mentionIds,
        likedByMe = likedByMe,
        attachment = attachment,
        ownedByMe = ownedByMe
    )

    companion object {
        fun fromDto(dtoPost: Post) =
            PostEntity(
                id = dtoPost.id,
                idFromServer = dtoPost.idFromServer,
                authorId = dtoPost.authorId,
                author = dtoPost.author,
                authorAvatar = dtoPost.authorAvatar,
                authorJob = dtoPost.authorJob,
                content = dtoPost.content,
                published = dtoPost.published,
                link = dtoPost.link,
                likeOwnerIds = dtoPost.likeOwnerIds,
                mentionIds = dtoPost.mentionIds,
                likedByMe = dtoPost.likedByMe,
                attachment = dtoPost.attachment,
                ownedByMe = dtoPost.ownedByMe,
                usersIds = dtoPost.users.keys.toList()
            )
    }
}
