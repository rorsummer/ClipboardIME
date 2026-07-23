package com.clipboardime.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clipboardime.data.ClipboardEntity
import com.clipboardime.data.ClipboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: ClipboardRepository
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<ClipboardEntity>>(emptyList())
    val searchResults: StateFlow<List<ClipboardEntity>> = _searchResults

    private val _historyCount = MutableStateFlow(0)
    val historyCount: StateFlow<Int> = _historyCount

    fun search(keyword: String) {
        viewModelScope.launch {
            if (keyword.isBlank()) {
                repository.getAll().collectLatest { items ->
                    _searchResults.value = items
                }
            } else {
                repository.searchByKeyword(keyword.trim()).collectLatest { items ->
                    _searchResults.value = items
                }
            }
        }
    }

    fun loadHistoryCount() {
        viewModelScope.launch {
            repository.getAll().collectLatest { items ->
                _historyCount.value = items.size
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }
}
