package com.gundogar.altiustubirmac.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToLong

const val UNDER_ODD = 1
const val OVER_ODD = 2

const val TYPE_NUMBER = 2

const val SUB_TYPE_NUMBER = 101

class MatchRepositoryImpl(
    private val client: HttpClient
) : MatchRepository {

    /**
     * Dispatchers.Default is used because JSON parsing is CPU-bound work.
     * Ktor's network call internally uses appropriate IO dispatcher.
     */
    override suspend fun fetchMatches(): List<MatchUiModel> = withContext(Dispatchers.Default) {
        val response: ApiResponse = client.get(
            "https://sportsbookv2.iddaa.com/sportsbook/events?st=1&type=0&version=0"
        ).body()

        filterMatchData(response)

    }

    private fun filterMatchData(response: ApiResponse): List<MatchUiModel> {
        return response.data.events.mapNotNull { event ->
            /**
             * Filter criteria from iddaa API:
             * - t=2: Pre-match market type
             * - st=101: Over/Under market subtype
             * - sov="2.5": The 2.5 goal threshold (app's core feature)
             */
            val market =
                event.m.firstOrNull { it.t == TYPE_NUMBER && it.st == SUB_TYPE_NUMBER && it.sov == "2.5" }
                    ?: return@mapNotNull null

            val underOdd =
                market.o.firstOrNull { it.no == UNDER_ODD }?.wodd ?: return@mapNotNull null
            val overOdd = market.o.firstOrNull { it.no == OVER_ODD }?.wodd ?: return@mapNotNull null

            MatchUiModel(
                id = event.i,
                homeTeam = event.hn,
                awayTeam = event.an,
                underOdd = formatOdd(underOdd),
                overOdd = formatOdd(overOdd)
            )
        }
    }

    /**
     * Manual formatting because String.format() is not available in Kotlin common code.
     * Ensures consistent 2 decimal places across all platforms.
     */
    private fun formatOdd(value: Double): String {
        val rounded = (value * 100).roundToLong()
        val whole = rounded / 100
        val frac = (rounded % 100).toString().padStart(2, '0')
        return "$whole.$frac"
    }
}
