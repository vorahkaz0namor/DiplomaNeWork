package ru.sign.conditional.diplomanework.model

data class UiState(
    val id: Int = 0,
    val lastIdScrolled: Int = 0,
    val hasNotScrolledToCurrentId: Boolean = id > lastIdScrolled
)