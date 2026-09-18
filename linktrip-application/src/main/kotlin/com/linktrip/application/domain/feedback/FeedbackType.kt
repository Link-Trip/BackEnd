package com.linktrip.application.domain.feedback

import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException

enum class FeedbackType {
    SUGGESTION,
    BUG,
    ETC,
    ;

    companion object {
        fun from(value: String): FeedbackType =
            entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
                ?: throw LinktripException(ExceptionCode.BAD_REQUEST_FEEDBACK_TYPE)
    }
}
