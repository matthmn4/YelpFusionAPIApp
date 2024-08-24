package com.weedmaps.challenge.data.network

import com.weedmaps.challenge.data.models.ReviewResponse
import com.weedmaps.challenge.data.models.SearchResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitInterface {

    //https://api.yelp.com/v3/
    //businesses/search
    //?location=NYC&term=Starbucks&open_now=true&sort_by=distance&limit=25
    @Headers("accept: application/json")
    @GET("businesses/search")
    fun getSearchResults(
        @Header("Authorization") apiKey: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("term") term: String,
        @Query("open_now") open: Boolean,
        @Query("sort_by") sortBy: String,
        @Query("limit") limit: Int
    ): Call<SearchResponse>

    @Headers("accept: application/json")
    @GET("businesses/search")
    fun getSearchResults(
        @Header("Authorization") apiKey: String,
        @Query("location") location: String,
        @Query("term") term: String,
        @Query("open_now") open: Boolean,
        @Query("sort_by") sortBy: String,
        @Query("limit") limit: Int
    ): Call<SearchResponse>

    @Headers("accept: application/json")
    @GET("businesses/{id}/reviews")
    fun getBusinessReviews(
        @Header("Authorization") apiKey: String,
        @Path("id") businessId: String,
        @Query("sort_by") sortBy: String,
    ): Call<ReviewResponse>

}
