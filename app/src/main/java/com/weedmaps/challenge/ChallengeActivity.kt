package com.weedmaps.challenge

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.weedmaps.challenge.ui.theme.ChallengeActivityTheme
import com.weedmaps.challenge.ui.view.YelpBusinessesUI
import com.weedmaps.challenge.ui.viewmodel.YelpViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChallengeActivity : ComponentActivity() {

    private val viewModel: YelpViewModel by viewModels()

    private val TAG = this::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get location manager
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "onCreate: Missing permissions to retrieve last known location")
            return
        }
        val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val currentLatitude = lastKnownLocation?.latitude ?: 0.0
        val currentLongitude = lastKnownLocation?.longitude ?: 0.0

        setContent {
            ChallengeActivityTheme {
                YelpBusinessesUI(
                    businessList = viewModel.businessListWithReviewsState.value ?: emptyList(),
                    searchQuery = viewModel.searchQueryState.value ?: "",
                    locationValue = viewModel.locationValueState.value ?: "",
                    isLoading = viewModel.isLoadingState.value ?: false,
                    isSearching = viewModel.isSearchingState.value ?: false,
                    onSearch = { searchTerm, location ->
                        viewModel.updateSearchQuery(searchTerm)
                        viewModel.updateLocationValue(location)
                        viewModel.loadBusinessListWithReviews(currentLatitude, currentLongitude, location, searchTerm)
                    },
                    onScrollToEnd = { searchTerm, location ->
                        viewModel.updateSearchQuery(searchTerm)
                        viewModel.updateLocationValue(location)
                        viewModel.loadMoreBusinessListWithReviews(currentLatitude, currentLongitude, location, searchTerm)
                    }
                )
            }
        }

        // Initial load
        viewModel.loadBusinessListWithReviews(currentLatitude, currentLongitude, viewModel.locationValueState.value ?: "", viewModel.searchQueryState.value ?: "")
    }
}
