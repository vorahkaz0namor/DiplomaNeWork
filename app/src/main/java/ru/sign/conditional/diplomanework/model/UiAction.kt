package ru.sign.conditional.diplomanework.model

sealed class UiAction {
    data class Get(
        val id: Int
    ) : UiAction()

    data class Scroll(
        val currentId: Int
    ) : UiAction()
}