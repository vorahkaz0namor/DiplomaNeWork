package ru.sign.conditional.diplomanework.model

data class AuthModelState(
    val loading: Boolean = false,
    val authShowing: Boolean = false,
    val regShowing: Boolean = false
) {
    fun loading() =
        copy(
            loading = true,
            authShowing = false,
            regShowing = false
        )

    fun authShowing() =
        copy(
            loading = false,
            authShowing = true,
            regShowing = false
        )

    fun regShowing() =
        copy(
            loading = false,
            authShowing = false,
            regShowing = true
        )

    fun error() =
        copy(
            loading = false,
            authShowing = false,
            regShowing = false
        )
}