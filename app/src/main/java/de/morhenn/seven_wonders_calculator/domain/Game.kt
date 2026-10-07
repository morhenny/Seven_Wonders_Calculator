package de.morhenn.seven_wonders_calculator.domain

import kotlinx.serialization.Serializable

@Serializable
data class Seat(
    val playerId: Long,
    /** Snapshot of the name at game time, so history survives renames and deletions. */
    val name: String,
    val wonder: String? = null,
)

/** One player's sheet. A category missing from [values] has not been entered yet. */
@Serializable
data class PlayerSheet(
    val values: Map<Category, Int> = emptyMap(),
    val military: MilitaryInput? = null,
    val naval: MilitaryInput? = null,
    val treasury: TreasuryInput? = null,
    val science: ScienceInput? = null,
) {
    fun total(categories: Collection<Category>): Int = categories.sumOf { values[it] ?: 0 }

    /** Coins are only known when the treasury helper was used; they decide ties. */
    val coins: Int? get() = treasury?.coins

    /** Sets a plain value. Helper details are dropped when they no longer match the number. */
    fun withValue(category: Category, value: Int?): PlayerSheet {
        val newValues = if (value == null) values - category else values + (category to value)
        return when (category) {
            Category.MILITARY -> copy(values = newValues, military = military?.takeIf { it.points == value })
            Category.NAVAL -> copy(values = newValues, naval = naval?.takeIf { it.points == value })
            Category.TREASURY -> copy(values = newValues, treasury = treasury?.takeIf { it.points == value })
            Category.SCIENCE -> copy(values = newValues, science = science?.takeIf { it.points == value })
            else -> copy(values = newValues)
        }
    }

    fun withMilitary(category: Category, input: MilitaryInput): PlayerSheet = when (category) {
        Category.NAVAL -> copy(values = values + (category to input.points), naval = input)
        else -> copy(values = values + (Category.MILITARY to input.points), military = input)
    }

    fun withTreasury(input: TreasuryInput): PlayerSheet =
        copy(values = values + (Category.TREASURY to input.points), treasury = input)

    fun withScience(input: ScienceInput): PlayerSheet =
        copy(values = values + (Category.SCIENCE to input.points), science = input)

    fun militaryInput(category: Category): MilitaryInput? = if (category == Category.NAVAL) naval else military
}

@Serializable
data class GameState(
    val seats: List<Seat>,
    val expansions: Set<Expansion>,
    val sheets: Map<Long, PlayerSheet> = emptyMap(),
) {
    val categories: List<Category> get() = Category.activeFor(expansions)

    fun sheet(playerId: Long): PlayerSheet = sheets[playerId] ?: PlayerSheet()

    fun value(playerId: Long, category: Category): Int? = sheet(playerId).values[category]

    fun total(playerId: Long): Int = sheet(playerId).total(categories)

    fun updateSheet(playerId: Long, transform: (PlayerSheet) -> PlayerSheet): GameState =
        copy(sheets = sheets + (playerId to transform(sheet(playerId))))

    fun isComplete(category: Category): Boolean = seats.all { value(it.playerId, category) != null }

    fun isComplete(playerId: Long): Boolean = categories.all { value(playerId, it) != null }

    val enteredCount: Int get() = seats.sumOf { s -> categories.count { value(s.playerId, it) != null } }
    val cellCount: Int get() = seats.size * categories.size

    fun standings(): List<Standing> = Ranking.rank(this)
}

data class Standing(
    val seat: Seat,
    val total: Int,
    val coins: Int?,
    /** 1-based; equal for players who are still tied after the coin tie-breaker. */
    val rank: Int,
    /** True if this player was tied on points and coins separated them. */
    val decidedByCoins: Boolean,
    /** True if tied on points and coins are unknown for at least one of the tied players. */
    val unresolvedTie: Boolean,
)

object Ranking {
    /** Most points wins; ties are broken by most coins (official rule), otherwise shared. */
    fun rank(game: GameState): List<Standing> {
        data class Row(val seat: Seat, val total: Int, val coins: Int?)

        val rows = game.seats.map { Row(it, game.total(it.playerId), game.sheet(it.playerId).coins) }
        val byTotal = rows.groupBy { it.total }

        return rows.map { row ->
            val tied = byTotal.getValue(row.total)
            val coinsKnown = tied.all { it.coins != null }
            val ahead = rows.count {
                it.total > row.total || (coinsKnown && it.total == row.total && it.coins!! > row.coins!!)
            }
            Standing(
                seat = row.seat,
                total = row.total,
                coins = row.coins,
                rank = ahead + 1,
                decidedByCoins = tied.size > 1 && coinsKnown && tied.any { it.coins != row.coins },
                unresolvedTie = tied.size > 1 && !coinsKnown,
            )
        }.sortedWith(compareBy<Standing> { it.rank }.thenBy { game.seats.indexOf(it.seat) })
    }
}
