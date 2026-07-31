package com.linktrip.application.domain.member

import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException

enum class Platform {
    IOS,
    ANDROID,
    ;

    companion object {
        fun from(value: String): Platform =
            entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
                ?: throw LinktripException(ExceptionCode.BAD_REQUEST_PLATFORM)
    }
}
