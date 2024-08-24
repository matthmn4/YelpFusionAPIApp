package com.weedmaps.challenge.data.models

import com.google.gson.annotations.SerializedName

data class BusinessWithReview (
    val business: Business,
    val topReview: Review
)

data class ReviewResponse(
    @SerializedName("reviews")
    val reviews: List<Review>
)

data class Review(
    @SerializedName("text")
    val text: String
)


data class SearchResponse(
    @SerializedName("businesses")
    val businesses: List<Business>
)

data class Business(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("distance")
    val distance: Double,
    @field:SerializedName("rating")
    val rating: Double,
    @field:SerializedName("review_count")
    val reviewCount: Int
)