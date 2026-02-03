package com.gundogar.altiustubirmac.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gundogar.altiustubirmac.data.MatchUiModel
import com.gundogar.altiustubirmac.openUrl
import org.koin.compose.koinInject

/**
 * koinInject() is used instead of koinViewModel() for cross-platform compatibility.
 * koinViewModel() has IrLinkageError issues on WASM.
 */


private val DarkColors = darkColorScheme()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(viewModel: MatchViewModel = koinInject()) {
    MaterialTheme(colorScheme = DarkColors) {
        val uiState by viewModel.uiState.collectAsState()
        val shouldShowInfo by viewModel.shouldShowInfoMessage.collectAsState()
        val isRefreshing by viewModel.isRefreshing.collectAsState()
        val searchQuery by viewModel.searchQuery.collectAsState()
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(shouldShowInfo) {
            if (shouldShowInfo) {
                val result = snackbarHostState.showSnackbar(
                    message = "Bu uygulamada sadece 2.5 Alt/Ust goruntuleyebilirsiniz.",
                    actionLabel = "Misliye Git",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    openUrl("https://www.misli.com/iddaa/futbol")
                }
                viewModel.infoMessageShown()
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .safeContentPadding()
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                Text(
                    text = "2.5 Alt/Ust Oranlari",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(16.dp)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Takim ara...") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Text("X", fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                when (val state = uiState) {
                    is MatchUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is MatchUiState.Error -> {
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(16.dp)
                            )
                            Button(onClick = { viewModel.loadMatches() }) {
                                Text("Tekrar Dene")
                            }
                        }
                    }

                    is MatchUiState.Success -> {
                        Text(
                            text = "${state.matches.size} mac bulundu",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        PullToRefreshBox(
                            isRefreshing = isRefreshing,
                            onRefresh = { viewModel.refresh() },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(Modifier.fillMaxSize()) {
                                items(
                                    items = state.matches,
                                    key = { it.id }
                                ) { match ->
                                    MatchRow(match)
                                    HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.5f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatchRow(match: MatchUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${match.homeTeam} - ${match.awayTeam}",
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(end = 8.dp)
        )

        OddBox(label = "Alt", value = match.underOdd, color = Color(0xFF2196F3))
        OddBox(label = "Ust", value = match.overOdd, color = Color(0xFFFF9800))
    }
}

@Composable
private fun OddBox(label: String, value: String, color: Color) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .width(64.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = color,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                fontSize = 13.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}