package ru.sign.conditional.diplomanework.dto

data class Attachment(
    val url: String,
    val type: AttachmentType
) {
    override fun toString(): String =
        "Post attachment:\nUrl = $url\nType = ${type.name}"
}