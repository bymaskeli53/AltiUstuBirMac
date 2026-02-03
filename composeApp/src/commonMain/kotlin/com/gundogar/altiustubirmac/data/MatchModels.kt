package com.gundogar.altiustubirmac.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * API response models use single-letter field names (i, hn, an, m, t, st, etc.)
 * because that's how iddaa API returns them. Default values are necessary because
 * the API may omit fields, and we use `ignoreUnknownKeys = true` in Json config.
 */

@Serializable
data class ApiResponse(
    val data: EventData
)

@Serializable
data class EventData(
    val events: List<Event>
)

@Serializable
data class Event(
    val i: Long,          // Event ID
    val hn: String,       // Home team name
    val an: String,       // Away team name
    val d: Long = 0L,     // Date timestamp
    val m: List<Market> = emptyList()  // Markets
)

@Serializable
data class Market(
    val t: Int = 0,       // Market type (2 = pre-match)
    val st: Int = 0,      // Subtype (101 = over/under)
    val sov: String = "", // Threshold value ("2.5")
    val o: List<Outcome> = emptyList()  // Outcomes
)

@Serializable
data class Outcome(
    val no: Int = 0,       // Outcome number (1 = under, 2 = over)
    val odd: Double = 0.0, // Base odd
    val wodd: Double = 0.0,// Weighted odd (displayed to user)
    val n: String = ""     // Name
)

/**
 * Presentation model decouples UI from API structure.
 * Pre-formatted strings avoid repeated formatting in Compose recompositions.
 */
data class MatchUiModel(
    val id: Long,
    val homeTeam: String,
    val awayTeam: String,
    val underOdd: String,
    val overOdd: String
)
