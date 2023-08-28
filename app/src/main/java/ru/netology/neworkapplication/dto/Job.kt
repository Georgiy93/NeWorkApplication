package ru.netology.neworkapplication.dto

import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

sealed interface FeedItemJob {
    val id: Long
}

data class Job(
    override val id: Long,
    val name: String,
    val position: String,
    val start: String,
    val finish: String? = null,
    val link: String? = null,
) : FeedItemJob
