package de.morhenn.seven_wonders_calculator.ui.setup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import de.morhenn.seven_wonders_calculator.SetupRoute
import de.morhenn.seven_wonders_calculator.container
import de.morhenn.seven_wonders_calculator.data.GameRepository
import de.morhenn.seven_wonders_calculator.data.PlayerEntity
import de.morhenn.seven_wonders_calculator.data.PlayerRepository
import de.morhenn.seven_wonders_calculator.data.SettingsRepository
import de.morhenn.seven_wonders_calculator.domain.Expansion
import de.morhenn.seven_wonders_calculator.domain.Seat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val MAX_PLAYERS = 8

data class SetupUiState(
    val seats: List<Seat> = emptyList(),
    val expansions: Set<Expansion> = emptySet(),
    /** Saved players that are not seated yet. */
    val suggestions: List<PlayerEntity> = emptyList(),
    /** The previous group, offered as a one-tap shortcut when it differs from the current seats. */
    val lastGroup: List<PlayerEntity> = emptyList(),
) {
    val canStart: Boolean get() = seats.isNotEmpty()
    val isFull: Boolean get() = seats.size >= MAX_PLAYERS
}

private data class Draft(val seats: List<Seat> = emptyList(), val expansions: Set<Expansion> = emptySet())

class SetupViewModel(
    savedStateHandle: SavedStateHandle,
    private val players: PlayerRepository,
    private val games: GameRepository,
    private val settings: SettingsRepository,
) : ViewModel() {
    private val route = savedStateHandle.toRoute<SetupRoute>()
    private val draft = MutableStateFlow(Draft())
    private val lastPlayerIds = MutableStateFlow<List<Long>>(emptyList())

    private val _started = Channel<Long>(Channel.BUFFERED)
    val started = _started.receiveAsFlow()

    val state: StateFlow<SetupUiState> = combine(draft, players.players, lastPlayerIds) { draft, all, lastIds ->
        val seated = draft.seats.map { it.playerId }.toSet()
        val byId = all.associateBy { it.id }
        val lastGroup = lastIds.mapNotNull { byId[it] }
        SetupUiState(
            seats = draft.seats,
            expansions = draft.expansions,
            suggestions = all.filter { it.id !in seated },
            lastGroup = if (lastGroup.map { it.id } == draft.seats.map { it.playerId }) emptyList() else lastGroup,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SetupUiState())

    init {
        viewModelScope.launch {
            val prefs = settings.current()
            lastPlayerIds.value = prefs.lastPlayerIds
            val previous = route.fromGameId?.let { games.get(it) }?.state
            draft.value = if (previous != null) {
                // Seats store the name at game time; a rematch should use today's names.
                val current = players.players.first().associateBy { it.id }
                Draft(
                    previous.seats.map { it.copy(name = current[it.playerId]?.name ?: it.name, wonder = null) },
                    previous.expansions,
                )
            } else {
                Draft(expansions = prefs.lastExpansions)
            }
        }
    }

    fun addByName(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { add(players.getOrCreate(name)) }
    }

    fun add(player: PlayerEntity) {
        draft.update { d ->
            if (d.seats.size >= MAX_PLAYERS || d.seats.any { it.playerId == player.id }) d
            else d.copy(seats = d.seats + Seat(player.id, player.name))
        }
    }

    fun useLastGroup() {
        val group = state.value.lastGroup
        draft.update { d -> d.copy(seats = group.take(MAX_PLAYERS).map { Seat(it.id, it.name) }) }
    }

    fun remove(index: Int) = draft.update { d -> d.copy(seats = d.seats.filterIndexed { i, _ -> i != index }) }

    /** Moves the seat of player [fromId] to where [toId] sits (drag and drop). */
    fun move(fromId: Long, toId: Long) = draft.update { d ->
        val from = d.seats.indexOfFirst { it.playerId == fromId }
        val to = d.seats.indexOfFirst { it.playerId == toId }
        if (from < 0 || to < 0) return@update d
        d.copy(seats = d.seats.toMutableList().apply { add(to, removeAt(from)) })
    }

    fun setWonder(index: Int, wonder: String?) = draft.update { d ->
        d.copy(seats = d.seats.mapIndexed { i, seat -> if (i == index) seat.copy(wonder = wonder) else seat })
    }

    fun toggleExpansion(expansion: Expansion) = draft.update { d ->
        d.copy(expansions = if (expansion in d.expansions) d.expansions - expansion else d.expansions + expansion)
    }

    private var starting = false

    fun start() {
        val d = draft.value
        // A quick double tap must not create two games.
        if (d.seats.isEmpty() || starting) return
        starting = true
        viewModelScope.launch {
            val id = games.create(d.seats, d.expansions)
            settings.rememberSetup(d.seats.map { it.playerId }, d.expansions)
            _started.send(id)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                SetupViewModel(createSavedStateHandle(), container.players, container.games, container.settings)
            }
        }
    }
}
