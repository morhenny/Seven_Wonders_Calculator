package de.morhenn.seven_wonders_calculator.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Foundation
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.morhenn.seven_wonders_calculator.R
import de.morhenn.seven_wonders_calculator.domain.Category
import de.morhenn.seven_wonders_calculator.domain.Expansion
import kotlinx.coroutines.launch

/** Card colors of the game, used identically in light and dark theme. */
val Category.color: Color
    get() = when (this) {
        Category.MILITARY -> Color(0xFFC0392B)
        Category.NAVAL -> Color(0xFF1F6F8B)
        Category.TREASURY -> Color(0xFFD9A21B)
        Category.WONDER -> Color(0xFF8C7B6B)
        Category.CIVILIAN -> Color(0xFF2E6FBF)
        Category.COMMERCIAL -> Color(0xFFF2C230)
        Category.GUILDS -> Color(0xFF7B3FA0)
        Category.SCIENCE -> Color(0xFF2E8B57)
        Category.LEADERS -> Color(0xFFE8E4DA)
        Category.CITIES -> Color(0xFF2B2B2B)
        Category.EDIFICE -> Color(0xFFA8642A)
    }

val Category.onColor: Color
    get() = when (this) {
        Category.TREASURY, Category.COMMERCIAL, Category.LEADERS -> Color(0xFF241A00)
        else -> Color.White
    }

val Category.icon: ImageVector
    get() = when (this) {
        Category.MILITARY -> Icons.Filled.Shield
        Category.NAVAL -> Icons.Filled.Sailing
        Category.TREASURY -> GameIcons.Coin
        Category.WONDER -> GameIcons.Pyramid
        Category.CIVILIAN -> Icons.Filled.AccountBalance
        Category.COMMERCIAL -> Icons.Filled.Storefront
        Category.GUILDS -> Icons.Filled.Groups
        Category.SCIENCE -> Icons.Filled.Science
        Category.LEADERS -> GameIcons.Crown
        Category.CITIES -> Icons.Filled.LocationCity
        Category.EDIFICE -> Icons.Filled.Foundation
    }

@get:StringRes
val Category.label: Int
    get() = when (this) {
        Category.MILITARY -> R.string.category_military
        Category.NAVAL -> R.string.category_naval
        Category.TREASURY -> R.string.category_treasury
        Category.WONDER -> R.string.category_wonder
        Category.CIVILIAN -> R.string.category_civilian
        Category.COMMERCIAL -> R.string.category_commercial
        Category.GUILDS -> R.string.category_guilds
        Category.SCIENCE -> R.string.category_science
        Category.LEADERS -> R.string.category_leaders
        Category.CITIES -> R.string.category_cities
        Category.EDIFICE -> R.string.category_edifice
    }

@get:StringRes
val Category.hint: Int
    get() = when (this) {
        Category.MILITARY -> R.string.hint_military
        Category.NAVAL -> R.string.hint_naval
        Category.TREASURY -> R.string.hint_treasury
        Category.WONDER -> R.string.hint_wonder
        Category.CIVILIAN -> R.string.hint_civilian
        Category.COMMERCIAL -> R.string.hint_commercial
        Category.GUILDS -> R.string.hint_guilds
        Category.SCIENCE -> R.string.hint_science
        Category.LEADERS -> R.string.hint_leaders
        Category.CITIES -> R.string.hint_cities
        Category.EDIFICE -> R.string.hint_edifice
    }

@get:StringRes
val Expansion.label: Int
    get() = when (this) {
        Expansion.LEADERS -> R.string.expansion_leaders
        Expansion.CITIES -> R.string.expansion_cities
        Expansion.ARMADA -> R.string.expansion_armada
        Expansion.EDIFICE -> R.string.expansion_edifice
    }

/**
 * Colored category tile. With [showName], tapping or long-pressing reveals the category name,
 * so the icons can be learned anywhere they appear.
 */
val Expansion.icon: ImageVector
    get() = when (this) {
        Expansion.LEADERS -> GameIcons.Crown
        Expansion.CITIES -> Icons.Filled.LocationCity
        Expansion.ARMADA -> Icons.Filled.Sailing
        Expansion.EDIFICE -> Icons.Filled.Foundation
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryBadge(category: Category, modifier: Modifier = Modifier, size: Dp = 36.dp, showName: Boolean = false) {
    if (!showName) {
        CategoryTile(category, modifier, size)
        return
    }
    val tooltipState = rememberTooltipState()
    val scope = rememberCoroutineScope()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(stringResource(category.label)) } },
        state = tooltipState,
    ) {
        CategoryTile(
            category,
            modifier.clickable(onClickLabel = stringResource(category.label)) { scope.launch { tooltipState.show() } },
            size,
        )
    }
}

@Composable
private fun CategoryTile(category: Category, modifier: Modifier, size: Dp) {
    val outlined = category == Category.LEADERS || category == Category.CITIES
    Box(
        modifier = modifier
            .size(size)
            .background(category.color, RoundedCornerShape(size / 4))
            .then(
                if (outlined) Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(size / 4))
                else Modifier
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = stringResource(category.label),
            tint = category.onColor,
            modifier = Modifier.size(size * 0.6f),
        )
    }
}

private val playerPalette = listOf(
    Color(0xFF3F6FD8), Color(0xFFD8553F), Color(0xFF2E9E6A), Color(0xFF9B59B6),
    Color(0xFFE09B1A), Color(0xFF16A2B8), Color(0xFFD84B8C), Color(0xFF6D7A2E),
)

/** Keyed by player id so a player keeps their color across games and screens. */
fun playerColor(playerId: Long): Color = playerPalette[playerId.mod(playerPalette.size)]

@Composable
fun PlayerAvatar(name: String, playerId: Long, modifier: Modifier = Modifier, size: Dp = 36.dp) {
    Box(
        modifier = modifier.size(size).background(playerColor(playerId), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.trim().take(1).uppercase().ifEmpty { "?" },
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.45f).sp,
        )
    }
}

/** Formats a score with a real minus sign. */
fun formatScore(value: Int): String = if (value < 0) "−${-value}" else value.toString()
