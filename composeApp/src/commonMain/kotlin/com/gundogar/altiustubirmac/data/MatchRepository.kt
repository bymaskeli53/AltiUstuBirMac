package com.gundogar.altiustubirmac.data

/**
 * Interface enables constructor injection via Koin and allows swapping implementations
 * for testing (fake/mock) without modifying ViewModel code.
 */
interface MatchRepository {
    suspend fun fetchMatches(): List<MatchUiModel>
}
