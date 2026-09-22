package com.linktrip.application.domain.terms

import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException

enum class TermsType {
    SERVICE,
    PRIVACY,
    ;

    companion object {
        fun from(value: String): TermsType =
            entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
                ?: throw LinktripException(ExceptionCode.BAD_REQUEST_TERMS_TYPE)
    }
}
