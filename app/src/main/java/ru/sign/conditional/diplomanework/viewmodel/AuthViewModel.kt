package ru.sign.conditional.diplomanework.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.internal.http.HTTP_CONTINUE
import okhttp3.internal.http.HTTP_OK
import ru.sign.conditional.diplomanework.auth.AppAuth
import ru.sign.conditional.diplomanework.model.AuthModel
import ru.sign.conditional.diplomanework.model.AuthModelState
import ru.sign.conditional.diplomanework.model.MediaModel
import ru.sign.conditional.diplomanework.repository.AuthRepository
import ru.sign.conditional.diplomanework.util.AndroidUtils.defaultDispatcher
import ru.sign.conditional.diplomanework.util.NeWorkHelper.exceptionCheck
import ru.sign.conditional.diplomanework.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appAuth: AppAuth
) : ViewModel() {
    val data: LiveData<AuthModel?>
        get() = appAuth.data.asLiveData(defaultDispatcher)
    private val _authState = MutableLiveData(AuthModelState())
    val authState: LiveData<AuthModelState>
        get() = _authState
    private val _authEvent: MutableLiveData<Int> = SingleLiveEvent(HTTP_CONTINUE)
    val authEvent: LiveData<Int>
        get() = _authEvent
    val authError = MutableLiveData(HTTP_OK)
    val authorized: Boolean
        get() = data.value != null
    val checkAuthorized = MutableLiveData(false)
    private val _media = MutableLiveData<MediaModel?>(null)
    val media: LiveData<MediaModel?>
        get() = _media

    fun authShowing() {
        viewModelScope.launch { _authState.value = _authState.value?.authShowing() }
    }

    fun regShowing() {
        viewModelScope.launch { _authState.value = _authState.value?.regShowing() }
    }

    fun login(login: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value?.loading()
                authRepository.login(login, password)
                _authEvent.value = HTTP_OK
            } catch (e: Exception) {
                authShowing()
                _authEvent.value = exceptionCheck(e)
            }
        }
    }

    fun register(
        login: String,
        password: String,
        name: String
    ) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value?.loading()
                authRepository.register(login, password, name, media.value)
                regShowing()
                _authEvent.value = HTTP_OK
            } catch (e: Exception) {
                _authEvent.value = exceptionCheck(e)
            }
        }
    }

    fun addAvatar(uri: Uri, file: File) {
        viewModelScope.launch { _media.value = MediaModel(uri, file) }
    }

    fun clearAvatar() {
        viewModelScope.launch { _media.value = null }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }

    fun saveAuthError(code: Int) {
        viewModelScope.launch { authError.value = code }
    }

    fun clearAuthError() {
        viewModelScope.launch {
            authError.value = HTTP_OK
            authShowing()
        }
    }

    fun checkAuth() {
        viewModelScope.launch {
            checkAuthorized.value = true
            checkAuthorized.value = false
        }
    }
}