package ru.sign.conditional.diplomanework.repository

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import ru.sign.conditional.diplomanework.api.UserApiService
import ru.sign.conditional.diplomanework.auth.AppAuth
import ru.sign.conditional.diplomanework.dao.AuxDao
import ru.sign.conditional.diplomanework.dto.UserPreview
import ru.sign.conditional.diplomanework.entity.UserPreviewEntity
import ru.sign.conditional.diplomanework.model.MediaModel
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val userApiService: UserApiService,
    private val appAuth: AppAuth,
    private val auxDao: AuxDao
) : AuthRepository, UserRepository {
    companion object {
        private val MEDIA_TYPE = "text/plain".toMediaType()
    }

    private suspend fun saveOwnerPreview(ownerId: Int) {
        val response = userApiService.getUserById(ownerId)
        if (response.isSuccessful) {
            val ownerPreview = response.body() ?: throw HttpException(response)
            auxDao.saveUserPreview(
                UserPreviewEntity.fromDto(
                    mapOf(ownerId.toString() to ownerPreview)
                )
            )
        } else
            throw HttpException(response)
    }

    override suspend fun getOwnerPreview(): UserPreview =
        auxDao.getUserPreview(
            getOwnerId().toString()
        )
            .toDto()

    override suspend fun getOwnerId(): Int =
        appAuth.data.value?.id ?: 0

    override suspend fun login(login: String, password: String) {
        val response = userApiService.login(login, password)
        if (response.isSuccessful) {
            val authModel = response.body() ?: throw HttpException(response)
            appAuth.setAuth(authModel)
            saveOwnerPreview(authModel.id)
        } else
            throw HttpException(response)
    }

    override suspend fun register(
        login: String,
        password: String,
        name: String,
        media: MediaModel?
    ) {
        val response = userApiService.let {
            val mediaAsRequest =
                if (media != null)
                    MultipartBody.Part.createFormData(
                        "file",
                        media.file.name,
                        media.file.asRequestBody()
                    )
                else
                    null
            it.register(
                login.toRequestBody(MEDIA_TYPE),
                password.toRequestBody(MEDIA_TYPE),
                name.toRequestBody(MEDIA_TYPE),
                mediaAsRequest
            )
        }
        if (response.isSuccessful) {
            val authModel = response.body() ?: throw HttpException(response)
            appAuth.setAuth(authModel)
            saveOwnerPreview(authModel.id)
        } else
            throw HttpException(response)
    }

    override suspend fun logout() {
        appAuth.removeAuth()
    }
}