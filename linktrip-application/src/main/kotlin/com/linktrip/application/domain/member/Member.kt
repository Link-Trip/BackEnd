package com.linktrip.application.domain.member

import com.linktrip.application.domain.common.IdGenerator
import java.time.LocalDateTime

data class Member(
    val id: String,
    val email: String?,
    val providerType: ProviderType,
    val providerId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun create(
            email: String?,
            providerType: ProviderType,
            providerId: String,
        ): Member =
            Member(
                id = IdGenerator.generate(),
                email = email,
                providerType = providerType,
                providerId = providerId,
            )
    }
}
