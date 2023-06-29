package ru.sign.conditional.diplomanework.adapter

interface OnInteractionListener {
    val authorized: Boolean
    fun checkAuth()
}