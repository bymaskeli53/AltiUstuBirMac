package com.gundogar.altiustubirmac.ui

import com.gundogar.altiustubirmac.data.AppException
import com.gundogar.altiustubirmac.data.MatchRepository
import com.gundogar.altiustubirmac.data.MatchUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class MatchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()


    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        val repository = FakeMatchRepository()
        val viewModel = MatchViewModel(repository)

        // Start collecting to activate the StateFlow
        backgroundScope.launch { viewModel.uiState.collect {} }

        assertIs<MatchUiState.Loading>(viewModel.uiState.value)
    }

    @Test
    fun `state changes to Success when repository returns matches`() = runTest {
        val matches = listOf(
            MatchUiModel(
                id = 1L,
                homeTeam = "Fenerbahce",
                awayTeam = "Galatasaray",
                underOdd = "1.50",
                overOdd = "2.30"
            )
        )
        val repository = FakeMatchRepository(matches = matches)
        val viewModel = MatchViewModel(repository)

        // Start collecting to activate the StateFlow
        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<MatchUiState.Success>(state)
        assertEquals(1, state.matches.size)
        assertEquals("Fenerbahce", state.matches[0].homeTeam)
    }

    @Test
    fun `state changes to Error when repository returns empty list`() = runTest {
        val repository = FakeMatchRepository(matches = emptyList())
        val viewModel = MatchViewModel(repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<MatchUiState.Error>(state)
        assertEquals(AppException.EmptyData.message, state.message)
    }

    @Test
    fun `state changes to Error when repository throws exception`() = runTest {
        val repository = FakeMatchRepository(exception = RuntimeException("Network error"))
        val viewModel = MatchViewModel(repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<MatchUiState.Error>(state)
    }

    @Test
    fun `loadMatches transitions through Loading to Success`() = runTest {
        val matches = listOf(
            MatchUiModel(
                id = 1L,
                homeTeam = "Besiktas",
                awayTeam = "Trabzonspor",
                underOdd = "1.80",
                overOdd = "1.90"
            )
        )
        val repository = FakeMatchRepository(matches = matches)
        val viewModel = MatchViewModel(repository)

        val states = mutableListOf<MatchUiState>()
        backgroundScope.launch { viewModel.uiState.collect { states.add(it) } }
        advanceUntilIdle()

        // Clear collected states and call loadMatches again
        states.clear()
        viewModel.loadMatches()
        advanceUntilIdle()

        // Should have collected Loading then Success
        assertEquals(true, states.any { it is MatchUiState.Loading })
        assertEquals(true, states.any { it is MatchUiState.Success })
    }

    @Test
    fun `refresh sets isRefreshing to true then false`() = runTest {
        val matches = listOf(
            MatchUiModel(
                id = 1L,
                homeTeam = "Team A",
                awayTeam = "Team B",
                underOdd = "1.50",
                overOdd = "2.50"
            )
        )
        val repository = FakeMatchRepository(matches = matches)
        val viewModel = MatchViewModel(repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertEquals(false, viewModel.isRefreshing.value)

        viewModel.refresh()
        assertEquals(true, viewModel.isRefreshing.value)

        advanceUntilIdle()

        assertEquals(false, viewModel.isRefreshing.value)
    }

    @Test
    fun `search filters matches by team name`() = runTest {
        val matches = listOf(
            MatchUiModel(
                id = 1L,
                homeTeam = "Fenerbahce",
                awayTeam = "Galatasaray",
                underOdd = "1.50",
                overOdd = "2.30"
            ),
            MatchUiModel(
                id = 2L,
                homeTeam = "Besiktas",
                awayTeam = "Trabzonspor",
                underOdd = "1.80",
                overOdd = "1.90"
            )
        )
        val repository = FakeMatchRepository(matches = matches)
        val viewModel = MatchViewModel(repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Fener")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<MatchUiState.Success>(state)
        assertEquals(1, state.matches.size)
        assertEquals("Fenerbahce", state.matches[0].homeTeam)
    }

    @Test
    fun `search is case insensitive`() = runTest {
        val matches = listOf(
            MatchUiModel(
                id = 1L,
                homeTeam = "Fenerbahce",
                awayTeam = "Galatasaray",
                underOdd = "1.50",
                overOdd = "2.30"
            )
        )
        val repository = FakeMatchRepository(matches = matches)
        val viewModel = MatchViewModel(repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onSearchQueryChange("GALATASARAY")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<MatchUiState.Success>(state)
        assertEquals(1, state.matches.size)
    }

    @Test
    fun `clearing search shows all matches`() = runTest {
        val matches = listOf(
            MatchUiModel(
                id = 1L,
                homeTeam = "Fenerbahce",
                awayTeam = "Galatasaray",
                underOdd = "1.50",
                overOdd = "2.30"
            ),
            MatchUiModel(
                id = 2L,
                homeTeam = "Besiktas",
                awayTeam = "Trabzonspor",
                underOdd = "1.80",
                overOdd = "1.90"
            )
        )
        val repository = FakeMatchRepository(matches = matches)
        val viewModel = MatchViewModel(repository)

        backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onSearchQueryChange("Fener")
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertIs<MatchUiState.Success>(state)
        assertEquals(1, state.matches.size)

        viewModel.onSearchQueryChange("")
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertIs<MatchUiState.Success>(state)
        assertEquals(2, state.matches.size)
    }
}

/**
 * Fake repository for testing ViewModel without real network calls.
 */
class FakeMatchRepository(
    private val matches: List<MatchUiModel> = emptyList(),
    private val exception: Exception? = null
) : MatchRepository {

    override suspend fun fetchMatches(): List<MatchUiModel> {
        if (exception != null) {
            throw exception
        }
        return matches
    }
}
