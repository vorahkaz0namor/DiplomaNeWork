package ru.sign.conditional.diplomanework.entity

import androidx.room.TypeConverter

class Converters {
    private val separator = ","

    private fun <T> listToString(list: List<T>?): String =
        buildString {
            list?.map { append(it, separator) }
        }

    @TypeConverter
    fun intListToString(list: List<Int>?): String =
        listToString(list)

    @TypeConverter
    fun stringListToString(list: List<String>?): String =
        listToString(list)

    @TypeConverter
    fun stringToIntList(string: String?): List<Int>? =
        stringToStringList(string)?.map { it.toInt() }

    @TypeConverter
    fun stringToStringList(string: String?): List<String>? =
        string?.split(separator)?.filter { it.isNotEmpty() }
}