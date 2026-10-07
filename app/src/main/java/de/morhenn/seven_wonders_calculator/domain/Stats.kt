package de.morhenn.seven_wonders_calculator.domain

data class PlayerStats(
    val playerId: Long,
    val name: String,
    val games: Int,
    val wins: Int,
    val averageScore: Double,
    val bestScore: Int,
    /** Average points per category over the games in which that category was played. */
    val categoryAverages: Map<Category, Double>,
    val favoriteWonder: String?,
) {
    val winRate: Double get() = if (games == 0) 0.0 else wins.toDouble() / games
    val strongestCategory: Category? get() = categoryAverages.maxByOrNull { it.value }?.key
}

data class WonderStats(val wonder: String, val games: Int, val wins: Int, val averageScore: Double)

object Stats {
    private class Entry(val seat: Seat, val game: GameState, val standing: Standing)

    private fun entries(games: List<GameState>): List<Entry> = games.flatMap { game ->
        game.standings().map { Entry(it.seat, game, it) }
    }

    /** [games] must be ordered oldest first so the latest name of a player wins. */
    fun players(games: List<GameState>): List<PlayerStats> =
        entries(games).groupBy { it.seat.playerId }.map { (playerId, entries) ->
            val totals = entries.map { it.standing.total }
            PlayerStats(
                playerId = playerId,
                name = entries.last().seat.name,
                games = entries.size,
                wins = entries.count { it.standing.rank == 1 },
                averageScore = totals.average(),
                bestScore = totals.max(),
                categoryAverages = Category.entries.mapNotNull { category ->
                    val values = entries.filter { category in it.game.categories }
                        .map { it.game.value(playerId, category) ?: 0 }
                    if (values.isEmpty()) null else category to values.average()
                }.toMap(),
                favoriteWonder = entries.mapNotNull { it.seat.wonder }
                    .groupingBy { it }.eachCount().maxByOrNull { it.value }?.key,
            )
        }.sortedWith(compareByDescending<PlayerStats> { it.wins }.thenByDescending { it.averageScore })

    fun wonders(games: List<GameState>): List<WonderStats> =
        entries(games).filter { it.seat.wonder != null }.groupBy { it.seat.wonder!! }.map { (wonder, entries) ->
            WonderStats(
                wonder = wonder,
                games = entries.size,
                wins = entries.count { it.standing.rank == 1 },
                averageScore = entries.map { it.standing.total }.average(),
            )
        }.sortedByDescending { it.averageScore }
}
