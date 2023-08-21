package ru.sign.conditional.diplomanework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.sign.conditional.diplomanework.dto.DraftCopy

@Entity
data class DraftCopyEntity(
    @PrimaryKey
    val postId: Int,
    val eventId: Int,
    val postContent: String,
    val eventContent: String
) {
    fun toDto() = DraftCopy(
        postId = postId,
        eventId = eventId,
        postContent = postContent,
        eventContent = eventContent
    )

    companion object {
        fun fromDto(draftCopy: DraftCopy) =
            DraftCopyEntity(
                postId = draftCopy.postId,
                eventId = draftCopy.eventId,
                postContent = draftCopy.postContent ?: "",
                eventContent = draftCopy.eventContent ?: ""
            )
    }
}
