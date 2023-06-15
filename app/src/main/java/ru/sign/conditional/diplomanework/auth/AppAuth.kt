package ru.sign.conditional.diplomanework.auth

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.sign.conditional.diplomanework.model.AuthModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
    private val context: Context
) {
    companion object {
        private const val TOKEN_KEY = "TOKEN_KEY"
        private const val ID_KEY = "ID_KEY"
    }
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val _data: MutableStateFlow<AuthModel?>

    init {
        val token = prefs.getString(TOKEN_KEY, null)
        val id = prefs.getInt(ID_KEY, 0)
        _data = MutableStateFlow(
            if (token == null || id == 0) {
                prefs.edit { clear() }
                null
            } else
                AuthModel(id, token)
        )
    }

    val data: StateFlow<AuthModel?>
        get() = _data.asStateFlow()

    @Synchronized
    fun setAuth(authModel: AuthModel) {
        _data.value = authModel
        prefs.edit {
            putInt(ID_KEY, authModel.id)
            putString(TOKEN_KEY, authModel.token)
        }
    }

    @Synchronized
    fun removeAuth() {
        _data.value = null
        prefs.edit { clear() }
    }
}