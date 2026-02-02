package com.gundogar.altiustubirmac.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val i: Long,
    val hn: String,
    val an: String,
    val d: Long = 0L,
    val m: List<Market> = emptyList()
)

@Serializable
data class Market(
    val t: Int = 0,
    val st: Int = 0,
    val sov: String = "",
    val o: List<Outcome> = emptyList()
)

@Serializable
data class Outcome(
    val no: Int = 0,
    val odd: Double = 0.0,
    val wodd: Double = 0.0,
    val n: String = ""
)

data class MatchUiModel(
    val id: Long,
    val homeTeam: String,
    val awayTeam: String,
    val underOdd: String,
    val overOdd: String
)
