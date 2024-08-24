package com.weedmaps.challenge.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import com.weedmaps.challenge.ui.viewmodel.SuggestionsHelper.addStringToSuggestions
import com.weedmaps.challenge.ui.viewmodel.SuggestionsHelper.clearSuggestions
import com.weedmaps.challenge.ui.viewmodel.SuggestionsHelper.getSuggestionsFromPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTextField(
    searchQuery: String,
    locationValue: String,
    onSearch: (String, String) -> Unit,
    scrollState: LazyListState,
    scrollToTop: Boolean,
) {
    var searchQueryState by remember { mutableStateOf(searchQuery) }
    var hasInteracted by remember { mutableStateOf(false) }
    val scrollToTopState = remember { mutableStateOf(scrollToTop) }
    var expanded by remember { mutableStateOf(false) }
    var suggestions by remember { mutableStateOf(listOf<String>()) }
    var filteredSuggestions = remember(searchQueryState) {
        suggestions.filter {
            it.contains(searchQueryState, ignoreCase = true) && searchQueryState.isNotEmpty()
        }
    }
    var locationDialogShown by remember { mutableStateOf(false) }
    var locationValueState by remember { mutableStateOf(locationValue) }
    var tempLocationValue by remember { mutableStateOf(locationValue) }
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        suggestions = getSuggestionsFromPreferences(context)
        filteredSuggestions = suggestions.filter { suggestion ->
            suggestion.contains(searchQueryState, ignoreCase = true) && searchQueryState.isNotEmpty()
        }
    }
    if (locationDialogShown) {
        AlertDialog(
            onDismissRequest = {
                locationValueState = tempLocationValue
                locationDialogShown = false },
            title = { Text("Enter Your Location") },
            confirmButton = {
                Button(onClick = {
                    locationDialogShown = false
                    scrollToTopState.value = true
                    tempLocationValue = locationValueState
                    onSearch(searchQueryState, locationValueState)}) {
                    Text("Confirm/Save")
                }
            },
            dismissButton = {
                Button(onClick = {
                    locationDialogShown = false
                    locationValueState = tempLocationValue}) {
                    Text("Cancel")
                }
            },
            text = {
                TextField(
                    maxLines = 1,
                    value = locationValueState,
                    onValueChange = { locationValueState = it },
                    placeholder = { Text("Enter your location... (EX. near me, New York City, NYC, 350 5th Ave, New York, NY 10118" ) }
                )
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        TextField(
            colors = TextFieldDefaults.textFieldColors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                containerColor = MaterialTheme.colorScheme.background
            ),
            shape = RoundedCornerShape(16.dp),
            value = if (hasInteracted) searchQueryState else "",
            onValueChange = {
                searchQueryState = it
                hasInteracted = true
                expanded = filteredSuggestions.isNotEmpty() && searchQueryState.isNotEmpty()
            },
            keyboardActions = KeyboardActions(
                onDone = {
                    scrollToTopState.value = true
                    onSearch(searchQueryState, locationValueState)
                    expanded = false
                    suggestions = addStringToSuggestions(context, searchQueryState)
                    keyboardController?.hide()
                }
            ),
            placeholder = {
                Text(
                    "Search",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
            singleLine = true,
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    "searchIcon"
                )
            },
            trailingIcon = {
                Icon(
                    Icons.Filled.LocationOn,
                    "locationIcon",
                    modifier = Modifier.clickable {
                        locationDialogShown = true
                    }
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            properties = PopupProperties(
                focusable = false
            ),
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            filteredSuggestions.forEach {suggestion ->
                DropdownMenuItem(text = { Text(suggestion) },
                    onClick = {
                        searchQueryState = suggestion
                        onSearch(suggestion, locationValueState)
                        expanded = false
                        keyboardController?.hide()
                        scrollToTopState.value = true
                    })

            }

            DropdownMenuItem(
                text = { Text(
                    text="Clear Suggestions",
                    fontWeight = FontWeight.Bold,
                    color = Color.Red ) },
                onClick = {
                    clearSuggestions(context)
                    suggestions = listOf()
                    filteredSuggestions = listOf()
                    expanded = false
                    keyboardController?.hide()
                }
            )
        }

        Text(
            "Showing results for ${if (locationValueState.lowercase() == "near me" || locationValueState.isEmpty()) "$searchQueryState near you"
                else if (locationValueState.isNotEmpty()) "$searchQueryState near $locationValueState"
                else "food near you"}",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(horizontal = 18.dp)
                .padding(top = 4.dp, bottom = 6.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        LaunchedEffect(scrollToTopState.value) {
            if (scrollToTopState.value) {
                scrollState.scrollToItem(index = 0)
                scrollToTopState.value = false
            }
        }
    }
}
