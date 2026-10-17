package com.surgicalboardmaster.app.feature.search

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SearchResultItem(
    val id: String,
    val title: String,
    val snippet: String,
    val sourceCategory: String
)

@HiltViewModel
class GlobalSearchViewModel @Inject constructor() : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResultItem>>(emptyList())
    val searchResults: StateFlow<List<SearchResultItem>> = _searchResults.asStateFlow()

    fun updateQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        // Mock comprehensive search across workspace data
        val sampleIndex = listOf(
            SearchResultItem("1", "Mirizzi Syndrome (Csendes Classification)", "Extrinsic CHD compression by impacted stone in Hartmann's pouch...", "Bailey & Love Library"),
            SearchResultItem("2", "Strasberg Critical View of Safety", "1. Clear hepatocystic triangle. 2. Lower third of gallbladder dissected. 3. Two structures entering...", "AI Lesson Cache"),
            SearchResultItem("3", "Acute Calculous Cholangitis", "Charcot's triad (pain, fever, jaundice) + Reynolds' pentad = septic shock...", "Exam Center Question Bank"),
            SearchResultItem("4", "SBM-2026-HPB-014", "Acute Calculous Cholangitis secondary to distal CBD stone • ERCP stenting", "Private Clinical Case Log")
        )

        _searchResults.value = sampleIndex.filter {
            it.title.contains(query, ignoreCase = true) || it.snippet.contains(query, ignoreCase = true)
        }
    }
}
