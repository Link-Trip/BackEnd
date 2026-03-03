package com.linktrip.output.http.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.linktrip.application.domain.video.VideoAnalysisResult

@JsonIgnoreProperties(ignoreUnknown = true)
data class AiApiResponse(
    val valid: Boolean,
    val eats: List<EatDto>?,
    val attractions: List<AttractionDto>?,
    val shoppings: List<ShoppingDto>?,
    val transportations: List<TransportationDto>?,
) {
    data class EatDto(
        val restaurant: String?,
        val food: String?,
        val restaurantsAndFoodsTips: String?,
    )

    data class AttractionDto(
        val attractions: String?,
        val attractionsTips: String?,
    )

    data class ShoppingDto(
        val shopping: String?,
        val shoppingTips: String?,
    )

    data class TransportationDto(
        val transportation: String?,
        val transportationTips: String?,
    )

    fun toDomain(): VideoAnalysisResult =
        VideoAnalysisResult(
            valid = valid,
            eats =
                eats?.map {
                    VideoAnalysisResult.EatInfo(
                        restaurant = it.restaurant ?: "",
                        food = it.food ?: "",
                        restaurantsAndFoodsTips = it.restaurantsAndFoodsTips,
                    )
                } ?: emptyList(),
            attractions =
                attractions?.map {
                    VideoAnalysisResult.AttractionInfo(
                        attractions = it.attractions ?: "",
                        attractionsTips = it.attractionsTips,
                    )
                } ?: emptyList(),
            shoppings =
                shoppings?.map {
                    VideoAnalysisResult.ShoppingInfo(
                        shopping = it.shopping ?: "",
                        shoppingTips = it.shoppingTips,
                    )
                } ?: emptyList(),
            transportations =
                transportations?.map {
                    VideoAnalysisResult.TransportationInfo(
                        transportation = it.transportation ?: "",
                        transportationTips = it.transportationTips,
                    )
                } ?: emptyList(),
        )
}
