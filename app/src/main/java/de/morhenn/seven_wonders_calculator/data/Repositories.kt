package de.morhenn.seven_wonders_calculator.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import de.morhenn.seven_wonders_calculator.domain.Expansion
import de.morhenn.seven_wonders_calculator.domain.GameState
import de.morhenn.seven_wonders_calculator.domain.Seat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class PlayerRepository(private val dao: PlayerDao) {
    val players: Flow<List<PlayerEntity>> = dao.observeVisible()

    /** Returns the existing player with that name (case-insensitive) or creates one. */
    suspend fun getOrCreate(name: String): PlayerEntity {
        val trimmed = name.trim()
        dao.findByName(trimmed)?.let { existing ->
            if (existing.hidden) dao.update(existing.copy(hidden = false))
            return existing.copy(hidden = false)
        }
        val id = dao.insert(PlayerEntity(name = trimmed))
        return PlayerEntity(id = id, name = trimmed)
    }

    /** Returns false if another player already has that name. */
    suspend fun rename(id: Long, name: String): Boolean {
        val trimmed = name.trim()
        val clash = dao.findByName(trimmed)
        if (trimmed.isEmpty() || (clash != null && clash.id != id)) return false
        dao.get(id)?.let { dao.update(it.copy(name = trimmed)) }
        return true
    }

    suspend fun hide(id: Long) {
        dao.get(id)?.let { dao.update(it.copy(hidden = true)) }
    }
}

class GameRepository(private val dao: GameDao) {
    val games: Flow<List<GameEntity>> = dao.observeAll()

    fun observe(id: Long): Flow<GameEntity?> = dao.observe(id)

    suspend fun get(id: Long): GameEntity? = dao.get(id)

    suspend fun create(seats: List<Seat>, expansions: Set<Expansion>): Long =
        dao.insert(GameEntity(startedAt = System.currentTimeMillis(), state = GameState(seats, expansions)))

    suspend fun save(id: Long, state: GameState) = dao.updateState(id, state)

    suspend fun finish(id: Long) = dao.setFinished(id, System.currentTimeMillis())

    suspend fun reopen(id: Long) = dao.setFinished(id, null)

    suspend fun delete(id: Long) = dao.delete(id)
}

enum class ScoringOrder { BY_CATEGORY, BY_PLAYER }

data class Settings(
    val scoringOrder: ScoringOrder = ScoringOrder.BY_CATEGORY,
    val haptics: Boolean = true,
    val lastExpansions: Set<Expansion> = emptySet(),
    val lastPlayerIds: List<Long> = emptyList(),
)

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {
    private object Keys {
        val order = stringPreferencesKey("scoring_order")
        val haptics = booleanPreferencesKey("haptics")
        val expansions = stringPreferencesKey("last_expansions")
        val players = stringPreferencesKey("last_players")
    }

    val settings: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            scoringOrder = prefs[Keys.order]?.let { runCatching { ScoringOrder.valueOf(it) }.getOrNull() }
                ?: ScoringOrder.BY_CATEGORY,
            haptics = prefs[Keys.haptics] ?: true,
            lastExpansions = prefs[Keys.expansions].orEmpty().split(',')
                .mapNotNull { runCatching { Expansion.valueOf(it) }.getOrNull() }.toSet(),
            lastPlayerIds = prefs[Keys.players].orEmpty().split(',').mapNotNull { it.toLongOrNull() },
        )
    }

    suspend fun current(): Settings = settings.first()

    suspend fun setScoringOrder(order: ScoringOrder) = context.dataStore.edit { it[Keys.order] = order.name }

    suspend fun setHaptics(enabled: Boolean) = context.dataStore.edit { it[Keys.haptics] = enabled }

    suspend fun clear() = context.dataStore.edit { it.clear() }

    suspend fun rememberSetup(playerIds: List<Long>, expansions: Set<Expansion>) = context.dataStore.edit {
        it[Keys.players] = playerIds.joinToString(",")
        it[Keys.expansions] = expansions.joinToString(",") { e -> e.name }
    }
}
