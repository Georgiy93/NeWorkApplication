package ru.netology.neworkapplication.dto

import androidx.room.TypeConverter

class TypeEventConverters {
    @TypeConverter
    fun fromEventType(eventType: EventType): String = eventType.name

    @TypeConverter
    fun toEventType(value: String): EventType = EventType.valueOf(value)
}