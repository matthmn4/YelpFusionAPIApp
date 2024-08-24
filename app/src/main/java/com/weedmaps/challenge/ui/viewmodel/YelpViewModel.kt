package com.weedmaps.challenge.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weedmaps.challenge.data.models.Business
import com.weedmaps.challenge.data.models.BusinessWithReview
import com.weedmaps.challenge.data.models.Review
import com.weedmaps.challenge.data.models.SearchResponse
import com.weedmaps.challenge.data.respository.YelpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@HiltViewModel
class YelpViewModel @Inject constructor(
    private val yelpRepository: YelpRepository
) : ViewModel() {

    private val TAG = this::class.simpleName

    // Private mutable state
    private val _businessListWithReviewsState = MutableLiveData<List<BusinessWithReview>>(emptyList())
    val businessListWithReviewsState: LiveData<List<BusinessWithReview>> get() = _businessListWithReviewsState

    private val _searchQueryState = MutableLiveData("food")
    val searchQueryState: LiveData<String> get() = _searchQueryState

    private val _locationValueState = MutableLiveData("")
    val locationValueState: LiveData<String> get() = _locationValueState

    private val _isLoadingState = MutableLiveData(false)
    val isLoadingState: LiveData<Boolean> get() = _isLoadingState

    private val _isSearchingState = MutableLiveData(false)
    val isSearchingState: LiveData<Boolean> get() = _isSearchingState

    private val _outOfBusinessesToLoad = MutableLiveData(false)
    val outOfBusinessesToLoad: LiveData<Boolean> get() = _outOfBusinessesToLoad

    private var offset = 0

    private val businessList = mutableListOf<Business>()
    private val businessListWithReviews = mutableListOf<BusinessWithReview>()

    private var searchJob: Job? = null

    // Public methods to interact with ViewModel state
    fun updateSearchQuery(searchTerm: String) {
        _searchQueryState.value = searchTerm
    }

    fun updateLocationValue(location: String) {
        _locationValueState.value = location
    }

    fun loadBusinessListWithReviews(latitude: Double, longitude: Double, location: String, searchTerm: String) {
        _isLoadingState.value = true
        _isSearchingState.value = true
        offset = 0
        Log.d(TAG, "loadBusinessListWithReviews: limit offset is currently $offset, location is $location")

        viewModelScope.launch(Dispatchers.IO) {
            if (location.isNotEmpty() && location.lowercase() == "near me") {
                getBusinessListWithReviews(latitude, longitude, searchTerm, 10)
            } else if (location.isNotEmpty()) {
                getLocationBusinessListWithReviews(location, searchTerm, 10)
            } else {
                getBusinessListWithReviews(latitude, longitude, searchTerm, 10)
            }
        }
    }

    fun loadMoreBusinessListWithReviews(latitude: Double, longitude: Double, location: String, searchTerm: String) {
        if (_outOfBusinessesToLoad.value == true) {
            Log.d(TAG, "loadMoreBusinessListWithReviews: outOfBusinessesToLoad")
            _isLoadingState.value = false
            return
        }
        if (_isLoadingState.value == true || searchJob?.isActive == true) {
            Log.d(TAG, "loadMoreBusinessListWithReviews: already loading a searchRequest")
            return
        }
        _isLoadingState.value = true
        offset += 10
        Log.d(TAG, "loadMoreBusinessListWithReviews: limit offset is currently $offset, location is $location")

        viewModelScope.launch(Dispatchers.IO) {
            if (location.isNotEmpty() && location.lowercase() == "near me") {
                getBusinessListWithReviews(latitude, longitude, searchTerm, 10 + offset)
            } else if (location.isNotEmpty()) {
                getLocationBusinessListWithReviews(location, searchTerm, 10 + offset)
            } else {
                getBusinessListWithReviews(latitude, longitude, searchTerm, 10 + offset)
            }
        }
    }

    private fun getLocationBusinessListWithReviews(location: String, searchTerm: String, limit: Int) {
        if (_isLoadingState.value == true) {
            searchJob?.cancel()
        }

        yelpRepository.callYelpSearchAPI(location, searchTerm, limit) { successResponse ->
            processBusinessListResponse(successResponse, limit)
        }
    }

    private fun getBusinessListWithReviews(latitude: Double, longitude: Double, searchTerm: String, limit: Int) {
        if (_isLoadingState.value == true) {
            searchJob?.cancel()
        }

        yelpRepository.callYelpSearchAPI(latitude, longitude, searchTerm, limit) { successResponse ->
            processBusinessListResponse(successResponse, limit)
        }
    }

    private fun processBusinessListResponse(successResponse: SearchResponse?, limit: Int) {
        if (successResponse != null) {
            if (offset == 0) {
                Log.d(TAG, "getBusinessListWithReviews: cleared business lists")
                businessList.clear()
                businessListWithReviews.clear()
            }
            var limitChecked = limit
            if (successResponse.businesses.size < limit) {
                Log.d(TAG, "Response is less than the current limit of $limit ")
                limitChecked = successResponse.businesses.size - offset
            }

            if (successResponse.businesses.size == businessList.size) {
                _isLoadingState.postValue(false)
                _outOfBusinessesToLoad.postValue(true)
            } else {
                Log.d(TAG, "getBusinessListWithReviews: offset is $offset, limitchecked is $limitChecked")
                if (limitChecked % 10 != 0) {
                    limitChecked += offset
                }
                businessList.addAll(
                    successResponse.businesses.subList(offset, limitChecked)
                )
                Log.d(TAG, "business list after sublist from offset to size: ${businessList.size}")

                searchJob = viewModelScope.launch(Dispatchers.IO) {
                    businessList.subList(offset, limitChecked).forEach { business ->
                        delay(200)
                        val topReview = getBusinessTopReview(business.id)
                        businessListWithReviews.add(BusinessWithReview(business, topReview))
                    }

                    withContext(Dispatchers.Main) {
                        _isLoadingState.value = false
                        _isSearchingState.value = false
                        _businessListWithReviewsState.value = businessListWithReviews
                    }
                }
            }
        } else {
            Log.d(TAG, "getBusinessListWithReviews callback response is null")
        }
    }

    private suspend fun getBusinessTopReview(businessId: String): Review {
        return suspendCoroutine { process ->
            try {
                yelpRepository.callYelpReviewAPI(businessId) { successResponse ->
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