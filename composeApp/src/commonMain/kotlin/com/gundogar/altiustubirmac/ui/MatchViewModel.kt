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

/**
 * Sealed class ensures exhaustive `when` expressions at compile time.
 * Adding a new state forces all consumers to handle it, preventing runtime crashes.
 */
sealed class MatchUiState {
    data object Loading : MatchUiState()
    data class Success(val matches: List<MatchUiModel>) : MatchUiState()
    data class Error(val message: String) : MatchUiState()
}

/**
 * Uses constructor injection instead of Koin's viewModelOf() because viewModelOf()
 * has IrLinkageError issues on WASM due to ViewModelStoreOwner linkage problems.
 * factoryOf() with koinInject() works across all platforms (Android, iOS, Web).
 */
class MatchViewModel(
    private val repository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MatchUiState>(MatchUiState.Loading)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    /**
     * Search filtering is done via [combine] instead of re-fetching from API.
     * This provides instant filtering without network latency and reduces API load.
     *
     * WhileSubscribed(5000) keeps the flow active for 5 seconds after the last subscriber
     * disconnects, surviving configuration changes without re-computation.
     */
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
                 * CancellationException must be rethrown - swallowing it breaks structured
                 * concurrency and can cause coroutine leaks when ViewModel is cleared.
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
