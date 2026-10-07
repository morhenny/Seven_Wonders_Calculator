package de.morhenn.seven_wonders_calculator.ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.morhenn.seven_wonders_calculator.BuildConfig
import de.morhenn.seven_wonders_calculator.R
import de.morhenn.seven_wonders_calculator.ui.components.SectionHeader
import de.morhenn.seven_wonders_calculator.container
import de.morhenn.seven_wonders_calculator.data.PlayerEntity
import de.morhenn.seven_wonders_calculator.data.PlayerRepository
import de.morhenn.seven_wonders_calculator.data.ScoringOrder
import de.morhenn.seven_wonders_calculator.data.Settings
import de.morhenn.seven_wonders_calculator.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: SettingsRepository,
    private val players: PlayerRepository,
    private val resetAll: suspend () -> Unit,
) : ViewModel() {
    val state: StateFlow<Settings> = settings.settings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Settings())
    val playerList: StateFlow<List<PlayerEntity>> =
        players.players.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setOrder(order: ScoringOrder) = viewModelScope.launch { settings.setScoringOrder(order) }
    fun setHaptics(enabled: Boolean) = viewModelScope.launch { settings.setHaptics(enabled) }
    fun rename(id: Long, name: String, result: (Boolean) -> Unit) = viewModelScope.launch { result(players.rename(id, name)) }
    fun remove(id: Long) = viewModelScope.launch { players.hide(id) }
    fun reset(done: () -> Unit) = viewModelScope.launch {
        resetAll()
        done()
    }

    companion object {
        val Factory = viewModelFactory { initializer { SettingsViewModel(container.settings, container.players, container::resetAll) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)) {
    val settings by viewModel.state.collectAsStateWithLifecycle()
    val players by viewModel.playerList.collectAsStateWithLifecycle()
    var renaming by remember { mutableStateOf<PlayerEntity?>(null) }
    var removing by remember { mutableStateOf<PlayerEntity?>(null) }
    var confirmReset by remember { mutableStateOf(false) }
    // Read once; changing the language recreates the activity and this screen with it.
    val language = remember { AppLanguage.current() }
    val snackbar = remember { SnackbarHostState() }
    val resetDone = stringResource(R.string.reset_done)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 24.dp)) {
            item { Header(stringResource(R.string.settings_language)) }
            AppLanguage.entries.forEach { option ->
                item(key = "lang_" + option.name) {
                    val label = when (option) {
                        AppLanguage.SYSTEM -> stringResource(R.string.language_system)
                        // Language names in their own language, so they can be found in any locale.
                        AppLanguage.GERMAN -> "Deutsch"
                        AppLanguage.ENGLISH -> "English"
                    }
                    ListItem(
                        headlineContent = { Text(label) },
                        leadingContent = { RadioButton(selected = option == language, onClick = null) },
                        modifier = Modifier.selectable(
                            selected = option == language,
                            role = Role.RadioButton,
                            onClick = { if (option != language) AppLanguage.apply(option) },
                        ),
                    )
                }
            }
            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            item { Header(stringResource(R.string.settings_scoring_order)) }
            item {
                OrderOption(
                    ScoringOrder.BY_CATEGORY, settings.scoringOrder,
                    stringResource(R.string.order_by_category), stringResource(R.string.order_by_category_hint), viewModel::setOrder,
                )
            }
            item {
                OrderOption(
                    ScoringOrder.BY_PLAYER, settings.scoringOrder,
                    stringResource(R.string.order_by_player), stringResource(R.string.order_by_player_hint), viewModel::setOrder,
                )
            }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_haptics)) },
                    supportingContent = { Text(stringResource(R.string.settings_haptics_hint)) },
                    trailingContent = { Switch(checked = settings.haptics, onCheckedChange = { viewModel.setHaptics(it) }) },
                )
            }
            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            item { Header(stringResource(R.string.settings_players)) }
            if (players.isEmpty()) {
                item {
                    ListItem(headlineContent = {
                        Text(stringResource(R.string.settings_no_players), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    })
                }
            }
            items(players, key = { it.id }) { player ->
                ListItem(
                    headlineContent = { Text(player.name) },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { renaming = player }) { Icon(Icons.Outlined.Edit, stringResource(R.string.rename)) }
                            IconButton(onClick = { removing = player }) { Icon(Icons.Outlined.Delete, stringResource(R.string.remove)) }
                        }
                    },
                )
            }
            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.reset_title), color = MaterialTheme.colorScheme.error) },
                    supportingContent = { Text(stringResource(R.string.reset_hint)) },
                    leadingContent = { Icon(Icons.Outlined.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable { confirmReset = true },
                )
            }
            item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.app_name)) },
                    supportingContent = {
                        Column {
                            Text(stringResource(R.string.settings_version, BuildConfig.VERSION_NAME))
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.disclaimer), style = MaterialTheme.typography.bodySmall)
                        }
                    },
                )
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            icon = { Icon(Icons.Outlined.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.reset_confirm_title)) },
            text = { Text(stringResource(R.string.reset_confirm_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmReset = false
                        viewModel.reset { scope.launch { snackbar.showSnackbar(resetDone) } }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(R.string.reset_confirm_action)) }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }

    renaming?.let { player ->
        var name by remember(player.id) { mutableStateOf(player.name) }
        var error by remember(player.id) { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { renaming = null },
            title = { Text(stringResource(R.string.rename)) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(24); error = false },
                    singleLine = true,
                    isError = error,
                    supportingText = if (error) {
                        { Text(stringResource(R.string.rename_error)) }
                    } else null,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.rename(player.id, name) { ok -> if (ok) renaming = null else error = true }
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { renaming = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
    removing?.let { player ->
        AlertDialog(
            onDismissRequest = { removing = null },
            title = { Text(stringResource(R.string.remove_player_title, player.name)) },
            text = { Text(stringResource(R.string.remove_player_text)) },
            confirmButton = {
                TextButton(onClick = { viewModel.remove(player.id); removing = null }) { Text(stringResource(R.string.remove)) }
            },
            dismissButton = { TextButton(onClick = { removing = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun Header(text: String) {
    SectionHeader(text, Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 6.dp))
}

@Composable
private fun OrderOption(
    option: ScoringOrder,
    current: ScoringOrder,
    title: String,
    hint: String,
    onSelect: (ScoringOrder) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(hint) },
        leadingContent = { RadioButton(selected = option == current, onClick = null) },
        modifier = Modifier.selectable(selected = option == current, role = Role.RadioButton, onClick = { onSelect(option) }),
    )
}
