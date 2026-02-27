package com.linktrip.application.port.input

interface AuthUseCase {
    fun authenticateBySerial(serialNumber: String): AuthResult

    data class AuthResult(
        val memberId: String,
        val accessToken: String,
        val isNewMember: Boolean,
    )
}
