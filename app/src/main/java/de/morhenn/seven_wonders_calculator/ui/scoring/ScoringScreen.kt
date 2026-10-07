package de.morhenn.seven_wonders_calculator.ui.scoring

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import de.morhenn.seven_wonders_calculator.ui.components.GameIcons
import de.morhenn.seven_wonders_calculator.ui.components.MeanderBand
import de.morhenn.seven_wonders_calculator.ui.components.icon
import de.morhenn.seven_wonders_calculator.ui.components.playerColor
import de.morhenn.seven_wonders_calculator.ui.theme.Brand
import de.morhenn.seven_wonders_calculator.ui.theme.ScoreStyle
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.morhenn.seven_wonders_calculator.R
import de.morhenn.seven_wonders_calculator.data.ScoringOrder
import de.morhenn.seven_wonders_calculator.domain.Category
import de.morhenn.seven_wonders_calculator.domain.Expansion
import de.morhenn.seven_wonders_calculator.domain.PlayerSheet
import de.morhenn.seven_wonders_calculator.ui.components.CategoryBadge
import de.morhenn.seven_wonders_calculator.ui.components.NumberPad
import de.morhenn.seven_wonders_calculator.ui.components.PlayerAvatar
import de.morhenn.seven_wonders_calculator.ui.components.color
import de.morhenn.seven_wonders_calculator.ui.components.formatScore
import de.morhenn.seven_wonders_calculator.ui.components.hint
import de.morhenn.seven_wonders_calculator.ui.components.label
import de.morhenn.seven_wonders_calculator.ui.components.onColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoringScreen(
    onBack: () -> Unit,
    onShowResults: (Long) -> Unit,
    viewModel: ScoringViewModel = viewModel(factory = ScoringViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tableOpen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ScoringEvent.ShowResults -> onShowResults(event.gameId)
            }
        }
    }

    val game = state.game
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.scoring_title))
                        if (game != null) {
                            Text(
                                stringResource(R.string.scoring_progress, game.enteredCount, game.cellCount),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::undo, enabled = state.canUndo) {
                        Icon(Icons.AutoMirrored.Filled.Undo, stringResource(R.string.action_undo))
                    }
                    IconButton(onClick = { tableOpen = true }) {
                        Icon(Icons.Filled.TableChart, stringResource(R.string.action_table))
                    }
                    OverflowMenu(state.order, onOrderChange = viewModel::setOrder, onFinish = viewModel::finish)
                },
            )
        },
    ) { padding ->
        if (game == null) return@Scaffold
        BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
            val landscape = maxWidth > maxHeight && maxWidth >= 560.dp
            val keyHeight = when {
                landscape -> ((maxHeight - 120.dp) / 4).coerceIn(44.dp, 64.dp)
                maxHeight < 640.dp -> 48.dp
                else -> 56.dp
            }
            if (landscape) {
                Row(Modifier.fillMaxSize()) {
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        ScopeHeader(state, viewModel)
                        CellList(state, viewModel, Modifier.weight(1f))
                    }
                    InputPanel(
                        state, viewModel, keyHeight,
                        modifier = Modifier.widthIn(max = 420.dp).fillMaxHeight().navigationBarsPadding(),
                    )
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    ScopeHeader(state, viewModel)
                    CellList(state, viewModel, Modifier.weight(1f))
                    InputPanel(state, viewModel, keyHeight, modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }

    if (tableOpen && game != null) {
        ScoreTableSheet(
            game = game,
            selected = state.categoryIndex to state.playerIndex,
            onSelect = { c, p -> tableOpen = false; viewModel.select(c, p) },
            onDismiss = { tableOpen = false },
        )
    }
}

@Composable
private fun OverflowMenu(order: ScoringOrder, onOrderChange: (ScoringOrder) -> Unit, onFinish: () -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) { Icon(Icons.Filled.MoreVert, stringResource(R.string.more)) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.order_by_category)) },
                leadingIcon = { Icon(Icons.Filled.ViewAgenda, null) },
                trailingIcon = { if (order == ScoringOrder.BY_CATEGORY) Icon(Icons.Filled.Check, null) },
                onClick = { open = false; onOrderChange(ScoringOrder.BY_CATEGORY) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.order_by_player)) },
                leadingIcon = { Icon(Icons.Filled.Person, null) },
                trailingIcon = { if (order == ScoringOrder.BY_PLAYER) Icon(Icons.Filled.Check, null) },
                onClick = { open = false; onOrderChange(ScoringOrder.BY_PLAYER) },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.action_finish)) },
                leadingIcon = { Icon(Icons.Filled.EmojiEvents, null) },
                onClick = { open = false; onFinish() },
            )
        }
    }
}

/** Tabs for the outer loop (categories or players) plus a banner for the selected one. */
@Composable
private fun ScopeHeader(state: ScoringUiState, viewModel: ScoringViewModel) {
    val game = state.game ?: return
    val listState = rememberLazyListState()
    val selectedTab = if (state.order == ScoringOrder.BY_CATEGORY) state.categoryIndex else state.playerIndex
    LaunchedEffect(selectedTab, state.order) {
        val visible = listState.layoutInfo.visibleItemsInfo
        if (visible.none { it.index == selectedTab } || visible.first().index == selectedTab || visible.last().index == selectedTab) {
            listState.animateScrollToItem((selectedTab - 2).coerceAtLeast(0))
        }
    }
    Column(Modifier.fillMaxWidth()) {
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (state.order == ScoringOrder.BY_CATEGORY) {
                itemsIndexed(state.categories) { index, category ->
                    TabItem(
                        selected = index == state.categoryIndex,
                        complete = game.isComplete(category),
                        accent = category.color,
                        onClick = { viewModel.selectCategory(index) },
                    ) { CategoryBadge(category, size = 38.dp) }
                }
            } else {
                itemsIndexed(state.seats) { index, seat ->
                    TabItem(
                        selected = index == state.playerIndex,
                        complete = game.isComplete(seat.playerId),
                        accent = playerColor(seat.playerId),
                        onClick = { viewModel.selectPlayer(index) },
                    ) { PlayerAvatar(seat.name, seat.playerId, size = 38.dp) }
                }
            }
        }
        if (state.order == ScoringOrder.BY_CATEGORY) {
            val category = state.category ?: return@Column
            val done = state.seats.count { game.value(it.playerId, category) != null }
            Banner(
                BannerData(
                    key = category,
                    color = category.color,
                    content = category.onColor,
                    icon = category.icon,
                    initial = null,
                    title = stringResource(category.label),
                    subtitle = stringResource(category.hint),
                    trailing = "$done/${state.seats.size}",
                )
            )
        } else {
            val seat = state.seat ?: return@Column
            Banner(
                BannerData(
                    key = seat.playerId,
                    color = playerColor(seat.playerId),
                    content = Color.White,
                    icon = null,
                    initial = seat.name.take(1).uppercase(),
                    title = seat.name,
                    subtitle = seat.wonder,
                    trailing = formatScore(game.total(seat.playerId)),
                )
            )
        }
    }
}

private data class BannerData(
    val key: Any,
    val color: Color,
    val content: Color,
    val icon: ImageVector?,
    val initial: String?,
    val title: String,
    val subtitle: String?,
    val trailing: String,
)

/** Colored banner naming what is being scored; slides when the category or player changes. */
@Composable
private fun Banner(data: BannerData) {
    val animatedColor by animateColorAsState(data.color, label = "bannerColor")
    val deep = lerp(animatedColor, Color.Black, 0.28f)
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(animatedColor, deep))),
    ) {
        MeanderBand(data.content.copy(alpha = 0.18f), Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 6.dp), height = 7.dp)
        AnimatedContent(
            targetState = data,
            contentKey = { it.key },
            transitionSpec = {
                (slideInHorizontally { it / 6 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 6 } + fadeOut())
            },
            label = "banner",
        ) { d ->
            Row(Modifier.padding(start = 14.dp, end = 18.dp, top = 12.dp, bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).background(d.content.copy(alpha = 0.16f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (d.icon != null) Icon(d.icon, null, tint = d.content, modifier = Modifier.size(26.dp))
                    if (d.initial != null) Text(d.initial, style = ScoreStyle, fontSize = 22.sp, color = d.content)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(d.title, style = MaterialTheme.typography.headlineSmall, color = d.content, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (d.subtitle != null) {
                        Text(
                            d.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = d.content.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Text(d.trailing, style = ScoreStyle, fontSize = 22.sp, color = d.content)
            }
        }
    }
}

@Composable
private fun TabItem(
    selected: Boolean,
    complete: Boolean,
    accent: Color,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val scale by animateFloatAsState(if (selected) 1.1f else 1f, spring(dampingRatio = 0.5f), label = "tabScale")
    val indicator by animateFloatAsState(if (selected) 1f else 0f, label = "tabIndicator")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box {
            Surface(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent,
                modifier = Modifier.padding(4.dp).graphicsLayer { scaleX = scale; scaleY = scale },
            ) {
                Box(Modifier.alpha(if (selected || !complete) 1f else 0.45f)) { content() }
            }
            if (complete) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                        .padding(1.5.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(10.dp))
                }
            }
        }
        Box(
            Modifier
                .width(22.dp)
                .height(4.dp)
                .graphicsLayer { scaleX = indicator; alpha = indicator }
                .background(accent, RoundedCornerShape(2.dp)),
        )
    }
}

/** The inner loop: players of the selected category, or categories of the selected player. */
@Composable
private fun CellList(state: ScoringUiState, viewModel: ScoringViewModel, modifier: Modifier) {
    val game = state.game ?: return
    val listState = rememberLazyListState()
    val selectedRow = if (state.order == ScoringOrder.BY_CATEGORY) state.playerIndex else state.categoryIndex
    LaunchedEffect(selectedRow, state.order, state.categoryIndex, state.playerIndex) {
        val info = listState.layoutInfo
        val item = info.visibleItemsInfo.firstOrNull { it.index == selectedRow }
        val fullyVisible = item != null && item.offset >= info.viewportStartOffset &&
            item.offset + item.size <= info.viewportEndOffset
        if (!fullyVisible) listState.animateScrollToItem((selectedRow - 1).coerceAtLeast(0))
    }
    // The crown follows whoever currently leads, once anyone has points.
    val best = state.seats.maxOfOrNull { game.total(it.playerId) } ?: 0
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (state.order == ScoringOrder.BY_CATEGORY) {
            val category = state.category ?: return@LazyColumn
            itemsIndexed(state.seats, key = { _, seat -> seat.playerId }) { index, seat ->
                val sheet = game.sheet(seat.playerId)
                val total = game.total(seat.playerId)
                CellRow(
                    leading = { LeaderAvatar(seat.name, seat.playerId, leading = best > 0 && total == best) },
                    title = seat.name,
                    subtitle = helperSummary(category, sheet) ?: seat.wonder,
                    value = cellText(state, category, sheet, selected = index == state.playerIndex),
                    valueCaption = coinsCaption(state.typingCoins, sheet),
                    trailing = formatScore(total),
                    selected = index == state.playerIndex,
                    accent = category.color,
                    onAccent = category.onColor,
                    onClick = { viewModel.select(state.categoryIndex, index) },
                )
            }
        } else {
            val seat = state.seat ?: return@LazyColumn
            val sheet = game.sheet(seat.playerId)
            itemsIndexed(state.categories, key = { _, c -> c.name }) { index, category ->
                val typingCoins = category == Category.TREASURY && state.treasuryAsCoins
                CellRow(
                    leading = { CategoryBadge(category) },
                    title = stringResource(category.label),
                    subtitle = helperSummary(category, sheet),
                    value = cellText(state, category, sheet, selected = index == state.categoryIndex),
                    valueCaption = coinsCaption(typingCoins, sheet),
                    trailing = null,
                    selected = index == state.categoryIndex,
                    accent = category.color,
                    onAccent = category.onColor,
                    onClick = { viewModel.select(index, state.playerIndex) },
                )
            }
        }
    }
}

@Composable
private fun LeaderAvatar(name: String, playerId: Long, leading: Boolean) {
    Box(contentAlignment = Alignment.TopCenter) {
        PlayerAvatar(name, playerId, modifier = Modifier.padding(top = 8.dp))
        AnimatedVisibility(leading, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
            Icon(GameIcons.Crown, stringResource(R.string.leader), tint = Brand.Gold, modifier = Modifier.size(16.dp))
        }
    }
}

/** What the value box of a cell shows: the typed draft, coins, points or a dash. */
private fun cellText(state: ScoringUiState, category: Category, sheet: PlayerSheet, selected: Boolean): String {
    val coins = category == Category.TREASURY && state.treasuryAsCoins
    if (selected && state.draft != null) return state.draft.replace("-", "−")
    if (coins && sheet.treasury != null) return sheet.treasury.coins.toString()
    if (coins && sheet.values[category] != null) return formatScore(sheet.values.getValue(category))
    return sheet.values[category]?.let(::formatScore) ?: "–"
}

/** Labels the value box as coins, unless the treasury holds points that were typed directly. */
@Composable
private fun coinsCaption(typingCoins: Boolean, sheet: PlayerSheet): String? =
    if (typingCoins && (sheet.treasury != null || sheet.values[Category.TREASURY] == null)) {
        stringResource(R.string.coins_caption)
    } else null

@Composable
private fun helperSummary(category: Category, sheet: PlayerSheet): String? = when (category) {
    Category.TREASURY -> sheet.treasury?.let {
        val points = formatScore(it.points)
        if (it.debt > 0) stringResource(R.string.treasury_summary_debt, it.coins, it.debt, points)
        else stringResource(R.string.treasury_summary, it.coins, points)
    } ?: sheet.values[category]?.let { stringResource(R.string.treasury_points_only) }
    Category.SCIENCE -> sheet.science?.let {
        val r = it.result
        stringResource(R.string.science_summary, r.compass, r.gear, r.tablet, r.sets)
    }
    Category.MILITARY, Category.NAVAL -> sheet.militaryInput(category)?.let {
        stringResource(R.string.military_summary, it.victoriesAge1 + it.victoriesAge2 + it.victoriesAge3, it.defeats)
    }
    else -> null
}

@Composable
private fun CellRow(
    leading: @Composable () -> Unit,
    title: String,
    subtitle: String?,
    value: String,
    valueCaption: String?,
    trailing: String?,
    selected: Boolean,
    accent: Color,
    onAccent: Color,
    onClick: () -> Unit,
) {
    val container by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerLow,
        label = "rowColor",
    )
    val empty = value == "–"
    val valueBackground by animateColorAsState(
        when {
            selected -> accent
            empty -> MaterialTheme.colorScheme.surfaceContainerHigh
            else -> accent.copy(alpha = 0.18f)
        },
        label = "valueColor",
    )
    val valueColor = when {
        selected -> onAccent
        empty -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.onSurface
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = container,
        border = if (selected) BorderStroke(2.dp, accent) else null,
        shadowElevation = if (selected) 3.dp else 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.heightIn(min = 60.dp).padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading()
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (trailing != null) {
                Text(
                    "Σ $trailing",
                    style = ScoreStyle,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 10.dp),
                )
            }
            Box(
                Modifier
                    .widthIn(min = 68.dp)
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(valueBackground)
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = value,
                        transitionSpec = { (slideInVertically { it / 2 } + fadeIn()) togetherWith (slideOutVertically { -it / 2 } + fadeOut()) },
                        label = "cellValue",
                    ) { text ->
                        Text(text.ifEmpty { " " }, style = ScoreStyle, fontSize = 24.sp, color = valueColor)
                    }
                    if (valueCaption != null) {
                        Text(valueCaption, style = MaterialTheme.typography.labelSmall, color = valueColor.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}

/**
 * Everything for entering the selected cell. Counting aids sit inline with the pad so typing a
 * total and counting tokens or symbols work on the same screen without switching.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputPanel(
    state: ScoringUiState,
    viewModel: ScoringViewModel,
    keyHeight: Dp,
    modifier: Modifier = Modifier,
) {
    val category = state.category ?: return
    val game = state.game ?: return
    val sheet = state.seat?.let { game.sheet(it.playerId) }
    val countingSymbols = category == Category.SCIENCE && state.scienceAsSymbols
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, modifier = modifier) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth().heightIn(min = 40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (category) {
                    Category.MILITARY, Category.NAVAL -> TokenCounterRow(
                        input = sheet?.militaryInput(category),
                        haptics = state.haptics,
                        onAdd = viewModel::addToken,
                    )
                    Category.TREASURY -> {
                        ModeSwitch(
                            first = stringResource(R.string.treasury_mode_coins),
                            second = stringResource(R.string.treasury_mode_points),
                            firstSelected = state.treasuryAsCoins,
                            onSelect = viewModel::setTreasuryAsCoins,
                            modifier = Modifier.weight(1f),
                        )
                        if (Expansion.CITIES in game.expansions) {
                            MiniStepper(
                                label = stringResource(R.string.treasury_debt),
                                value = sheet?.treasury?.debt ?: 0,
                                onChange = viewModel::setDebt,
                                haptics = state.haptics,
                            )
                        }
                    }
                    Category.SCIENCE -> ModeSwitch(
                        first = stringResource(R.string.science_mode_symbols),
                        second = stringResource(R.string.science_mode_points),
                        firstSelected = state.scienceAsSymbols,
                        onSelect = viewModel::setScienceAsSymbols,
                        modifier = Modifier.weight(1f),
                    )
                    else -> Text(
                        state.seat?.let { seat ->
                            stringResource(R.string.entering_for, stringResource(category.label), seat.name)
                        }.orEmpty(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            if (countingSymbols) {
                ScienceCounter(
                    input = sheet?.science,
                    withAristotle = Expansion.LEADERS in game.expansions,
                    haptics = state.haptics,
                    height = keyHeight * 4 + 18.dp,
                    onChange = viewModel::updateScience,
                ) {
                    StepKeys(
                        onPrevious = viewModel::onPrevious,
                        onNext = viewModel::onNext,
                        isLastCell = state.isLastCell,
                        accent = category.color,
                        onAccent = category.onColor,
                        haptics = state.haptics,
                        height = keyHeight,
                    )
                }
            } else {
                NumberPad(
                    onDigit = viewModel::onDigit,
                    onBackspace = viewModel::onBackspace,
                    onToggleSign = viewModel::onToggleSign,
                    onClear = viewModel::onClear,
                    onPrevious = viewModel::onPrevious,
                    onNext = viewModel::onNext,
                    isLastCell = state.isLastCell,
                    accent = category.color,
                    onAccent = category.onColor,
                    haptics = state.haptics,
                    keyHeight = keyHeight,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModeSwitch(first: String, second: String, firstSelected: Boolean, onSelect: (Boolean) -> Unit, modifier: Modifier) {
    SingleChoiceSegmentedButtonRow(modifier) {
        SegmentedButton(
            selected = firstSelected,
            onClick = { onSelect(true) },
            shape = SegmentedButtonDefaults.itemShape(0, 2),
        ) { Text(first, maxLines = 1) }
        SegmentedButton(
            selected = !firstSelected,
            onClick = { onSelect(false) },
            shape = SegmentedButtonDefaults.itemShape(1, 2),
        ) { Text(second, maxLines = 1) }
    }
}
