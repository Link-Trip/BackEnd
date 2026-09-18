package com.linktrip.application.port.input

interface MemberWithdrawUseCase {
    fun withdraw(memberId: String): WithdrawResult

    data class WithdrawResult(
        val deletedTripPlanCount: Int,
    )
}
