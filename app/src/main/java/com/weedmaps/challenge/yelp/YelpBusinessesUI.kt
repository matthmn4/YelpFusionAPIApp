package com.weedmaps.challenge.yelp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun YelpBusinessesUI(
    businessList: List<BusinessWithReview>,
    searchQuery: String,
    locationValue: String,
    isLoading: Boolean,
    isSearching: Boolean,
    onSearch: (String, String) -> Unit,
    onScrollToEnd: (String, String) -> Unit,
) {

    val scrollState = rememberLazyListState()
    val scrollToTop by remember { mutableStateOf(false) }

    val reachedBottom: Boolean by remember {
        derivedStateOf {
            val lastVisibleItem = scrollState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index != 0 && lastVisibleItem?.index == scrollState.layoutInfo.totalItemsCount - 1
        }
    }

    LaunchedEffect(reachedBottom) {
        if (reachedBottom && !isLoading) {
            onScrollToEnd(searchQuery, locationValue)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SearchTextField(
            searchQuery = searchQuery,
            locationValue = locationValue,
            onSearch = onSearch,
            scrollState = scrollState,
            scrollToTop = scrollToTop
        )

        if (isLoading && isSearching) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (isLoading) {
            CircularProgressIndicator(modifier = Modifier
                .padding(bottom = 8.dp)
                .align(Alignment.CenterHorizontally))
            LazyColumn(
                state = scrollState
            ) {
                itemsIndexed(
                    items = businessList,
                    key = { _, item -> item.business.id }
                ) { index, businessWithReview ->
                    BusinessCard(businessWithReview)
                }
            }
        } else {
            LazyColumn(
                state = scrollState
            ) {
                itemsIndexed(
                    items = businessList,
                    key = { _, item -> item.business.id }
                ) { index, businessWithReview ->
                    BusinessCard(businessWithReview)
                }
            }
        }
    }
}
