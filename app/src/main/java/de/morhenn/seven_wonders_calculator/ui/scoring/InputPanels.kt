package de.morhenn.seven_wonders_calculator.ui.scoring

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.morhenn.seven_wonders_calculator.R
import de.morhenn.seven_wonders_calculator.domain.Category
import de.morhenn.seven_wonders_calculator.domain.ConflictToken
import de.morhenn.seven_wonders_calculator.domain.MilitaryInput
import de.morhenn.seven_wonders_calculator.domain.ScienceInput
import de.morhenn.seven_wonders_calculator.ui.components.GameIcons
import de.morhenn.seven_wonders_calculator.ui.components.color
import de.morhenn.seven_wonders_calculator.ui.components.formatScore
import de.morhenn.seven_wonders_calculator.ui.theme.NumberStyle

private fun ConflictToken.label(): String = if (points < 0) "−${-points}" else "+$points"

/** Token faces: a dark defeat token and bronze, silver and gold laurels for the three ages. */
private fun ConflictToken.colors(): List<Color> = when (this) {
    ConflictToken.DEFEAT -> listOf(Color(0xFF5B5B5B), Color(0xFF2E2E2E))
    ConflictToken.AGE_1 -> listOf(Color(0xFFE0A26E), Color(0xFFA2602F))
    ConflictToken.AGE_2 -> listOf(Color(0xFFE3E7EC), Color(0xFF9AA3AD))
    ConflictToken.AGE_3 -> listOf(Color(0xFFFFE08A), Color(0xFFD19B1C))
}

private fun ConflictToken.textColor(): Color = if (this == ConflictToken.DEFEAT) Color.White else Color(0xFF2A1C00)

/**
 * One tap per token: tap a token to count it, the minus underneath (or a long press) takes it
 * back. Lives right above the number pad, so typing the total still works at any time.
 */
@Composable
fun TokenCounterRow(input: MilitaryInput?, haptics: Boolean, onAdd: (ConflictToken, Int) -> Unit, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ConflictToken.entries.forEach { token ->
            val count = input?.count(token) ?: 0
            TokenCounter(
                token = token,
                count = count,
                onAdd = {
                    if (haptics) haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                    onAdd(token, 1)
                },
                onRemove = {
                    if (count > 0) {
                        if (haptics) haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
                        onAdd(token, -1)
                    }
                },
            )
        }
    }
}

@Composable
private fun RowScope.TokenCounter(token: ConflictToken, count: Int, onAdd: () -> Unit, onRemove: () -> Unit) {
    val addLabel = stringResource(R.string.token_add, token.label())
    val removeLabel = stringResource(R.string.token_remove, token.label())
    val pop by animateFloatAsState(if (count > 0) 1f else 0.92f, spring(dampingRatio = 0.4f), label = "tokenPop")
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.weight(1f),
    ) {
        Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .scale(pop)
                    .background(Brush.linearGradient(token.colors()), CircleShape)
                    .border(1.5.dp, Color.Black.copy(alpha = 0.15f), CircleShape)
                    .combinedClickable(onClick = onAdd, onLongClick = onRemove, onClickLabel = addLabel, onLongClickLabel = removeLabel)
                    .semantics { contentDescription = addLabel },
                contentAlignment = Alignment.Center,
            ) {
                Text(token.label(), color = token.textColor(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedCount(count, MaterialTheme.typography.titleMedium.fontSize.value)
                IconButton(onClick = onRemove, enabled = count > 0, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Remove, removeLabel, Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun AnimatedCount(count: Int, fontSize: Float) {
    AnimatedContent(
        targetState = count,
        transitionSpec = {
            val up = targetState > initialState
            (slideInVertically { if (up) it else -it } + fadeIn()) togetherWith
                (slideOutVertically { if (up) -it else it } + fadeOut())
        },
        label = "count",
    ) { value ->
        Text(
            "×$value",
            style = MaterialTheme.typography.titleMedium.merge(NumberStyle),
            fontSize = fontSize.sp,
            color = if (value == 0) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/** Compact −/+ control, used for debt tokens next to the coins/points switch. */
@Composable
fun MiniStepper(label: String, value: Int, onChange: (Int) -> Unit, haptics: Boolean, modifier: Modifier = Modifier) {
    val haptic = LocalHapticFeedback.current
    fun change(v: Int) {
        if (v < 0) return
        if (haptics) haptic.performHapticFeedback(HapticFeedbackType.SegmentTick)
        onChange(v)
    }
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest, modifier = modifier) {
        Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(start = 10.dp, end = 2.dp))
            IconButton(onClick = { change(value - 1) }, enabled = value > 0, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Remove, stringResource(R.string.decrease, label), Modifier.size(18.dp))
            }
            Text(value.toString(), style = MaterialTheme.typography.titleMedium.merge(NumberStyle))
            IconButton(onClick = { change(value + 1) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Add, stringResource(R.string.increase, label), Modifier.size(18.dp))
            }
        }
    }
}

/**
 * Science symbols counted in place of the number pad: nobody knows their science score by heart,
 * so counting symbols is the main path and typing points is the fallback.
 */
@Composable
fun ScienceCounter(
    input: ScienceInput?,
    withAristotle: Boolean,
    haptics: Boolean,
    height: Dp,
    onChange: ((ScienceInput) -> ScienceInput) -> Unit,
    footer: @Composable () -> Unit,
) {
    val current = input ?: ScienceInput()
    val green = Category.SCIENCE.color
    val gap = 6.dp
    Column(Modifier.height(height), verticalArrangement = Arrangement.spacedBy(gap)) {
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
            SymbolCell(GameIcons.Compass, stringResource(R.string.science_compass), current.compass, green, haptics) { v ->
                onChange { it.copy(compass = v) }
            }
            SymbolCell(Icons.Filled.Settings, stringResource(R.string.science_gear), current.gear, green, haptics) { v ->
                onChange { it.copy(gear = v) }
            }
        }
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(gap)) {
            SymbolCell(GameIcons.Tablet, stringResource(R.string.science_tablet), current.tablet, green, haptics) { v ->
                onChange { it.copy(tablet = v) }
            }
            SymbolCell(GameIcons.Wildcard, stringResource(R.string.science_wild_short), current.wild, Category.GUILDS.color, haptics) { v ->
                onChange { it.copy(wild = v.coerceAtMost(5)) }
            }
        }
        val r = current.result
        Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.points_value, formatScore(r.points)),
                    style = MaterialTheme.typography.titleMedium.merge(NumberStyle),
                )
                Text(
                    buildString {
                        append("${r.compass}² + ${r.gear}² + ${r.tablet}²")
                        if (r.sets > 0) append(" + ${r.sets} × ${current.setBonus}")
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (withAristotle || current.setBonus != 7) {
                FilterChip(
                    selected = current.setBonus == 10,
                    onClick = { onChange { it.copy(setBonus = if (it.setBonus == 10) 7 else 10) } },
                    label = { Text(stringResource(R.string.science_aristotle)) },
                    leadingIcon = if (current.setBonus == 10) {
                        { Icon(Icons.Filled.Check, null, Modifier.size(16.dp)) }
                    } else null,
                )
            }
        }
        footer()
    }
}

@Composable
private fun RowScope.SymbolCell(icon: ImageVector, label: String, value: Int, tint: Color, haptics: Boolean, onChange: (Int) -> Unit) {
    val haptic = LocalHapticFeedback.current
    fun change(v: Int) {
        if (v !in 0..20) return
        if (haptics) haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
        onChange(v)
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.weight(1f).fillMaxSize(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Tapping the symbol itself counts it: the most common action gets the biggest target.
            Surface(
                onClick = { change(value + 1) },
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                modifier = Modifier.weight(1f).fillMaxSize().semantics { contentDescription = label },
            ) {
                Row(Modifier.padding(start = 10.dp, end = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = tint, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.width(6.dp))
                    Column(Modifier.weight(1f)) {
                        Text(label, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        AnimatedCount(value, 20f)
                    }
                }
            }
            IconButton(onClick = { change(value - 1) }, enabled = value > 0, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Remove, stringResource(R.string.decrease, label))
            }
            FilledTonalIconButton(
                onClick = { change(value + 1) },
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = tint.copy(alpha = 0.22f), contentColor = tint),
                modifier = Modifier.padding(end = 6.dp).size(38.dp),
            ) {
                Icon(Icons.Filled.Add, stringResource(R.string.increase, label))
            }
        }
    }
}

/** Previous / next keys for panels that replace the number pad. */
@Composable
fun StepKeys(onPrevious: () -> Unit, onNext: () -> Unit, isLastCell: Boolean, accent: Color, onAccent: Color, haptics: Boolean, height: Dp) {
    val haptic = LocalHapticFeedback.current
    Row(Modifier.fillMaxWidth().height(height), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Surface(
            onClick = onPrevious,
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f).fillMaxSize(),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.key_previous))
            }
        }
        val nextLabel = stringResource(if (isLastCell) R.string.key_finish else R.string.key_next)
        Surface(
            onClick = {
                if (haptics) haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onNext()
            },
            shape = RoundedCornerShape(16.dp),
            color = accent,
            contentColor = onAccent,
            modifier = Modifier.weight(3f).fillMaxSize().semantics { contentDescription = nextLabel },
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (isLastCell) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(30.dp),
                )
            }
        }
    }
}
