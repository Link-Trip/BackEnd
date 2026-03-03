package com.linktrip.input.http.controller.dto.response

import com.linktrip.application.domain.video.VideoAnalysisResult

data class VideoAnalyzeResponse(
    val eats: List<EatResponse>,
    val attractions: List<AttractionResponse>,
    val shoppings: List<ShoppingResponse>,
    val transportations: List<TransportationResponse>,
) {
    data class EatResponse(
        val restaurant: String,
        val food: String,
        val restaurantsAndFoodsTips: String?,
    )

    data class AttractionResponse(
        val attractions: String,
        val attractionsTips: String?,
    )

    data class ShoppingResponse(
        val shopping: String,
        val shoppingTips: String?,
    )

    data class TransportationResponse(
        val transportation: String,
        val transportationTips: String?,
    )

    companion object {
        fun from(result: VideoAnalysisResult): VideoAnalyzeResponse =
            VideoAnalyzeResponse(
                eats =
                    result.eats.map {
                        EatResponse(
                            restaurant = it.restaurant,
                            food = it.food,
                            restaurantsAndFoodsTips = it.restaurantsAndFoodsTips,
                        )
                    },
                attractions =
                    result.attractions.map {
                        AttractionResponse(
                            attractions = it.attractions,
                            attractionsTips = it.attractionsTips,
                        )
                    },
                shoppings =
                    result.shoppings.map {
                        ShoppingResponse(
                            shopping = it.shopping,
                            shoppingTips = it.shoppingTips,
                        )
                    },
                transportations =
                    result.transportations.map {
                        TransportationResponse(
                            transportation = it.transportation,
                            transportationTips = it.transportationTips,
                        )
                    },
            )
    }
}
