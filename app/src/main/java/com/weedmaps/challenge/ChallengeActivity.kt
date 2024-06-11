package com.weedmaps.challenge

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import com.weedmaps.challenge.ui.theme.ChallengeActivityTheme
import com.weedmaps.challenge.yelp.Business
import com.weedmaps.challenge.yelp.BusinessWithReview
import com.weedmaps.challenge.yelp.Review
import com.weedmaps.challenge.yelp.ReviewResponse
import com.weedmaps.challenge.yelp.SearchResponse
import com.weedmaps.challenge.yelp.YelpBusinessesUI
import com.weedmaps.challenge.yelp.YelpHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChallengeActivity : ComponentActivity() {

    private val TAG = this::class.simpleName
    private var businessListWithReviewsState = mutableStateOf<List<BusinessWithReview>>(emptyList())
    private var searchQueryState = mutableStateOf("food")
    private var locationValueState = mutableStateOf("")
    private var isLoadingState by mutableStateOf(false)
    private var isSearchingState by mutableStateOf(false)
    private var outOfBusinessesToLoad by mutableStateOf(false)

    private var offset = 0

    private val businessList = mutableListOf<Business>()
    private val businessListWithReviews = mutableListOf<BusinessWithReview>()

    private var searchJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use FLP if need locaation updates instead
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "onCreate: Missing permissions to retrieve last known location")
            return
        }
        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val currentLatitude = lastKnownLocation?.latitude ?: 0.0
        val currentLongitude = lastKnownLocation?.longitude ?: 0.0

        // new thread for initial fill business + reviews list
        loadBusinessListWithReviews(currentLatitude, currentLongitude, locationValueState.value, searchQueryState.value)

        setContent {
            ChallengeActivityTheme {
                YelpBusinessesUI(businessListWithReviewsState.value,
                    searchQuery = searchQueryState.value,
                    locationValue = locationValueState.value,
                    isLoading = isLoadingState,
                    isSearching = isSearchingState,
                    onSearch = { searchTerm, location ->
                        searchQueryState.value = searchTerm
                        locationValueState.value = location
                        loadBusinessListWithReviews(currentLatitude, currentLongitude, location, searchTerm)
                    },
                    onScrollToEnd = { searchTerm, location ->
                        searchQueryState.value = searchTerm
                        locationValueState.value = location
                        loadMoreBusinessListWithReviews(currentLatitude, currentLongitude, location, searchTerm)
                    }
                )
            }
        }
    }

    private fun loadBusinessListWithReviews(latitude: Double, longitude: Double, location: String, searchTerm: String) {
        isLoadingState = true
        isSearchingState = true
        offset = 0
        Log.d(TAG, "loadBusinessListWithReviews: limit offset is currently $offset, location is $location")
        CoroutineScope(Dispatchers.IO).launch {
            if (location.isNotEmpty() && location.lowercase() == "near me") {
                getBusinessListWithReviews(latitude, longitude, searchTerm, 10)
            }
            else if (location.isNotEmpty()) {
                getLocationBusinessListWithReviews(location, searchTerm, 10)
            } else {
                getBusinessListWithReviews(latitude, longitude, searchTerm, 10)
            }
        }
    }

    private fun loadMoreBusinessListWithReviews(latitude: Double, longitude: Double, location: String, searchTerm: String) {
        if (outOfBusinessesToLoad) {
            Log.d(TAG, "loadMoreBusinessListWithReviews: outOfBusinessesToLoad")
            isLoadingState = false
            return
        }
        if (isLoadingState || searchJob?.isActive == true) {
            Log.d(TAG, "loadMoreBusinessListWithReviews: already loading a searchRequest")
            return
        }
        isLoadingState = true
        offset += 10
        Log.d(TAG, "loadMoreBusinessListWithReviews: limit offset is currently $offset, location is $location")
        CoroutineScope(Dispatchers.IO).launch {
            if (location.isNotEmpty() && location.lowercase() == "near me") {
                getBusinessListWithReviews(latitude, longitude, searchTerm, 10 + offset)
            }
            else if (location.isNotEmpty()) {
                getLocationBusinessListWithReviews(location, searchTerm, 10 + offset)
            } else {
                getBusinessListWithReviews(latitude, longitude, searchTerm, 10 + offset)
            }
        }
    }


    private fun getLocationBusinessListWithReviews(location: String, searchTerm: String, limit: Int) {
        // cancel the job if the user calls while isLoading
        if (isLoadingState) {
            searchJob?.cancel()
        }

        YelpHelper.callYelpSearchAPI(location, searchTerm, limit,) { successResponse: SearchResponse? ->
            if (successResponse != null) {
                if (offset == 0) {
                    Log.d(TAG, "getBusinessListWithReviews: cleared business lists")
                    businessList.clear()
                    businessListWithReviews.clear()
                }
                Log.d(TAG, "business list before sublist from offset to size ${businessList.size}, current limit is $limit")

                // case where we get less than increments of 10 per request
                var limitChecked = limit
                if (successResponse.businesses.size < limit) {
                    Log.d(TAG, "Response is less than the current limit of $limit ")
                    limitChecked = successResponse.businesses.size - offset
                }

                // no more businesses left to load
                if (successResponse.businesses.size == businessList.size) {
                    isLoadingState = false
                    outOfBusinessesToLoad = true
                } else {
                    Log.d(TAG, "getBusinessListWithReviews: offset is $offset, limitchecked is $limitChecked")
                    if (limitChecked % 10 != 0) {
                        limitChecked += offset
                    }
                    businessList.addAll(
                        successResponse.businesses.subList(offset, limitChecked)
                    )
                    Log.d(TAG, "business list after sublist from offset to size: ${businessList.size}")

                    searchJob = CoroutineScope(Dispatchers.IO).launch {
                        businessList.subList(offset, limitChecked).forEach { business ->
                            delay(200)
                            val topReview = getBusinessTopReview(business.id)
                            businessListWithReviews.add(BusinessWithReview(business, topReview))
                        }

                        // notifydatasetchanged()
                        withContext(Dispatchers.Main) {
                            isLoadingState = false
                            isSearchingState = false
                            businessListWithReviewsState.value = businessListWithReviews
                        }
                    }
                }
            } else {
                Log.d(TAG, "getBusinessListWithReviews callback response is null")
            }
        }
    }

    private suspend fun getBusinessListWithReviews(latitude: Double, longitude: Double, searchTerm: String, limit: Int) {
        // cancel the job if the user calls while isLoading
        if (isLoadingState) {
            searchJob?.cancel()
        }

        YelpHelper.callYelpSearchAPI(latitude, longitude, searchTerm, limit,) { successResponse: SearchResponse? ->
            if (successResponse != null) {
                if (offset == 0) {
                    Log.d(TAG, "getBusinessListWithReviews: cleared business lists")
                    businessList.clear()
                    businessListWithReviews.clear()
                }
                Log.d(TAG, "business list before sublist from offset to size ${businessList.size}, current limit is $limit")

                // case where we get less than increments of 10 per request
                var limitChecked = limit
                if (successResponse.businesses.size < limit) {
                    Log.d(TAG, "Response is less than the current limit of $limit ")
                    limitChecked = successResponse.businesses.size - offset
                }

                // no more businesses left to load
                if (successResponse.businesses.size == businessList.size) {
                    isLoadingState = false
                    outOfBusinessesToLoad = true
                } else {
                    Log.d(TAG, "getBusinessListWithReviews: offset is $offset, limitchecked is $limitChecked")
                    if (limitChecked % 10 != 0) {
                        limitChecked += offset
                    }
                    businessList.addAll(
                        successResponse.businesses.subList(offset, limitChecked)
                    )
                    Log.d(TAG, "business list after sublist from offset to size: ${businessList.size}")

                    searchJob = CoroutineScope(Dispatchers.IO).launch {
                        businessList.subList(offset, limitChecked).forEach { business ->
                            delay(200)
                            val topReview = getBusinessTopReview(business.id)
                            businessListWithReviews.add(BusinessWithReview(business, topReview))
                        }

                        // notifydatasetchanged()
                        withContext(Dispatchers.Main) {
                            isLoadingState = false
                            isSearchingState = false
                            businessListWithReviewsState.value = businessListWithReviews
                        }
                    }
                }
            } else {
                Log.d(TAG, "getBusinessListWithReviews callback response is null")
            }
        }
    }

    private suspend fun getBusinessTopReview(businessId: String): Review {
        // pause each process until callback is received
        return suspendCoroutine { process ->
            try {
                YelpHelper.callYelpReviewAPI(businessId) { successResponse: ReviewResponse? ->
                    if (successResponse != null) {
                        val reviewResponse = successResponse.reviews
                        val review = if (reviewResponse.isNotEmpty()) reviewResponse[0] else Review("")
                        process.resume(review)
                    } else {
                        Log.d(TAG, "getBusinessTopReview: Callback response is null")
                        process.resume(Review(""))
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "getBusinessTopReview: Exception: ${e.message}")
                process.resume(Review(""))
            }
        }
    }
}
