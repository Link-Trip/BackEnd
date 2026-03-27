package com.linktrip.application.domain.video

import com.linktrip.application.domain.common.IdGenerator
import java.time.LocalDateTime

data class Hashtag(
    val id: String,
    val name: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun create(name: String): Hashtag =
            Hashtag(
                id = IdGenerator.generate(),
                name = name,
            )
    }
}
