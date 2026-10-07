package de.morhenn.seven_wonders_calculator.ui.scoring

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import de.morhenn.seven_wonders_calculator.ScoringRoute
import de.morhenn.seven_wonders_calculator.container
import de.morhenn.seven_wonders_calculator.data.GameRepository
import de.morhenn.seven_wonders_calculator.data.ScoringOrder
import de.morhenn.seven_wonders_calculator.data.SettingsRepository
import de.morhenn.seven_wonders_calculator.domain.Category
import de.morhenn.seven_wonders_calculator.domain.ConflictToken
import de.morhenn.seven_wonders_calculator.domain.GameState
import de.morhenn.seven_wonders_calculator.domain.MilitaryInput
import de.morhenn.seven_wonders_calculator.domain.PlayerSheet
import de.morhenn.seven_wonders_calculator.domain.ScienceInput
import de.morhenn.seven_wonders_calculator.domain.Seat
import de.morhenn.seven_wonders_calculator.domain.TreasuryInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScoringUiState(
    val game: GameState? = null,
    val order: ScoringOrder = ScoringOrder.BY_CATEGORY,
    val categoryIndex: Int = 0,
    val playerIndex: Int = 0,
    /** Text being typed into the selected cell; null while the cell shows its stored value. */
    val draft: String? = null,
    /** In the treasury, typed numbers are coins (converted to points) unless switched off. */
    val treasuryAsCoins: Boolean = true,
    /** In science, the panel counts symbols unless switched to typing points. */
    val scienceAsSymbols: Boolean = true,
    val canUndo: Boolean = false,
    val haptics: Boolean = true,
) {
    val categories: List<Category> get() = game?.categories.orEmpty()
    val seats: List<Seat> get() = game?.seats.orEmpty()
    val category: Category? get() = categories.getOrNull(categoryIndex)
    val seat: Seat? get() = seats.getOrNull(playerIndex)

    val isLastCell: Boolean
        get() = categoryIndex == categories.lastIndex && playerIndex == seats.lastIndex

    val typingCoins: Boolean get() = category == Category.TREASURY && treasuryAsCoins
}

sealed interface ScoringEvent {
    data class ShowResults(val gameId: Long) : ScoringEvent
}

class ScoringViewModel(
    savedStateHandle: SavedStateHandle,
    private val games: GameRepository,
    private val settings: SettingsRepository,
    /** Outlives this ViewModel, so the final save is not cancelled when the screen closes. */
    private val appScope: CoroutineScope,
) : ViewModel() {
    val gameId: Long = savedStateHandle.toRoute<ScoringRoute>().gameId

    private val _state = MutableStateFlow(ScoringUiState())
    val state: StateFlow<ScoringUiState> = _state.asStateFlow()

    private val _events = Channel<ScoringEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val undoStack = ArrayDeque<GameState>()
    /** Set when the cursor moves; the next edit then records an undo step. */
    private var undoArmed = true

    init {
        viewModelScope.launch {
            val prefs = settings.current()
            val entity = games.get(gameId) ?: return@launch
            val game = entity.state
            // Resume at the first empty cell in the preferred order.
            val (category, player) = firstEmptyCell(game, prefs.scoringOrder)
            _state.update {
                it.copy(game = game, order = prefs.scoringOrder, categoryIndex = category, playerIndex = player, haptics = prefs.haptics)
            }
            // Persist every change; conflate so a burst of key presses ends in one final write.
            _state.map { it.game }.filterNotNull().distinctUntilChanged().drop(1).conflate().collect { games.save(gameId, it) }
        }
    }

    private fun firstEmptyCell(game: GameState, order: ScoringOrder): Pair<Int, Int> {
        val cells = cellOrder(game.categories.size, game.seats.size, order)
        val empty = cells.firstOrNull { (c, p) -> game.value(game.seats[p].playerId, game.categories[c]) == null }
        return empty ?: (0 to 0)
    }

    private fun cellOrder(categories: Int, players: Int, order: ScoringOrder): List<Pair<Int, Int>> =
        if (order == ScoringOrder.BY_CATEGORY) {
            (0 until categories).flatMap { c -> (0 until players).map { p -> c to p } }
        } else {
            (0 until players).flatMap { p -> (0 until categories).map { c -> c to p } }
        }

    // region editing

    private fun editGame(transform: (GameState) -> GameState) {
        val current = _state.value.game ?: return
        val updated = transform(current)
        if (updated == current) return
        if (undoArmed) {
            undoStack.addLast(current)
            if (undoStack.size > 100) undoStack.removeFirst()
            undoArmed = false
        }
        _state.update { it.copy(game = updated, canUndo = true) }
    }

    /** Applies the typed text to the selected cell. */
    private fun commitDraft(draft: String) {
        val s = _state.value
        val seat = s.seat ?: return
        val category = s.category ?: return
        val number = draft.toIntOrNull()
        _state.update { it.copy(draft = draft) }
        editGame { game ->
            game.updateSheet(seat.playerId) { sheet ->
                if (s.typingCoins) {
                    if (number == null) sheet.withValue(category, null)
                    else sheet.withTreasury(TreasuryInput(coins = number, debt = sheet.treasury?.debt ?: 0))
                } else {
                    sheet.withValue(category, number)
                }
            }
        }
    }

    /** The text the cell currently shows, as a starting point for editing. */
    private fun currentText(): String {
        val s = _state.value
        s.draft?.let { return it }
        val seat = s.seat ?: return ""
        val sheet = s.game?.sheet(seat.playerId) ?: return ""
        return if (s.typingCoins) sheet.treasury?.coins?.toString().orEmpty()
        else sheet.values[s.category]?.toString().orEmpty()
    }

    fun onDigit(digit: Int) {
        // A fresh cell starts over, like a calculator display; a minus typed first is kept.
        val base = _state.value.draft ?: ""
        if (base.trimStart('-').length >= 3) return
        val next = if (base == "0") "$digit" else if (base == "-0") "-$digit" else base + digit
        commitDraft(next)
    }

    fun onBackspace() {
        commitDraft(currentText().dropLast(1))
    }

    fun onToggleSign() {
        if (_state.value.typingCoins) return
        val text = currentText()
        val toggled = if (text.startsWith("-")) text.drop(1) else "-$text"
        commitDraft(toggled)
    }

    fun onClear() {
        commitDraft("")
    }

    fun setTreasuryAsCoins(asCoins: Boolean) {
        _state.update { it.copy(treasuryAsCoins = asCoins, draft = null) }
    }

    fun setScienceAsSymbols(asSymbols: Boolean) {
        _state.update { it.copy(scienceAsSymbols = asSymbols, draft = null) }
    }

    /** Counts a conflict token for the selected player. A typed total is replaced by the tokens. */
    fun addToken(token: ConflictToken, delta: Int) = editSheet { sheet ->
        val category = _state.value.category ?: return@editSheet sheet
        sheet.withMilitary(category, (sheet.militaryInput(category) ?: MilitaryInput()).plus(token, delta))
    }

    fun setDebt(debt: Int) = editSheet { sheet ->
        val coins = sheet.treasury?.coins ?: 0
        sheet.withTreasury(TreasuryInput(coins = coins, debt = debt.coerceIn(0, 99)))
    }

    fun updateScience(transform: (ScienceInput) -> ScienceInput) = editSheet { sheet ->
        sheet.withScience(transform(sheet.science ?: ScienceInput()))
    }

    private fun editSheet(transform: (PlayerSheet) -> PlayerSheet) {
        val seat = _state.value.seat ?: return
        _state.update { it.copy(draft = null) }
        editGame { it.updateSheet(seat.playerId, transform) }
    }

    fun undo() {
        val previous = undoStack.removeLastOrNull() ?: return
        undoArmed = true
        _state.update { it.copy(game = previous, draft = null, canUndo = undoStack.isNotEmpty()) }
    }

    // endregion

    // region navigation

    fun select(categoryIndex: Int, playerIndex: Int) {
        undoArmed = true
        _state.update { it.copy(categoryIndex = categoryIndex, playerIndex = playerIndex, draft = null) }
    }

    fun selectCategory(index: Int) = select(index, if (_state.value.order == ScoringOrder.BY_CATEGORY) 0 else _state.value.playerIndex)

    fun selectPlayer(index: Int) = select(if (_state.value.order == ScoringOrder.BY_PLAYER) 0 else _state.value.categoryIndex, index)

    fun onNext() {
        val s = _state.value
        if (s.game == null) return
        if (s.isLastCell) {
            finish()
            return
        }
        step(+1)
    }

    fun onPrevious() = step(-1)

    private fun step(delta: Int) {
        val s = _state.value
        val cells = cellOrder(s.categories.size, s.seats.size, s.order)
        val index = cells.indexOf(s.categoryIndex to s.playerIndex)
        val (c, p) = cells.getOrNull(index + delta) ?: return
        select(c, p)
    }

    fun setOrder(order: ScoringOrder) {
        _state.update { it.copy(order = order, draft = null) }
        viewModelScope.launch { settings.setScoringOrder(order) }
    }

    fun finish() {
        val game = _state.value.game ?: return
        viewModelScope.launch {
            games.save(gameId, game)
            games.finish(gameId)
            _events.send(ScoringEvent.ShowResults(gameId))
        }
    }

    // endregion

    override fun onCleared() {
        // A key pressed just before leaving may still be waiting for its write.
        val game = _state.value.game ?: return
        appScope.launch { games.save(gameId, game) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ScoringViewModel(createSavedStateHandle(), container.games, container.settings, container.appScope)
            }
        }
    }
}
