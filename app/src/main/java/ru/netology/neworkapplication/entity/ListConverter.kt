package ru.netology.neworkapplication.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.neworkapplication.dto.Attachment

class ListConverter {
    @TypeConverter
    fun fromString(value: String): List<Long> {
        return value.split(",").mapNotNull {
            it.toLongOrNull()
        }
    }

    @TypeConverter
    fun toString(value: List<Long>): String {
        return value.joinToString(",")
    }

}
