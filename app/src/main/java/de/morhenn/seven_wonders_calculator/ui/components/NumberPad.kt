package de.morhenn.seven_wonders_calculator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import de.morhenn.seven_wonders_calculator.ui.theme.ScoreStyle
import de.morhenn.seven_wonders_calculator.R

/**
 * Calculator-style pad. Kept on screen permanently so entering a whole game never needs the
 * system keyboard:
 * ```
 *  1 2 3 ⌫
 *  4 5 6 ±
 *  7 8 9 ┐
 *  C 0 ‹ ┘ next
 * ```
 */
@Composable
fun NumberPad(
    onDigit: (Int) -> Unit,
    onBackspace: () -> Unit,
    onToggleSign: () -> Unit,
    onClear: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    isLastCell: Boolean,
    accent: Color,
    onAccent: Color,
    haptics: Boolean,
    modifier: Modifier = Modifier,
    keyHeight: Dp = 56.dp,
) {
    val haptic = LocalHapticFeedback.current
    fun tap(action: () -> Unit): () -> Unit = {
        if (haptics) haptic.performHapticFeedback(HapticFeedbackType.KeyboardTap)
        action()
    }
    val gap = 6.dp
    // Built column by column so every key lines up and "next" can span two rows.
    Row(modifier.height(keyHeight * 4 + gap * 3), horizontalArrangement = Arrangement.spacedBy(gap)) {
        val digitColumns = listOf(listOf(1, 4, 7), listOf(2, 5, 8), listOf(3, 6, 9))
        digitColumns.forEachIndexed { index, digits ->
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(gap)) {
                digits.forEach { d -> DigitKey(d, tap { onDigit(d) }) }
                when (index) {
                    0 -> TextKey("C", stringResource(R.string.key_clear), tap(onClear))
                    1 -> DigitKey(0, tap { onDigit(0) })
                    else -> IconKey(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.key_previous), tap(onPrevious))
                }
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(gap)) {
            IconKey(Icons.AutoMirrored.Outlined.Backspace, stringResource(R.string.key_backspace), tap(onBackspace))
            TextKey("±", stringResource(R.string.key_sign), tap(onToggleSign))
            val nextLabel = stringResource(if (isLastCell) R.string.key_finish else R.string.key_next)
            Key(
                onClick = {
                    if (haptics) haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                    onNext()
                },
                container = accent,
                content = onAccent,
                brush = Brush.verticalGradient(listOf(lerp(accent, Color.White, 0.18f), accent, lerp(accent, Color.Black, 0.15f))),
                shadow = 3.dp,
                description = nextLabel,
                modifier = Modifier.height(keyHeight * 2 + gap).fillMaxWidth(),
            ) {
                Icon(
                    if (isLastCell) Icons.Filled.Check else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.DigitKey(digit: Int, onClick: () -> Unit) {
    val light = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    Key(
        onClick,
        modifier = Modifier.weight(1f).fillMaxWidth(),
        container = if (light) MaterialTheme.colorScheme.surfaceContainerLowest else MaterialTheme.colorScheme.surfaceContainerHighest,
        shadow = if (light) 1.dp else 0.dp,
        description = digit.toString(),
    ) {
        Text(digit.toString(), style = ScoreStyle, fontSize = 28.sp)
    }
}

@Composable
private fun ColumnScope.TextKey(text: String, description: String, onClick: () -> Unit) {
    Key(
        onClick,
        modifier = Modifier.weight(1f).fillMaxWidth(),
        container = MaterialTheme.colorScheme.surfaceContainerHighest,
        content = MaterialTheme.colorScheme.secondary,
        description = description,
    ) {
        Text(text, style = ScoreStyle, fontSize = 24.sp)
    }
}

@Composable
private fun ColumnScope.IconKey(icon: ImageVector, description: String, onClick: () -> Unit) {
    Key(
        onClick,
        modifier = Modifier.weight(1f).fillMaxWidth(),
        container = MaterialTheme.colorScheme.surfaceContainerHighest,
        content = MaterialTheme.colorScheme.secondary,
        description = description,
    ) {
        Icon(icon, contentDescription = null)
    }
}

@Composable
private fun Key(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    container: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    content: Color = MaterialTheme.colorScheme.onSurface,
    brush: Brush? = null,
    shadow: Dp = 0.dp,
    description: String,
    label: @Composable () -> Unit,
) {
    // Keys sink slightly while pressed, like a physical button.
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.93f else 1f, spring(stiffness = Spring.StiffnessHigh), label = "keyScale")
    val shape = RoundedCornerShape(16.dp)
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .semantics { contentDescription = description },
        shape = shape,
        color = if (brush != null) Color.Transparent else container,
        contentColor = content,
        shadowElevation = if (pressed) 0.dp else shadow,
        interactionSource = interaction,
    ) {
        Box(
            Modifier.fillMaxSize().then(if (brush != null) Modifier.background(brush, shape) else Modifier),
            contentAlignment = Alignment.Center,
        ) { label() }
    }
}
