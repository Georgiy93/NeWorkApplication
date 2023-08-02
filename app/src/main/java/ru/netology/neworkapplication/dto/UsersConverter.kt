package ru.netology.neworkapplication.dto

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class UsersConverter {
    @TypeConverter
    fun fromString(value: String): Map<String, UserPreview> {
        val type = object : TypeToken<Map<String, UserPreview>>() {}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun toString(value: Map<String, UserPreview>): String {
        return Gson().toJson(value)
    }
}
