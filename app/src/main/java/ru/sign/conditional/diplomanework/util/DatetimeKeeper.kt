package ru.sign.conditional.diplomanework.util

import java.time.Instant
import java.time.OffsetDateTime

class DatetimeKeeper {
    private val currentYear: Int
        get() = Instant.now().atOffset(OffsetDateTime.now().offset).year

    fun datetimeValidation(datetime: NeWorkDatetime): NeWorkDatetime =
        NeWorkDatetime(
            year = yearValidation(year = datetime.year),
            month = valueValidation(
                value = datetime.month,
                limit = 13
            ),
            day = valueValidation(
                value = datetime.day,
                limit = 32
            ),
            hour = valueValidation(
                value = datetime.hour,
                limit = 24
            ),
            minute = valueValidation(
                value = datetime.minute,
                limit = 60
            )
        )

    fun jobDateValidation(date: NeWorkDatetime): NeWorkDatetime =
        NeWorkDatetime(
            year = yearValidation(
                year = date.year,
                itIsJob = true
            ),
            month = valueValidation(
                value = date.month,
                limit = 13
            )
        )

    private fun yearValidation(
        year: CharSequence,
        itIsJob: Boolean = false
    ): CharSequence =
        year
            .filterNot { char ->
                char.digitToIntOrNull() == null ||
                year.startsWith("0")
            }
            .filterIndexed { index, _ -> index < 4 }
            .let { correctedYear ->
                val yearValidation =
                    correctedYear.isNotBlank() &&
                    correctedYear.length == 4 &&
                    if (itIsJob)
                        correctedYear.toString().toInt() > currentYear
                    else
                        correctedYear.toString().toInt() < currentYear
                if (yearValidation)
                    ""
                else
                    correctedYear
            }

    private fun valueValidation(
        value: CharSequence,
        limit: Int
    ): CharSequence =
        value
            .filterNot { char ->
                char.digitToIntOrNull() == null ||
                value.startsWith("0")
            }
            .let { correctedValue ->
                val valueValidation =
                    correctedValue.isNotBlank() &&
                    correctedValue.toString().toInt() >= limit
                if (valueValidation)
                    ""
                else
                    correctedValue
            }
}