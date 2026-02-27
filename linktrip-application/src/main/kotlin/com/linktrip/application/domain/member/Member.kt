package com.linktrip.application.domain.member

import com.linktrip.application.domain.common.IdGenerator
import java.time.LocalDateTime

data class Member(
    val id: String,
    val serialNumber: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun create(serialNumber: String): Member =
            Member(
                id = IdGenerator.generate(),
                serialNumber = serialNumber,
            )
    }
}
