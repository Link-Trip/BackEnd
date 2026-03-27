package com.linktrip.application.domain.trip

import com.linktrip.application.domain.common.CursorPage
import com.linktrip.application.port.input.TripPlanDetail
import com.linktrip.application.port.input.TripPlanItemDetail
import com.linktrip.application.port.input.TripPlanSummary
import com.linktrip.application.port.input.TripPlanUseCase
import com.linktrip.application.port.input.UpdateTripPlanCommand
import com.linktrip.application.port.output.persistence.TravelItineraryItemPersistencePort
import com.linktrip.application.port.output.persistence.TripPlanItemPersistencePort
import com.linktrip.application.port.output.persistence.TripPlanPersistencePort
import com.linktrip.application.port.output.persistence.TripPlanRequestPersistencePort
import com.linktrip.common.exception.ExceptionCode
import com.linktrip.common.exception.LinktripException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class TripPlanService(
    private val planPort: TripPlanPersistencePort,
    private val planItemPort: TripPlanItemPersistencePort,
    private val itineraryItemPort: TravelItineraryItemPersistencePort,
    private val requestPort: TripPlanRequestPersistencePort,
) : TripPlanUseCase {
    @Transactional
    override fun registerRequest(
        memberId: String,
        videoAnalysisTaskId: String,
    ) {
        if (requestPort.existsByMemberIdAndVideoAnalysisTaskId(memberId, videoAnalysisTaskId)) return
        requestPort.save(TripPlanRequest.create(memberId, videoAnalysisTaskId))
    }

    @Transactional
    override fun createFromAnalysisIfAbsent(
        memberId: String,
        videoAnalysisTaskId: String,
        title: String,
    ) {
        if (memberId.isBlank()) return

        if (planPort.existsByMemberIdAndVideoAnalysisTaskId(memberId, videoAnalysisTaskId)) {
            return
        }

        val itineraryItems = itineraryItemPort.findByVideoAnalysisTaskId(videoAnalysisTaskId)
        if (itineraryItems.isEmpty()) return

        val tripPlan =
            TripPlan.create(
                memberId = memberId,
                videoAnalysisTaskId = videoAnalysisTaskId,
                title = title,
            )
        val savedTripPlan = planPort.save(tripPlan)

        val tripPlanItems =
            itineraryItems.map { item ->
                TripPlanItem.create(
                    tripPlanId = savedTripPlan.id,
                    travelItineraryItemId = item.id,
                    day = item.day,
                    itemOrder = item.itemOrder,
                )
            }
        planItemPort.saveAll(tripPlanItems)
    }

    @Transactional(readOnly = true)
    override fun getTripPlans(
        memberId: String,
        cursor: LocalDateTime?,
        size: Int,
    ): CursorPage<TripPlanSummary> {
        val fetchSize = size + 1
        val rows = planPort.findSummariesByMemberId(memberId, cursor, fetchSize)
        val hasNext = rows.size > size
        val items = rows.take(size)

        val summaries =
            items.map { row ->
                TripPlanSummary(
                    tripPlan = row.tripPlan,
                    youtubeUrl = row.youtubeUrl,
                    itemCount = row.activeItemCount,
                )
            }

        val nextCursor = if (hasNext) items.last().tripPlan.createdAt.toString() else null

        return CursorPage(
            items = summaries,
            nextCursor = nextCursor,
            hasNext = hasNext,
        )
    }

    @Transactional(readOnly = true)
    override fun getTripPlanDetail(
        memberId: String,
        tripPlanId: String,
    ): TripPlanDetail {
        val tripPlan = findTripPlanOrThrow(tripPlanId, memberId)

        val itemsWithItinerary =
            planItemPort.findActiveWithItineraryAndPlaceByTripPlanId(tripPlanId)

        val itemDetails =
            itemsWithItinerary.map { row ->
                TripPlanItemDetail(row.tripPlanItem, row.travelItineraryItem)
            }

        return TripPlanDetail(tripPlan, itemDetails)
    }

    @Transactional
    override fun updateTripPlan(
        memberId: String,
        tripPlanId: String,
        command: UpdateTripPlanCommand,
    ): TripPlanDetail {
        findTripPlanOrThrow(tripPlanId, memberId)

        if (command.title != null) {
            planPort.updateTitle(tripPlanId, command.title)
        }

        if (command.items != null) {
            planItemPort.updateItems(tripPlanId, command.items)
        }

        return getTripPlanDetail(memberId, tripPlanId)
    }

    @Transactional
    override fun deleteTripPlan(
        memberId: String,
        tripPlanId: String,
    ) {
        findTripPlanOrThrow(tripPlanId, memberId)
        planItemPort.deleteByTripPlanId(tripPlanId)
        planPort.deleteById(tripPlanId)
    }

    private fun findTripPlanOrThrow(
        tripPlanId: String,
        memberId: String,
    ): TripPlan {
        val tripPlan =
            planPort.findById(tripPlanId)
                ?: throw LinktripException(ExceptionCode.NOT_FOUND_TRIP_PLAN)
        if (tripPlan.memberId != memberId) {
            throw LinktripException(ExceptionCode.FORBIDDEN_TRIP_PLAN)
        }
        return tripPlan
    }
}
