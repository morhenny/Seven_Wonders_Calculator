package de.morhenn.seven_wonders_calculator.domain

import kotlinx.serialization.Serializable

enum class ConflictToken(val points: Int) { DEFEAT(-1), AGE_1(1), AGE_2(3), AGE_3(5) }

/** Conflict tokens: defeats (-1) and victories of age I/II/III (+1/+3/+5). */
@Serializable
data class MilitaryInput(
    val defeats: Int = 0,
    val victoriesAge1: Int = 0,
    val victoriesAge2: Int = 0,
    val victoriesAge3: Int = 0,
) {
    val points: Int get() = -defeats + victoriesAge1 + 3 * victoriesAge2 + 5 * victoriesAge3

    fun count(token: ConflictToken): Int = when (token) {
        ConflictToken.DEFEAT -> defeats
        ConflictToken.AGE_1 -> victoriesAge1
        ConflictToken.AGE_2 -> victoriesAge2
        ConflictToken.AGE_3 -> victoriesAge3
    }

    /** Adds [delta] tokens of one kind; counts never drop below zero. */
    fun plus(token: ConflictToken, delta: Int): MilitaryInput {
        val n = (count(token) + delta).coerceIn(0, 99)
        return when (token) {
            ConflictToken.DEFEAT -> copy(defeats = n)
            ConflictToken.AGE_1 -> copy(victoriesAge1 = n)
            ConflictToken.AGE_2 -> copy(victoriesAge2 = n)
            ConflictToken.AGE_3 -> copy(victoriesAge3 = n)
        }
    }
}

/** Coins are worth 1 point per full 3; debt tokens (Cities) are -1 point each. */
@Serializable
data class TreasuryInput(val coins: Int = 0, val debt: Int = 0) {
    val points: Int get() = coins / 3 - debt
}

/**
 * Science symbols. [wild] counts symbols that may become any symbol (Scientists Guild, Babylon,
 * and expansion effects). [setBonus] is 7 by default, 10 with Aristotle (Leaders).
 */
@Serializable
data class ScienceInput(
    val compass: Int = 0,
    val gear: Int = 0,
    val tablet: Int = 0,
    val wild: Int = 0,
    val setBonus: Int = 7,
) {
    val result: ScienceResult get() = Science.best(compass, gear, tablet, wild, setBonus)
    val points: Int get() = result.points
}

data class ScienceResult(val compass: Int, val gear: Int, val tablet: Int, val sets: Int, val points: Int)

object Science {
    fun score(compass: Int, gear: Int, tablet: Int, setBonus: Int = 7): Int {
        val sets = minOf(compass, gear, tablet)
        return compass * compass + gear * gear + tablet * tablet + sets * setBonus
    }

    /** Tries every distribution of wildcard symbols and returns the best one. */
    fun best(compass: Int, gear: Int, tablet: Int, wild: Int, setBonus: Int = 7): ScienceResult {
        var best: ScienceResult? = null
        for (a in 0..wild) {
            for (b in 0..wild - a) {
                val c = compass + a
                val g = gear + b
                val t = tablet + (wild - a - b)
                val points = score(c, g, t, setBonus)
                if (best == null || points > best.points) best = ScienceResult(c, g, t, minOf(c, g, t), points)
            }
        }
        return best!!
    }
}
