package ru.sign.conditional.diplomanework.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.sign.conditional.diplomanework.dto.Attachment
import ru.sign.conditional.diplomanework.dto.Event
import ru.sign.conditional.diplomanework.dto.EventType

@Entity
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val idFromServer: Int,
    val authorId: Int,
    val author: String,
    val authorAvatar: String?,
    val authorJob: String?,
    val content: String,
    val datetime: String,
    val published: String,
    val eventType: EventType,
    val likeOwnerIds: List<Int> = emptyList(),
    val likedByMe: Boolean,
    val speakerIds: List<Int> = emptyList(),
    val participantsIds: List<Int> = emptyList(),
    val participatedByMe: Boolean,
    @Embedded
    val attachment: Attachment?,
    val link: String?,
    val ownedByMe: Boolean,
    val usersIds: List<String> = emptyList()
) {
    fun toDto() = Event(
        id = id,
        idFromServer = idFromServer,
        authorId = authorId,
        author = author,
        authorAvatar = authorAvatar,
        authorJob = authorJob,
        content = content,
        datetime = datetime,
        published = published,
        type = eventType,
        likeOwnerIds = likeOwnerIds,
        likedByMe = likedByMe,
        speakerIds = speakerIds,
        participantsIds = participantsIds,
        participatedByMe = participatedByMe,
        attachment = attachment,
        link = link,
        ownedByMe = ownedByMe
    )

    companion object {
        fun fromDto(dtoEvent: Event) =
            EventEntity(
                id = dtoEvent.id,
                idFromServer = dtoEvent.idFromServer,
                authorId = dtoEvent.authorId,
                author = dtoEvent.author,
                authorAvatar = dtoEvent.authorAvatar,
                authorJob = dtoEvent.authorJob,
                content = dtoEvent.content,
                datetime = dtoEvent.datetime,
                published = dtoEvent.published,
                eventType = dtoEvent.type,
                likeOwnerIds = dtoEvent.likeOwnerIds,
                likedByMe = dtoEvent.likedByMe,
                speakerIds = dtoEvent.speakerIds,
                participantsIds = dtoEvent.participantsIds,
                participatedByMe = dtoEvent.participatedByMe,
                attachment = dtoEvent.attachment,
                link = dtoEvent.link,
                ownedByMe = dtoEvent.ownedByMe,
                usersIds = dtoEvent.users.keys.toList()
            )
    }
}
