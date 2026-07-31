package com.linktrip.output.persistence.mysql.adapter

import com.linktrip.application.domain.member.Member
import com.linktrip.application.port.output.persistence.MemberPort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import com.linktrip.output.persistence.mysql.entity.MemberEntity
import com.linktrip.output.persistence.mysql.repository.MemberJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class MemberAdapter(
    private val memberJpaRepository: MemberJpaRepository,
) : MemberPort {
    override fun findBySerialNumber(serialNumber: String): Member? =
        memberJpaRepository.findBySerialNumber(serialNumber)?.toDomain()

    override fun findById(id: String): Member? = memberJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun save(member: Member): Member = memberJpaRepository.save(MemberEntity.from(member)).toDomain()

    override fun update(member: Member): Member {
        val entity =
            memberJpaRepository.findByIdOrNull(member.id)
                ?: throw LinktripException(ExceptionCode.NOT_FOUND_MEMBER)
        entity.applySettings(member)
        return memberJpaRepository.save(entity).toDomain()
    }
}
