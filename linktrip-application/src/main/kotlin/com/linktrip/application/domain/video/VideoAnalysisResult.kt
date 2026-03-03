package com.linktrip.application.domain.video

data class VideoAnalysisResult(
    val valid: Boolean,
    val eats: List<EatInfo>,
    val attractions: List<AttractionInfo>,
    val shoppings: List<ShoppingInfo>,
    val transportations: List<TransportationInfo>,
) {
    data class EatInfo(
        val restaurant: String,
        val food: String,
        val restaurantsAndFoodsTips: String?,
    )

    data class AttractionInfo(
        val attractions: String,
        val attractionsTips: String?,
    )

    data class ShoppingInfo(
        val shopping: String,
        val shoppingTips: String?,
    )

    data class TransportationInfo(
        val transportation: String,
        val transportationTips: String?,
    )
}
