package ru.sign.conditional.diplomanework.util

import java.time.LocalDateTime
import java.time.OffsetDateTime

data class NeWorkDatetime(
    val year: CharSequence,
    val month: CharSequence,
    val day: CharSequence,
    val hour: CharSequence,
    val minute: CharSequence
) {
    val offsetDateTime: OffsetDateTime? =
        if (propertiesValidation()) {
            LocalDateTime
                .of(
                    /* year = */
                    year.toString().toInt(),
                    /* month = */
                    month.toString().toInt(),
                    /* dayOfMonth = */
                    day.toString().toInt(),
                    /* hour = */
                    hour.toString().toInt(),
                    /* minute = */
                    minute.toString().toInt(),
                )
                .atOffset(OffsetDateTime.now().offset)
        } else
            null

    private fun propertiesValidation() =
        year.isNotBlank()
            .and(month.isNotBlank())
            .and(day.isNotBlank())
            .and(hour.isNotBlank())
            .and(minute.isNotBlank())

    override fun toString(): String =
        "$day.$month.$year $hour:$minute"
}