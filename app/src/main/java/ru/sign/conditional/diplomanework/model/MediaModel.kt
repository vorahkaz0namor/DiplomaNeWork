package ru.sign.conditional.diplomanework.model

import android.net.Uri
import java.io.File

data class MediaModel(
    val uri: Uri,
    val file: File
) {
    override fun toString(): String =
        "Media model:\nUri = $uri\nFilename = ${file.name}"
}