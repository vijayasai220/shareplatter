package com.example.shareplate.data

import kotlinx.serialization.Serializable

@Serializable
data class FoodDonation(
    val id: String? = null,
    val food_name: String,
    val serving_count: Int,
    val image_url: String? = null,
    val latitude: Double,
    val longitude: Double,
    val created_at: String? = null
) 