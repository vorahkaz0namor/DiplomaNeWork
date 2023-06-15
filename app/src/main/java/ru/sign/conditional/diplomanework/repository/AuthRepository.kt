package ru.sign.conditional.diplomanework.repository

import ru.sign.conditional.diplomanework.model.MediaModel

interface AuthRepository {
    suspend fun login(login: String, password: String)
    suspend fun register(login: String, password: String, name: String, media: MediaModel?)
    suspend fun logout()
}