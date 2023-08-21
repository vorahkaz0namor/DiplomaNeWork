package ru.sign.conditional.diplomanework.dto

sealed interface FeedItem {
    val id: Int
    val attachment: Attachment?
}