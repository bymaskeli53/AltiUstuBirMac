package com.gundogar.altiustubirmac.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gundogar.altiustubirmac.data.AppException
import com.gundogar.altiustubirmac.data.MatchRepository
import com.gundogar.altiustubirmac.data.MatchUiModel
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class MatchUiState {
    data object Loading : MatchUiState()
    data class Success(val matches: List<MatchUiModel>) : MatchUiState()
    data class Error(val message: String) : MatchUiState()
}

class MatchViewModel : ViewModel() {

    private val repository = MatchRepository()

    private val _uiState = MutableStateFlow<MatchUiState>(MatchUiState.Loading)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val uiState: StateFlow<MatchUiState> = combine(_uiState, _searchQuery) { state, query ->
        when (state) {
            is MatchUiState.Success -> {
                if (query.isBlank()) {
                    state
                } else {
                    val filtered = state.matches.filter { match ->
                        match.homeTeam.contains(query, ignoreCase = true) ||
                            match.awayTeam.contains(query, ignoreCase = true)
                    }
                    state.copy(matches = filtered)
                }
            }
            else -> state
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MatchUiState.Loading)

    private val _shouldShowInfoMessage = MutableStateFlow(true)
    val shouldShowInfoMessage: StateFlow<Boolean> = _shouldShowInfoMessage

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun infoMessageShown() {
        _shouldShowInfoMessage.value = false
    }

    init {
        loadMatches()
    }

    fun loadMatches() {
        _uiState.value = MatchUiState.Loading
        fetchData()
    }

    fun refresh() {
        _isRefreshing.value = true
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            try {
                val matches = repository.fetchMatches()
                if (matches.isEmpty()) {
                    _uiState.value = MatchUiState.Error(AppException.EmptyData.message)
                } else {
                    _uiState.value = MatchUiState.Success(matches)
                }
                /**
                 * Cancellation exception should rethrow again
                 */
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = MatchUiState.Error(AppException.from(e).message)
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
