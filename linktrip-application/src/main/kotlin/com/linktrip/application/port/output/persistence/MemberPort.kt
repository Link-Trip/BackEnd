package com.linktrip.application.port.output.persistence

import com.linktrip.application.domain.member.Member

interface MemberPort {
    fun findBySerialNumber(serialNumber: String): Member?

    fun save(member: Member): Member
}
