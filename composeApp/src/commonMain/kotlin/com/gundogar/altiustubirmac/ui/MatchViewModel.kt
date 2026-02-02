package com.gundogar.altiustubirmac.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gundogar.altiustubirmac.data.AppException
import com.gundogar.altiustubirmac.data.MatchRepository
import com.gundogar.altiustubirmac.data.MatchUiModel
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MatchUiState {
    data object Loading : MatchUiState()
    data class Success(val matches: List<MatchUiModel>) : MatchUiState()
    data class Error(val message: String) : MatchUiState()
}

class MatchViewModel : ViewModel() {

    private val repository = MatchRepository()

    private val _uiState = MutableStateFlow<MatchUiState>(MatchUiState.Loading)
    val uiState: StateFlow<MatchUiState> = _uiState

    private val _shouldShowInfoMessage = MutableStateFlow(true)
    val shouldShowInfoMessage: StateFlow<Boolean> = _shouldShowInfoMessage

    fun infoMessageShown() {
        _shouldShowInfoMessage.value = false
    }

    init {
        loadMatches()
    }

    fun loadMatches() {
        _uiState.value = MatchUiState.Loading
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
            }
        }
    }
}
