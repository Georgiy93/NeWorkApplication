package ru.netology.neworkapplication.entity

import ru.netology.neworkapplication.dto.Attachment
import ru.netology.neworkapplication.enumeration.AttachmentType


data class AttachmentEmbeddable(
    var url: String,
    var type: AttachmentType,
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}


