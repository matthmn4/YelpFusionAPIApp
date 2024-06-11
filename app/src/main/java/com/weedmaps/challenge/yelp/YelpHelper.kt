package com.weedmaps.challenge.yelp

import android.util.Log
import com.weedmaps.challenge.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object YelpHelper {

    private val TAG = this::class.simpleName
    private const val KEY:
            String = "Bearer n0ju-XhvEen3ms1hGCe_PT4wOy8KvcUWTIhc7kf7eOlc4qjC5f29G2cDwN3rfpXmyFSXVaDvJ3FpUYWulSJoYsw-KjjkkhTpquUY5XYbHGd-jOkuhs4zSTcAvbJjZnYx"

    fun callYelpSearchAPI(latitude: Double, longitude: Double, searchTerm: String, limit: Int,
                          responseCallback: (successResponse: SearchResponse?) -> Unit) {
        val call: Call<SearchResponse> =
            RetrofitClient.getRetrofitClient().getSearchResults(KEY, latitude, longitude, searchTerm, true, "distance", limit)
        call.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                if (response.isSuccessful) {
                    val searchResponse: SearchResponse? = response.body()
                    Log.d(TAG,"callYelpSearchAPI Success response: " + searchResponse.toString())
                    responseCallback(searchResponse)
                } else {
                    Log.d(TAG,"callYelpSearchAPI Error response")
                    responseCallback(null)
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Log.d(TAG, "callYelpSearchAPI onFailure: " + t.message)
                responseCallback(null)
            }

        })
    }

    fun callYelpSearchAPI(location: String, searchTerm: String, limit: Int,
                          responseCallback: (successResponse: SearchResponse?) -> Unit) {
        val call: Call<SearchResponse> =
            RetrofitClient.getRetrofitClient().getSearchResults(KEY, location, searchTerm, true, "distance", limit)
        call.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                if (response.isSuccessful) {
                    val searchResponse: SearchResponse? = response.body()
                    Log.d(TAG,"callYelpSearchAPI Success response: " + searchResponse.toString())
                    responseCallback(searchResponse)
                } else {
                    Log.d(TAG,"callYelpSearchAPI Error response")
                    responseCallback(null)
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Log.d(TAG, "callYelpSearchAPI onFailure: " + t.message)
                responseCallback(null)
            }

        })
    }

    fun callYelpReviewAPI(businessId: String, responseCallback: (successResponse: ReviewResponse?) -> Unit) {
        val call: Call<ReviewResponse> =
            RetrofitClient.getRetrofitClient().getBusinessReviews(KEY, businessId, "yelp_sort")
        call.enqueue(object : Callback<ReviewResponse> {
            override fun onResponse(
                call: Call<ReviewResponse>,
                response: Response<ReviewResponse>
            ) {
                if (response.isSuccessful) {
                    val reviewResponse: ReviewResponse? = response.body()
                    if (reviewResponse != null && reviewResponse.reviews.isNotEmpty()) {
                        Log.d(TAG, "callYelpReviewAPI Success response: $reviewResponse")
                        responseCallback(reviewResponse)
                    }
                } else {
                    Log.d(TAG,"callYelpReviewAPI Error response")
                    responseCallback(null)
                }
            }

            override fun onFailure(call: Call<ReviewResponse>, t: Throwable) {
                Log.d(TAG, "callYelpReviewAPI onFailure: " + t.message)
                responseCallback(null)
            }

        })
    }
}
