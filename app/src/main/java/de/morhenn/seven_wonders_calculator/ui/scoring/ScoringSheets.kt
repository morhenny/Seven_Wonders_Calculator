package de.morhenn.seven_wonders_calculator.ui.scoring

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.morhenn.seven_wonders_calculator.R
import de.morhenn.seven_wonders_calculator.domain.GameState
import de.morhenn.seven_wonders_calculator.ui.components.CategoryBadge
import de.morhenn.seven_wonders_calculator.ui.components.PlayerAvatar
import de.morhenn.seven_wonders_calculator.ui.components.color
import de.morhenn.seven_wonders_calculator.ui.components.formatScore
import de.morhenn.seven_wonders_calculator.ui.components.label
import de.morhenn.seven_wonders_calculator.ui.theme.NumberStyle

/** Overview of the whole score sheet. Tapping a cell jumps there. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreTableSheet(
    game: GameState,
    selected: Pair<Int, Int>?,
    onSelect: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            stringResource(R.string.score_sheet),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(8.dp))
        Column(Modifier.verticalScroll(rememberScrollState())) {
            ScoreTable(game, selected, onSelect, Modifier.padding(horizontal = 12.dp).padding(bottom = 24.dp).navigationBarsPadding())
        }
    }
}

@Composable
fun ScoreTable(
    game: GameState,
    selected: Pair<Int, Int>?,
    onSelect: ((Int, Int) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val cellWidth = 64.dp
    val labelWidth = 112.dp
    Row(modifier.horizontalScroll(rememberScrollState())) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Spacer(Modifier.height(56.dp))
            game.categories.forEach { category ->
                Row(Modifier.size(labelWidth, 40.dp), verticalAlignment = Alignment.CenterVertically) {
                    CategoryBadge(category, size = 28.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(category.label),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Box(Modifier.size(labelWidth, 44.dp), contentAlignment = Alignment.CenterStart) {
                Text("Σ", style = MaterialTheme.typography.titleLarge)
            }
        }
        game.seats.forEachIndexed { p, seat ->
            Column(
                Modifier.width(cellWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Column(Modifier.height(56.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    PlayerAvatar(seat.name, seat.playerId, size = 30.dp)
                    Text(
                        seat.name,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 2.dp),
                    )
                }
                game.categories.forEachIndexed { c, category ->
                    val value = game.value(seat.playerId, category)
                    val isSelected = selected == (c to p)
                    Box(
                        Modifier
                            .size(cellWidth - 6.dp, 40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) category.color.copy(alpha = 0.25f)
                                else MaterialTheme.colorScheme.surfaceContainerHigh
                            )
                            .then(if (onSelect != null) Modifier.clickable { onSelect(c, p) } else Modifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            value?.let(::formatScore) ?: "–",
                            style = MaterialTheme.typography.titleMedium.merge(NumberStyle),
                            color = if (value == null) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Box(
                    Modifier
                        .size(cellWidth - 6.dp, 44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        formatScore(game.total(seat.playerId)),
                        style = MaterialTheme.typography.titleLarge.merge(NumberStyle),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}
