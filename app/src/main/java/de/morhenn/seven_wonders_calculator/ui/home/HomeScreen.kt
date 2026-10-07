package de.morhenn.seven_wonders_calculator.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.morhenn.seven_wonders_calculator.R
import de.morhenn.seven_wonders_calculator.container
import de.morhenn.seven_wonders_calculator.data.GameEntity
import de.morhenn.seven_wonders_calculator.data.GameRepository
import de.morhenn.seven_wonders_calculator.ui.components.CompositionBar
import de.morhenn.seven_wonders_calculator.ui.components.GameIcons
import de.morhenn.seven_wonders_calculator.ui.components.MeanderBand
import de.morhenn.seven_wonders_calculator.ui.components.PlayerAvatar
import de.morhenn.seven_wonders_calculator.ui.components.RingProgress
import de.morhenn.seven_wonders_calculator.ui.components.SectionHeader
import de.morhenn.seven_wonders_calculator.ui.components.SkylineBackdrop
import de.morhenn.seven_wonders_calculator.ui.components.HeroStatusBar
import de.morhenn.seven_wonders_calculator.ui.components.formatScore
import de.morhenn.seven_wonders_calculator.ui.formatDate
import de.morhenn.seven_wonders_calculator.ui.theme.Brand
import de.morhenn.seven_wonders_calculator.ui.theme.OverlineStyle
import de.morhenn.seven_wonders_calculator.ui.theme.ScoreStyle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class HomeUiState(val loading: Boolean = true, val ongoing: List<GameEntity> = emptyList(), val finished: List<GameEntity> = emptyList())

class HomeViewModel(private val games: GameRepository) : ViewModel() {
    val state: StateFlow<HomeUiState> = games.games.map { all ->
        HomeUiState(
            loading = false,
            ongoing = all.filter { it.finishedAt == null },
            finished = all.filter { it.finishedAt != null },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun delete(id: Long) {
        viewModelScope.launch { games.delete(id) }
    }

    companion object {
        val Factory = viewModelFactory { initializer { HomeViewModel(container.games) } }
    }
}

@Composable
fun HomeScreen(
    onNewGame: () -> Unit,
    onOpenGame: (id: Long, finished: Boolean) -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDiscard by remember { mutableStateOf<GameEntity?>(null) }
    val listState = rememberLazyListState()
    val heroVisible by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    // Surface provides the theme's content color; without it, plain texts default to black.
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Box(Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "hero") { Hero(onNewGame, onStats, onSettings) }
        items(state.ongoing, key = { "o${it.id}" }) { game ->
            OngoingGameCard(
                game,
                onContinue = { onOpenGame(game.id, false) },
                onDiscard = { confirmDiscard = game },
                modifier = Modifier.padding(horizontal = 16.dp).animateItem(),
            )
        }
        if (state.finished.isNotEmpty()) {
            item(key = "recent") {
                SectionHeader(stringResource(R.string.recent_games), Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp))
            }
            items(state.finished, key = { "f${it.id}" }) { game ->
                FinishedGameCard(game, onClick = { onOpenGame(game.id, true) }, modifier = Modifier.padding(horizontal = 16.dp).animateItem())
            }
        } else if (!state.loading && state.ongoing.isEmpty()) {
            item(key = "empty") { EmptyHome() }
        }
        item(key = "bottom") { Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars).height(24.dp)) }
    }
        // The hero is a night sky; its status bar icons are light in both themes.
        HeroStatusBar(heroVisible)
    }
    }

    confirmDiscard?.let { game ->
        AlertDialog(
            onDismissRequest = { confirmDiscard = null },
            title = { Text(stringResource(R.string.discard_game_title)) },
            text = { Text(stringResource(R.string.discard_game_text)) },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(game.id); confirmDiscard = null }) {
                    Text(stringResource(R.string.discard))
                }
            },
            dismissButton = { TextButton(onClick = { confirmDiscard = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun Hero(onNewGame: () -> Unit, onStats: () -> Unit, onSettings: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        SkylineBackdrop(Modifier.matchParentSize())
        Column(Modifier.statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onStats) { Icon(Icons.Filled.Insights, stringResource(R.string.stats_title), tint = Color.White) }
                IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, stringResource(R.string.settings_title), tint = Color.White) }
            }
            Column(Modifier.padding(horizontal = 24.dp)) {
                Text(
                    stringResource(R.string.hero_overline).uppercase(),
                    style = OverlineStyle,
                    color = Brand.GoldLight,
                )
                Text(
                    stringResource(R.string.hero_title),
                    style = MaterialTheme.typography.displayMedium,
                    color = Brand.Parchment,
                )
                Text(
                    stringResource(R.string.hero_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Brand.Parchment.copy(alpha = 0.8f),
                )
            }
            Spacer(Modifier.height(132.dp))
            Button(
                onClick = onNewGame,
                colors = ButtonDefaults.buttonColors(containerColor = Brand.Gold, contentColor = Color(0xFF2A1C00)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 20.dp).height(60.dp),
            ) {
                Icon(Icons.Filled.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.new_game), style = MaterialTheme.typography.titleMedium, fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun OngoingGameCard(game: GameEntity, onContinue: () -> Unit, onDiscard: () -> Unit, modifier: Modifier = Modifier) {
    val state = game.state
    val progress = state.enteredCount.toFloat() / state.cellCount.coerceAtLeast(1)
    Surface(
        onClick = onContinue,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        border = BorderStroke(1.dp, Brush.linearGradient(listOf(Brand.GoldLight, Brand.GoldDeep))),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                RingProgress(progress, Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.secondary)
                Icon(Icons.Filled.PlayArrow, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.continue_game), style = MaterialTheme.typography.titleLarge)
                Text(
                    state.seats.joinToString(" · ") { it.name },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(R.string.scoring_progress_percent, (progress * 100).roundToInt()),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                )
            }
            IconButton(onClick = onDiscard) { Icon(Icons.Outlined.Delete, stringResource(R.string.discard)) }
        }
    }
}

@Composable
private fun FinishedGameCard(game: GameEntity, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val standings = game.state.standings()
    val winners = standings.filter { it.rank == 1 }
    val winner = winners.firstOrNull()
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (winner != null) {
                    Box(contentAlignment = Alignment.TopCenter) {
                        PlayerAvatar(winner.seat.name, winner.seat.playerId, size = 44.dp, modifier = Modifier.padding(top = 10.dp))
                        Icon(GameIcons.Crown, null, tint = Brand.Gold, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        winners.joinToString(" & ") { it.seat.name },
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        formatDate(game.finishedAt ?: game.startedAt) + " · " +
                            pluralStringResource(R.plurals.players_count, game.state.seats.size, game.state.seats.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    formatScore(winner?.total ?: 0),
                    style = ScoreStyle,
                    fontSize = 30.sp,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            if (winner != null) {
                Spacer(Modifier.height(12.dp))
                CompositionBar(
                    game.state.categories.map { it to (game.state.value(winner.seat.playerId, it) ?: 0) },
                    height = 6.dp,
                )
            }
            val others = standings.drop(winners.size)
            if (others.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    others.joinToString("   ") { "${it.rank}. ${it.seat.name} ${formatScore(it.total)}" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun EmptyHome() {
    Column(
        Modifier.fillMaxWidth().padding(top = 32.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MeanderBand(MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), Modifier.width(120.dp))
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.empty_home_title), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.empty_home_text),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(20.dp))
        MeanderBand(MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f), Modifier.width(120.dp))
    }
}
