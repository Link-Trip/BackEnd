package com.linktrip.application.domain.member

import com.linktrip.application.port.input.AuthUseCase
import com.linktrip.application.port.input.AuthUseCase.AuthResult
import com.linktrip.application.port.output.auth.TokenProvider
import com.linktrip.application.port.output.persistence.MemberPort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val memberPort: MemberPort,
    private val tokenProvider: TokenProvider,
) : AuthUseCase {
    @Transactional
    override fun authenticateBySerial(serialNumber: String): AuthResult {
        val existingMember = memberPort.findBySerialNumber(serialNumber)

        val member =
            existingMember ?: memberPort.save(
                Member.create(serialNumber = serialNumber),
            )

        val accessToken = tokenProvider.create(member.id)

        return AuthResult(
            memberId = member.id,
            accessToken = accessToken,
            isNewMember = existingMember == null,
        )
    }
}
