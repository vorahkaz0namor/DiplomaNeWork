package ru.sign.conditional.diplomanework.adapter

import ru.sign.conditional.diplomanework.viewmodel.AuthViewModel

open class OnInteractionListenerImpl(
    private val authViewModel: AuthViewModel
) : OnInteractionListener {
    override val authorized: Boolean
        get() = authViewModel.authorized

    override fun checkAuth() {
        authViewModel.checkAuth()
    }
}