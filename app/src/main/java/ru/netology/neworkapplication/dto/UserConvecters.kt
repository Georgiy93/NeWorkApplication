package ru.netology.neworkapplication.dto

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class UserConvecters {
    @TypeConverter
    fun fromUserPreviewMap(value: Map<Int, UserPreview>): String {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toUserPreviewMap(value: String): Map<Int, UserPreview> {
        val gson = Gson()
        val type = object : TypeToken<Map<Int, UserPreview>>() {}.type
        return gson.fromJson(value, type)
    }
}