package de.morhenn.seven_wonders_calculator.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScienceTest {
    @Test fun emptyIsZero() = assertEquals(0, ScienceInput().points)
    @Test fun singleSet() = assertEquals(10, ScienceInput(1, 1, 1).points)
    @Test fun squaresOnly() = assertEquals(9, ScienceInput(compass = 3).points)
    @Test fun mixed() = assertEquals(4 + 9 + 4 + 14, ScienceInput(2, 3, 2).points)
    @Test fun aristotleBonus() = assertEquals(13, ScienceInput(1, 1, 1, setBonus = 10).points)

    @Test fun wildcardCompletesSet() {
        val result = ScienceInput(2, 2, 1, wild = 1).result
        assertEquals(26, result.points)
        assertEquals(2, result.sets)
    }

    @Test fun wildcardPrefersSquareWhenBetter() {
        val result = ScienceInput(4, 0, 0, wild = 1).result
        assertEquals(25, result.points)
        assertEquals(5, result.compass)
    }

    @Test fun twoWildcards() {
        // 3,3,1 + 2 wild: (3,3,3)=27+21=48 vs (5,3,1)=35+7=42
        assertEquals(48, ScienceInput(3, 3, 1, wild = 2).points)
    }

    // (1,1,1) = 3 + 7 beats (3,0,0) = 9
    @Test fun onlyWildcards() = assertEquals(10, ScienceInput(wild = 3).points)
}

class HelperInputTest {
    @Test fun treasuryRoundsDown() = assertEquals(3, TreasuryInput(coins = 11).points)
    @Test fun treasuryDebt() = assertEquals(1, TreasuryInput(coins = 10, debt = 2).points)
    @Test fun treasuryCanBeNegative() = assertEquals(-4, TreasuryInput(coins = 2, debt = 4).points)
    @Test fun military() = assertEquals(14, MilitaryInput(defeats = 1, victoriesAge1 = 2, victoriesAge2 = 1, victoriesAge3 = 2).points)
}

class PlayerSheetTest {
    @Test fun manualEditDropsStaleHelper() {
        val sheet = PlayerSheet().withTreasury(TreasuryInput(coins = 9))
        assertEquals(9, sheet.coins)
        assertEquals(9, sheet.withValue(Category.TREASURY, 3).coins)
        assertNull(sheet.withValue(Category.TREASURY, 4).coins)
    }

    @Test fun totalOnlyCountsActiveCategories() {
        val sheet = PlayerSheet(mapOf(Category.WONDER to 5, Category.LEADERS to 7))
        assertEquals(5, sheet.total(Category.activeFor(emptySet())))
        assertEquals(12, sheet.total(Category.activeFor(setOf(Expansion.LEADERS))))
    }
}

class RankingTest {
    private fun game(vararg players: Pair<Int, Int?>): GameState {
        var state = GameState(players.mapIndexed { i, _ -> Seat(i.toLong(), "P$i") }, emptySet())
        players.forEachIndexed { i, (points, coins) ->
            state = state.updateSheet(i.toLong()) {
                val withCoins = if (coins != null) it.withTreasury(TreasuryInput(coins = coins)) else it
                withCoins.withValue(Category.WONDER, points - (withCoins.values[Category.TREASURY] ?: 0))
            }
        }
        return state
    }

    @Test fun ordersByTotal() {
        val standings = game(30 to null, 50 to null, 40 to null).standings()
        assertEquals(listOf(1L, 2L, 0L), standings.map { it.seat.playerId })
        assertEquals(listOf(1, 2, 3), standings.map { it.rank })
    }

    @Test fun tieBrokenByCoins() {
        val standings = game(50 to 3, 50 to 7).standings()
        assertEquals(1L, standings[0].seat.playerId)
        assertEquals(listOf(1, 2), standings.map { it.rank })
        assertTrue(standings.all { it.decidedByCoins })
    }

    @Test fun tieWithUnknownCoinsIsShared() {
        val standings = game(50 to 3, 50 to null, 20 to null).standings()
        assertEquals(listOf(1, 1, 3), standings.map { it.rank })
        assertTrue(standings[0].unresolvedTie)
        assertFalse(standings[2].unresolvedTie)
    }

    @Test fun tieWithEqualCoinsIsShared() {
        val standings = game(50 to 6, 50 to 6).standings()
        assertEquals(listOf(1, 1), standings.map { it.rank })
        assertFalse(standings[0].decidedByCoins)
    }
}

class StatsTest {
    @Test fun countsWinsAndAverages() {
        val seats = listOf(Seat(1, "Ann", "Gizah"), Seat(2, "Bob"))
        val g1 = GameState(seats, emptySet())
            .updateSheet(1) { it.withValue(Category.WONDER, 20) }
            .updateSheet(2) { it.withValue(Category.WONDER, 10) }
        val g2 = GameState(seats, emptySet())
            .updateSheet(1) { it.withValue(Category.WONDER, 10) }
            .updateSheet(2) { it.withValue(Category.WONDER, 30) }
        val stats = Stats.players(listOf(g1, g2)).associateBy { it.playerId }
        assertEquals(1, stats.getValue(1).wins)
        assertEquals(15.0, stats.getValue(1).averageScore, 0.001)
        assertEquals(30, stats.getValue(2).bestScore)
        assertEquals("Gizah", stats.getValue(1).favoriteWonder)
        assertEquals(Category.WONDER, stats.getValue(2).strongestCategory)
    }
}
