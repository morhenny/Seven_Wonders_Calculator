package de.morhenn.seven_wonders_calculator.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.morhenn.seven_wonders_calculator.R
import de.morhenn.seven_wonders_calculator.container
import de.morhenn.seven_wonders_calculator.data.GameRepository
import de.morhenn.seven_wonders_calculator.domain.PlayerStats
import de.morhenn.seven_wonders_calculator.domain.Stats
import de.morhenn.seven_wonders_calculator.domain.WonderStats
import de.morhenn.seven_wonders_calculator.ui.components.CategoryBadge
import de.morhenn.seven_wonders_calculator.ui.components.PlayerAvatar
import de.morhenn.seven_wonders_calculator.ui.components.label
import de.morhenn.seven_wonders_calculator.ui.theme.NumberStyle
import de.morhenn.seven_wonders_calculator.ui.theme.OverlineStyle
import de.morhenn.seven_wonders_calculator.ui.theme.ScoreStyle
import de.morhenn.seven_wonders_calculator.ui.theme.Brand
import de.morhenn.seven_wonders_calculator.ui.components.RingProgress
import de.morhenn.seven_wonders_calculator.ui.components.SectionHeader
import de.morhenn.seven_wonders_calculator.ui.components.playerColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Locale
import kotlin.math.roundToInt

data class StatsUiState(
    val loading: Boolean = true,
    val gameCount: Int = 0,
    val players: List<PlayerStats> = emptyList(),
    val wonders: List<WonderStats> = emptyList(),
    val highScore: Pair<String, Int>? = null,
)

class StatsViewModel(games: GameRepository) : ViewModel() {
    val state: StateFlow<StatsUiState> = games.games.map { all ->
        val finished = all.filter { it.finishedAt != null }.sortedBy { it.startedAt }.map { it.state }
        val best = finished.flatMap { it.standings() }.maxByOrNull { it.total }
        StatsUiState(
            loading = false,
            gameCount = finished.size,
            players = Stats.players(finished),
            wonders = Stats.wonders(finished),
            highScore = best?.let { it.seat.name to it.total },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    companion object {
        val Factory = viewModelFactory { initializer { StatsViewModel(container.games) } }
    }
}

private fun Double.oneDecimal(): String = String.format(Locale.getDefault(), "%.1f", this)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(onBack: () -> Unit, viewModel: StatsViewModel = viewModel(factory = StatsViewModel.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.stats_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
                },
            )
        },
    ) { padding ->
        if (!state.loading && state.gameCount == 0) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(Icons.Filled.Insights, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.stats_empty), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryTile(stringResource(R.string.stats_games), state.gameCount.toString(), Modifier.weight(1f))
                    SummaryTile(stringResource(R.string.stats_players), state.players.size.toString(), Modifier.weight(1f))
                    SummaryTile(
                        stringResource(R.string.stats_high_score),
                        state.highScore?.second?.toString() ?: "–",
                        Modifier.weight(1f),
                        caption = state.highScore?.first,
                        gold = true,
                    )
                }
            }
            item { SectionTitle(stringResource(R.string.stats_players)) }
            items(state.players, key = { it.playerId }) { PlayerStatsCard(it) }
            if (state.wonders.isNotEmpty()) {
                item { SectionTitle(stringResource(R.string.stats_wonders)) }
                items(state.wonders, key = { it.wonder }) { WonderRow(it) }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    SectionHeader(text, Modifier.padding(top = 16.dp, bottom = 4.dp))
}

@Composable
private fun SummaryTile(label: String, value: String, modifier: Modifier, caption: String? = null, gold: Boolean = false) {
    val colors = if (gold) listOf(Brand.GoldLight, Brand.Gold, Brand.GoldDeep) else listOf(Brand.Lapis, Brand.LapisDeep)
    val content = if (gold) Color(0xFF2A1C00) else Brand.Parchment
    Box(modifier.fillMaxHeight().clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(colors))) {
        Column(Modifier.padding(14.dp)) {
            Text(label.uppercase(), style = OverlineStyle, fontSize = 11.sp, color = content.copy(alpha = 0.8f))
            Text(value, style = ScoreStyle, fontSize = 34.sp, color = content)
            if (caption != null) {
                Text(
                    caption,
                    style = MaterialTheme.typography.labelMedium,
                    color = content.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PlayerStatsCard(stats: PlayerStats) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlayerAvatar(stats.name, stats.playerId)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(stats.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        pluralStringResource(R.plurals.stats_wins_of_games, stats.games, stats.wins, stats.games),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                    RingProgress(stats.winRate.toFloat(), Modifier.fillMaxSize(), color = playerColor(stats.playerId))
                    Text("${(stats.winRate * 100).roundToInt()}%", style = ScoreStyle, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Stat(stringResource(R.string.stats_average), stats.averageScore.oneDecimal())
                Stat(stringResource(R.string.stats_best), stats.bestScore.toString())
                stats.strongestCategory?.let { category ->
                    Column {
                        Text(stringResource(R.string.stats_strongest), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryBadge(category, size = 20.dp, showName = true)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                stringResource(category.label),
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
            stats.favoriteWonder?.let {
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.stats_favorite_wonder, it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleMedium.merge(NumberStyle))
    }
}

@Composable
private fun WonderRow(stats: WonderStats) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stats.wonder, style = MaterialTheme.typography.titleSmall)
                Text(
                    pluralStringResource(R.plurals.stats_wins_of_games, stats.games, stats.wins, stats.games),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                stringResource(R.string.stats_avg_short, stats.averageScore.oneDecimal()),
                style = MaterialTheme.typography.titleMedium.merge(NumberStyle),
            )
        }
    }
}
