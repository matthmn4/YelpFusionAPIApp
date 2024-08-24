package com.weedmaps.challenge.ui.viewmodel

import android.content.Context

object SuggestionsHelper {
    fun addStringToSuggestions(context: Context, newString: String): List<String> {
        val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        val set: MutableSet<String> = sharedPreferences.getStringSet("user_inputs", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        set.add(newString)
        with(sharedPreferences.edit()) {
            putStringSet("user_inputs", set)
            apply()
        }

        return set.toList()
    }

    fun getSuggestionsFromPreferences(context: Context): List<String> {
        val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet("user_inputs", mutableSetOf())?.toList() ?: listOf()
    }

    fun clearSuggestions(context: Context) {
        val sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("user_inputs")
            apply()
        }
    }

}