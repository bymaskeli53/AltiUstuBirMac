package com.gundogar.altiustubirmac.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlin.math.roundToLong

private fun formatOdd(value: Double): String {
    val rounded = (value * 100).roundToLong()
    val whole = rounded / 100
    val frac = (rounded % 100).toString().padStart(2, '0')
    return "$whole.$frac"
}

class MatchRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun fetchMatches(): List<MatchUiModel> = withContext(Dispatchers.Default) {
        val response: ApiResponse = client.get(
            "https://sportsbookv2.iddaa.com/sportsbook/events?st=1&type=0&version=0"
        ).body()

        response.data.events.mapNotNull { event ->
            val market = event.m.firstOrNull { it.t == 2 && it.st == 101 && it.sov == "2.5" }
                ?: return@mapNotNull null

            val underOdd = market.o.firstOrNull { it.no == 1 }?.wodd ?: return@mapNotNull null
            val overOdd = market.o.firstOrNull { it.no == 2 }?.wodd ?: return@mapNotNull null

            MatchUiModel(
                id = event.i,
                homeTeam = event.hn,
                awayTeam = event.an,
                underOdd = formatOdd(underOdd),
                overOdd = formatOdd(overOdd)
            )
        }
    }
}
