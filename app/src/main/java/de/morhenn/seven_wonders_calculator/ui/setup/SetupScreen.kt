package de.morhenn.seven_wonders_calculator.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.morhenn.seven_wonders_calculator.R
import de.morhenn.seven_wonders_calculator.domain.Category
import de.morhenn.seven_wonders_calculator.domain.Expansion
import de.morhenn.seven_wonders_calculator.domain.Seat
import de.morhenn.seven_wonders_calculator.domain.Wonders
import de.morhenn.seven_wonders_calculator.ui.components.CategoryBadge
import de.morhenn.seven_wonders_calculator.ui.components.GameIcons
import de.morhenn.seven_wonders_calculator.ui.components.SectionHeader
import de.morhenn.seven_wonders_calculator.ui.components.icon
import de.morhenn.seven_wonders_calculator.ui.components.PlayerAvatar
import de.morhenn.seven_wonders_calculator.ui.components.label
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SetupScreen(
    onBack: () -> Unit,
    onStarted: (Long) -> Unit,
    viewModel: SetupViewModel = viewModel(factory = SetupViewModel.Factory),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    val nameFocus = remember { FocusRequester() }
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(listState) { from, to ->
        viewModel.move(from.key as Long, to.key as Long)
        haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
    }

    LaunchedEffect(viewModel) { viewModel.started.collect(onStarted) }

    fun submitName() {
        viewModel.addByName(name)
        name = ""
        // Keep the keyboard up so a whole group can be typed in one go.
        nameFocus.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.new_game)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
                },
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                Button(
                    onClick = viewModel::start,
                    enabled = state.canStart,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(16.dp).height(56.dp),
                ) {
                    Text(
                        if (state.canStart) pluralStringResource(R.plurals.start_game, state.seats.size, state.seats.size)
                        else stringResource(R.string.add_players_first),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "header") {
                SectionHeader(stringResource(R.string.players)) {
                    Text(
                        "${state.seats.size}/$MAX_PLAYERS",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (state.seats.size > 1) {
                    Text(
                        stringResource(R.string.seating_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (state.lastGroup.isNotEmpty()) {
                item(key = "last_group") {
                    AssistChip(
                        onClick = viewModel::useLastGroup,
                        label = {
                            Text(
                                stringResource(R.string.last_group, state.lastGroup.joinToString(", ") { it.name }),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        leadingIcon = { Icon(Icons.Filled.History, null, Modifier.size(AssistChipDefaults.IconSize)) },
                    )
                }
            }
            itemsIndexed(state.seats, key = { _, seat -> seat.playerId }) { index, seat ->
                ReorderableItem(reorderState, key = seat.playerId) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "dragElevation")
                    SeatRow(
                        seat = seat,
                        wonders = Wonders.available(state.expansions),
                        onRemove = { viewModel.remove(index) },
                        onWonder = { viewModel.setWonder(index, it) },
                        elevation = elevation,
                        handle = {
                            Icon(
                                Icons.Filled.DragHandle,
                                contentDescription = stringResource(R.string.reorder),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .draggableHandle(
                                        onDragStarted = { haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate) },
                                        onDragStopped = { haptic.performHapticFeedback(HapticFeedbackType.GestureEnd) },
                                    )
                                    .padding(12.dp),
                            )
                        },
                        modifier = Modifier.longPressDraggableHandle(
                            onDragStarted = { haptic.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate) },
                            onDragStopped = { haptic.performHapticFeedback(HapticFeedbackType.GestureEnd) },
                        ),
                    )
                }
            }
            if (!state.isFull) {
                item(key = "add_field") {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it.take(24) },
                        label = { Text(stringResource(R.string.add_player)) },
                        leadingIcon = { Icon(Icons.Filled.PersonAdd, null) },
                        trailingIcon = {
                            if (name.isNotBlank()) {
                                IconButton(onClick = ::submitName) { Icon(Icons.Filled.Add, stringResource(R.string.add)) }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submitName() }),
                        // Hardware keyboards (tablets, Chromebooks) send Enter instead of the IME action.
                        modifier = Modifier.fillMaxWidth().focusRequester(nameFocus).onPreviewKeyEvent { event ->
                            if (event.key == Key.Enter && event.type == KeyEventType.KeyUp) {
                                submitName()
                                true
                            } else {
                                event.key == Key.Enter
                            }
                        },
                    )
                }
                if (state.suggestions.isNotEmpty()) {
                    item(key = "suggestions") {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.suggestions.forEach { player ->
                                AssistChip(
                                    onClick = { viewModel.add(player) },
                                    label = { Text(player.name) },
                                    leadingIcon = { Icon(Icons.Filled.Add, null, Modifier.size(AssistChipDefaults.IconSize)) },
                                )
                            }
                        }
                    }
                }
            }
            item(key = "expansions") {
                SectionHeader(stringResource(R.string.expansions), Modifier.padding(top = 16.dp, bottom = 6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Expansion.entries.forEach { expansion ->
                        val selected = expansion in state.expansions
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.toggleExpansion(expansion) },
                            label = { Text(stringResource(expansion.label)) },
                            leadingIcon = {
                                Icon(if (selected) Icons.Filled.Check else expansion.icon, null, Modifier.size(18.dp))
                            },
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.categories_in_game),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Category.activeFor(state.expansions).forEach { category ->
                        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceContainerLow) {
                            Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                CategoryBadge(category, size = 24.dp)
                                Text(
                                    stringResource(category.label),
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(start = 6.dp, end = 6.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeatRow(
    seat: Seat,
    wonders: List<String>,
    onRemove: () -> Unit,
    onWonder: (String?) -> Unit,
    elevation: Dp,
    handle: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = elevation,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            handle()
            PlayerAvatar(seat.name, seat.playerId)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(seat.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Box {
                    TextButton(
                        onClick = { menuOpen = true },
                        contentPadding = PaddingValues(horizontal = 0.dp),
                        modifier = Modifier.height(32.dp),
                    ) {
                        Icon(GameIcons.Pyramid, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(seat.wonder ?: stringResource(R.string.choose_wonder), style = MaterialTheme.typography.labelLarge)
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.no_wonder)) },
                            onClick = { menuOpen = false; onWonder(null) },
                        )
                        wonders.forEach { wonder ->
                            DropdownMenuItem(
                                text = { Text(wonder) },
                                trailingIcon = { if (wonder == seat.wonder) Icon(Icons.Filled.Check, null) },
                                onClick = { menuOpen = false; onWonder(wonder) },
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onRemove) { Icon(Icons.Filled.Close, stringResource(R.string.remove)) }
        }
    }
}
