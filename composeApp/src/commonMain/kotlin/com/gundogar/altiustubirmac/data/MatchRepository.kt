package com.gundogar.altiustubirmac.data

interface MatchRepository {
    suspend fun fetchMatches(): List<MatchUiModel>
}
