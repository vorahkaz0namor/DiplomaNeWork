package ru.sign.conditional.diplomanework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.sign.conditional.diplomanework.dto.UserPreview

@Entity
data class UserPreviewEntity(
    @PrimaryKey
    val userId: String,
    val name: String,
    val avatar: String?
) {
    fun toDto() =
        UserPreview(
            name = name,
            avatar = avatar
        )

    companion object {
        fun fromDto(users: Map<String, UserPreview>) =
            users.map {
                UserPreviewEntity(
                    userId = it.key,
                    name = it.value.name,
                    avatar = it.value.avatar
                )
            }
    }
}
