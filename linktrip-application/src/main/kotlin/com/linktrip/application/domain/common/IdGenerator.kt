package com.linktrip.application.domain.common

import java.util.UUID

object IdGenerator {
    fun generate(): String = UUID.randomUUID().toString()
}
