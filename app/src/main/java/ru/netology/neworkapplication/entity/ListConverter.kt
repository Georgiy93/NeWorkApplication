package ru.netology.neworkapplication.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.neworkapplication.dto.Attachment

class ListConverter {
    @TypeConverter
    fun fromString(value: String): List<Int> {
        return value.split(",").map { it.toInt() }
    }

    @TypeConverter
    fun toString(value: List<Int>): String {
        return value.joinToString(",")
    }

}
