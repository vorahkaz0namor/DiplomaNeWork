package ru.sign.conditional.diplomanework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.sign.conditional.diplomanework.dto.DraftCopy

@Entity
data class DraftCopyEntity(
    @PrimaryKey
    val postId: Int,
    val content: String
) {
    fun toDto() = DraftCopy(
        postId = postId,
        content = content
    )

    companion object {
        fun fromDto(draftCopy: DraftCopy) =
            DraftCopyEntity(
                postId = draftCopy.postId,
                content = draftCopy.content ?: ""
            )
    }
}
