package de.morhenn.seven_wonders_calculator.ui.results

import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import de.morhenn.seven_wonders_calculator.R
import de.morhenn.seven_wonders_calculator.ResultsRoute
import de.morhenn.seven_wonders_calculator.container
import de.morhenn.seven_wonders_calculator.data.GameEntity
import de.morhenn.seven_wonders_calculator.data.GameRepository
import de.morhenn.seven_wonders_calculator.domain.GameState
import de.morhenn.seven_wonders_calculator.domain.Standing
import de.morhenn.seven_wonders_calculator.ui.components.CategoryBadge
import de.morhenn.seven_wonders_calculator.ui.components.CompositionBar
import de.morhenn.seven_wonders_calculator.ui.components.ConfettiBurst
import de.morhenn.seven_wonders_calculator.ui.components.LaurelWreath
import de.morhenn.seven_wonders_calculator.ui.components.PlayerAvatar
import de.morhenn.seven_wonders_calculator.ui.components.RankMedal
import de.morhenn.seven_wonders_calculator.ui.components.SectionHeader
import de.morhenn.seven_wonders_calculator.ui.components.HeroStatusBar
import de.morhenn.seven_wonders_calculator.ui.components.formatScore
import de.morhenn.seven_wonders_calculator.ui.components.label
import de.morhenn.seven_wonders_calculator.ui.formatDateTime
import de.morhenn.seven_wonders_calculator.ui.scoring.ScoreTable
import de.morhenn.seven_wonders_calculator.ui.theme.Brand
import de.morhenn.seven_wonders_calculator.ui.theme.OverlineStyle
import de.morhenn.seven_wonders_calculator.ui.theme.ScoreStyle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ResultsViewModel(savedStateHandle: SavedStateHandle, private val games: GameRepository) : ViewModel() {
    val gameId = savedStateHandle.toRoute<ResultsRoute>().gameId

    val game: StateFlow<GameEntity?> = games.observe(gameId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun reopen(then: () -> Unit) {
        viewModelScope.launch {
            games.reopen(gameId)
            then()
        }
    }

    fun delete(then: () -> Unit) {
        viewModelScope.launch {
            games.delete(gameId)
            then()
        }
    }

    companion object {
        val Factory = viewModelFactory { initializer { ResultsViewModel(createSavedStateHandle(), container.games) } }
    }
}

/** Confetti only for a game that was just finished, not when browsing history. */
private const val CELEBRATION_WINDOW_MS = 60_000L

@Composable
fun ResultsScreen(
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onRematch: (Long) -> Unit,
    viewModel: ResultsViewModel = viewModel(factory = ResultsViewModel.Factory),
) {
    val entity by viewModel.game.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var confirmDelete by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val heroVisible by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    val game = entity?.state
    // Surface provides the theme's content color; without it, plain texts default to black.
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
    Box(Modifier.fillMaxSize()) {
        if (entity != null && game != null) {
            val standings = remember(game) { game.standings() }
            val note = tieNote(standings)
            LazyColumn(Modifier.fillMaxSize(), state = listState, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item(key = "hero") {
                    VictoryHero(
                        standings = standings,
                        date = formatDateTime(entity!!.finishedAt ?: entity!!.startedAt),
                        onBack = onBack,
                        onShare = { share(context, entity!!) },
                        onDelete = { confirmDelete = true },
                    )
                }
                if (note != null) {
                    item(key = "tie") { TieNote(note, Modifier.padding(horizontal = 16.dp)) }
                }
                item(key = "ranking") {
                    SectionHeader(stringResource(R.string.ranking), Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp))
                }
                itemsIndexed(standings, key = { _, s -> s.seat.playerId }) { index, standing ->
                    StandingCard(standing, game, index, Modifier.padding(horizontal = 16.dp))
                }
                item(key = "sheet") {
                    Column(Modifier.padding(top = 12.dp)) {
                        SectionHeader(stringResource(R.string.score_sheet), Modifier.padding(horizontal = 20.dp))
                        Spacer(Modifier.height(12.dp))
                        ScoreTable(game, selected = null, onSelect = null, modifier = Modifier.padding(horizontal = 12.dp))
                    }
                }
                item(key = "actions") {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.reopen { onEdit(viewModel.gameId) } },
                            modifier = Modifier.weight(1f).height(56.dp),
                        ) {
                            Icon(Icons.Filled.Edit, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.edit_scores))
                        }
                        Button(
                            onClick = { onRematch(viewModel.gameId) },
                            colors = ButtonDefaults.buttonColors(containerColor = Brand.Gold, contentColor = Color(0xFF2A1C00)),
                            modifier = Modifier.weight(1f).height(56.dp),
                        ) {
                            Icon(Icons.Filled.Replay, null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.rematch))
                        }
                    }
                }
            }
            val justFinished = remember(entity!!.id) {
                System.currentTimeMillis() - (entity!!.finishedAt ?: 0L) < CELEBRATION_WINDOW_MS
            }
            if (justFinished) ConfettiBurst(Modifier.fillMaxSize())
        }
        HeroStatusBar(heroVisible)
    }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_game_title)) },
            text = { Text(stringResource(R.string.delete_game_text)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; viewModel.delete(onBack) }) { Text(stringResource(R.string.delete)) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun VictoryHero(
    standings: List<Standing>,
    date: String,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
) {
    val winners = standings.filter { it.rank == 1 }
    val winner = winners.firstOrNull() ?: return
    val rise = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) { rise.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
    Box(
        Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Brand.Night, Brand.LapisDeep, Brand.Lapis)))
            .background(Brush.radialGradient(listOf(Brand.Gold.copy(alpha = 0.35f), Color.Transparent), radius = 600f)),
    ) {
        Column(Modifier.statusBarsPadding().padding(bottom = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back), tint = Color.White) }
                Text(date, style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.weight(1f))
                IconButton(onClick = onShare) { Icon(Icons.Filled.Share, stringResource(R.string.share), tint = Color.White) }
                IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, stringResource(R.string.delete), tint = Color.White) }
            }
            Box(
                Modifier.size(196.dp).graphicsLayer { scaleX = rise.value; scaleY = rise.value },
                contentAlignment = Alignment.Center,
            ) {
                LaurelWreath(Modifier.fillMaxSize())
                if (winners.size == 1) {
                    PlayerAvatar(winner.seat.name, winner.seat.playerId, size = 96.dp)
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy((-16).dp)) {
                        winners.take(3).forEach { PlayerAvatar(it.seat.name, it.seat.playerId, size = 64.dp) }
                    }
                }
            }
            Text(
                stringResource(if (winners.size > 1) R.string.shared_victory else R.string.victory).uppercase(),
                style = OverlineStyle,
                color = Brand.GoldLight,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                winners.joinToString(" & ") { it.seat.name },
                style = MaterialTheme.typography.displaySmall,
                color = Brand.Parchment,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(formatScore(winner.total), style = ScoreStyle, fontSize = 44.sp, color = Brand.Gold)
                Spacer(Modifier.width(6.dp))
                Text(
                    stringResource(R.string.points_short),
                    style = MaterialTheme.typography.titleMedium,
                    color = Brand.GoldLight,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            winner.seat.wonder?.takeIf { winners.size == 1 }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun tieNote(standings: List<Standing>): String? {
    val unresolved = standings.filter { it.unresolvedTie }
    return when {
        unresolved.isNotEmpty() -> stringResource(R.string.tie_unresolved, unresolved.joinToString(", ") { it.seat.name })
        standings.any { it.decidedByCoins } -> stringResource(R.string.tie_by_coins)
        else -> null
    }
}

@Composable
private fun TieNote(text: String, modifier: Modifier) {
    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.tertiaryContainer, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Info, null)
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StandingCard(standing: Standing, game: GameState, index: Int, modifier: Modifier) {
    // Cards rise in one after another.
    val appear = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(120L * index)
        appear.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
    }
    val id = standing.seat.playerId
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (standing.rank == 1) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = appear.value
                translationY = (1f - appear.value) * 60f
            },
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RankMedal(standing.rank, size = 32.dp)
                Spacer(Modifier.width(10.dp))
                PlayerAvatar(standing.seat.name, id)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(standing.seat.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val details = listOfNotNull(
                        standing.seat.wonder,
                        standing.coins?.let { pluralStringResource(R.plurals.coins_count, it, it) },
                    )
                    if (details.isNotEmpty()) {
                        Text(
                            details.joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(formatScore(standing.total), style = ScoreStyle, fontSize = 30.sp)
            }
            Spacer(Modifier.height(12.dp))
            CompositionBar(game.categories.map { it to (game.value(id, it) ?: 0) })
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                game.categories.forEach { category ->
                    val value = game.value(id, category) ?: return@forEach
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryBadge(category, size = 22.dp, showName = true)
                        Spacer(Modifier.width(4.dp))
                        Text(formatScore(value), style = ScoreStyle, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

private fun share(context: Context, entity: GameEntity) {
    val game = entity.state
    val medals = listOf("🥇", "🥈", "🥉")
    val text = buildString {
        appendLine(context.getString(R.string.share_header, formatDateTime(entity.finishedAt ?: entity.startedAt)))
        game.standings().forEach { s ->
            val medal = medals.getOrNull(s.rank - 1) ?: "${s.rank}."
            val wonder = s.seat.wonder?.let { " ($it)" }.orEmpty()
            appendLine("$medal ${s.seat.name}$wonder: ${formatScore(s.total)}")
        }
        appendLine()
        game.categories.forEach { category ->
            val values = game.seats.joinToString(" / ") { formatScore(game.value(it.playerId, category) ?: 0) }
            appendLine("${context.getString(category.label)}: $values")
        }
    }.trim()
    val intent = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
}
