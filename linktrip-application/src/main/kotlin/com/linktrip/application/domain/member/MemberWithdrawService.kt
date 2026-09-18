package com.linktrip.application.domain.member

import com.linktrip.application.port.input.MemberWithdrawUseCase
import com.linktrip.application.port.input.MemberWithdrawUseCase.WithdrawResult
import com.linktrip.application.port.output.persistence.MemberPort
import com.linktrip.application.port.output.persistence.TripPlanItemPersistencePort
import com.linktrip.application.port.output.persistence.TripPlanPersistencePort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * 회원 탈퇴(개인정보 파기).
 *
 * 회원 레코드는 통계·정합성을 위해 남기되 기기식별자(serialNumber)를 비가역 마스킹하고,
 * 회원이 생성한 여행 계획을 소프트 삭제한다. 마스킹 후에는 동일 기기로 재로그인해도
 * 기존 회원과 매칭되지 않아 새 회원으로 시작한다.
 */
@Service
class MemberWithdrawService(
    private val memberPort: MemberPort,
    private val tripPlanPersistencePort: TripPlanPersistencePort,
    private val tripPlanItemPersistencePort: TripPlanItemPersistencePort,
) : MemberWithdrawUseCase {
    @Transactional
    override fun withdraw(memberId: String): WithdrawResult {
        val member =
            memberPort.findById(memberId)
                ?: throw LinktripException(ExceptionCode.NOT_FOUND_MEMBER)

        if (member.isWithdrawn) {
            return WithdrawResult(deletedTripPlanCount = 0)
        }

        val tripPlans = tripPlanPersistencePort.findByMemberId(memberId)
        tripPlans.forEach { tripPlan ->
            tripPlanItemPersistencePort.deleteByTripPlanId(tripPlan.id)
            tripPlanPersistencePort.deleteById(tripPlan.id)
        }

        memberPort.withdraw(member.withdraw())

        logger.info { "회원 탈퇴 처리 완료: memberId=$memberId, deletedTripPlanCount=${tripPlans.size}" }
        return WithdrawResult(deletedTripPlanCount = tripPlans.size)
    }
}
