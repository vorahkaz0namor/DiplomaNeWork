package ru.sign.conditional.diplomanework.util

import java.time.LocalDateTime
import java.time.OffsetDateTime

data class NeWorkDatetime(
    val year: CharSequence,
    val month: CharSequence,
    val day: CharSequence = "",
    val hour: CharSequence = "",
    val minute: CharSequence = ""
) {
    val offsetDateTime: OffsetDateTime? = toOffsetDateTime()
    val jobDatetime: OffsetDateTime? = toOffsetDateTime(itIsJob = true)

    private fun toOffsetDateTime(itIsJob: Boolean = false) =
        if (propertiesValidation(itIsJob))
            LocalDateTime
                .of(
                    /* year = */
                    year.toString().toInt(),
                    /* month = */
                    month.toString().toInt(),
                    /* dayOfMonth = */
                    if (itIsJob)
                        15
                    else
                        day.toString().toInt(),
                    /* hour = */
                    if (itIsJob)
                        8
                    else
                        hour.toString().toInt(),
                    /* minute = */
                    if (itIsJob)
                        0
                    else
                        minute.toString().toInt(),
                )
                .atOffset(OffsetDateTime.now().offset)
        else
            null

    private fun propertiesValidation(itIsJob: Boolean): Boolean {
        val validationForJob = year.isNotBlank() && month.isNotBlank()
        return if (!itIsJob) {
            validationForJob
                .and(day.isNotBlank())
                .and(hour.isNotBlank())
                .and(minute.isNotBlank())
        } else {
            validationForJob
        }
    }

    override fun toString(): String =
        "$day.$month.$year $hour:$minute"
}