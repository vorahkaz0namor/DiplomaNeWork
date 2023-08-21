package ru.sign.conditional.diplomanework.dto

data class Job(
    val id: Int,
    val idFromServer: Int = 0,
    val name: String,
    val position: String,
    val start: String,
    val finish: String? = null,
    val link: String? = null
)
