package com.linktrip.common.exception

class LinktripException : RuntimeException {
    val statusCode: Int
    val defaultMessage: String
    val detailMessage: String?

    constructor(
        exceptionCode: ExceptionCode,
        detailMessage: String,
    ) : super("[$exceptionCode] $detailMessage") {
        this.statusCode = exceptionCode.statusCode
        this.defaultMessage = exceptionCode.defaultMessage
        this.detailMessage = detailMessage
    }

    constructor(exceptionCode: ExceptionCode) :
        super("[$exceptionCode] ${exceptionCode.defaultMessage}") {
        this.statusCode = exceptionCode.statusCode
        this.defaultMessage = exceptionCode.defaultMessage
        this.detailMessage = null
    }
}
