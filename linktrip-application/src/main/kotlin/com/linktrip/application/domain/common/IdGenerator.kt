package com.linktrip.application.domain.common

import java.security.SecureRandom
import java.util.UUID

object IdGenerator {
    private val random = SecureRandom()

    /**
     * UUID v7 호환 time-ordered UUID를 생성한다.
     * 상위 48비트에 밀리초 타임스탬프가 들어가므로
     * B-Tree 인덱스에서 순차 삽입이 보장되어 페이지 분할을 최소화한다.
     */
    fun generate(): String {
        val timestamp = System.currentTimeMillis()
        val randomBytes = ByteArray(10).also { random.nextBytes(it) }

        val msb =
            (timestamp shl 16) or // 상위 48비트: 타임스탬프
                (0x7000L) or // version 7
                (randomBytes[0].toLong() and 0x0FL shl 8) or
                (randomBytes[1].toLong() and 0xFFL)

        val lsb =
            (0x80L shl 56) or // variant 10
                (randomBytes[2].toLong() and 0x3FL shl 56) or
                (randomBytes[3].toLong() and 0xFFL shl 48) or
                (randomBytes[4].toLong() and 0xFFL shl 40) or
                (randomBytes[5].toLong() and 0xFFL shl 32) or
                (randomBytes[6].toLong() and 0xFFL shl 24) or
                (randomBytes[7].toLong() and 0xFFL shl 16) or
                (randomBytes[8].toLong() and 0xFFL shl 8) or
                (randomBytes[9].toLong() and 0xFFL)

        return UUID(msb, lsb).toString()
    }
}
