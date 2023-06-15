package ru.sign.conditional.diplomanework.repository

import ru.sign.conditional.diplomanework.dto.UserPreview

interface UserRepository {
    suspend fun getOwnerId(): Int
    suspend fun getOwnerPreview(): UserPreview
}