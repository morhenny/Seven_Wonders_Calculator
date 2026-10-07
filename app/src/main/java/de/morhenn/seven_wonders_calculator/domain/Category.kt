package de.morhenn.seven_wonders_calculator.domain

import kotlinx.serialization.Serializable

@Serializable
enum class Expansion {
    LEADERS, CITIES, ARMADA, EDIFICE
}

/**
 * Scoring categories in the order of the official score pad. Expansion categories are inserted
 * where they are usually scored.
 */
@Serializable
enum class Category(val expansion: Expansion?) {
    MILITARY(null),
    NAVAL(Expansion.ARMADA),
    TREASURY(null),
    WONDER(null),
    CIVILIAN(null),
    COMMERCIAL(null),
    GUILDS(null),
    SCIENCE(null),
    LEADERS(Expansion.LEADERS),
    CITIES(Expansion.CITIES),
    EDIFICE(Expansion.EDIFICE);

    companion object {
        fun activeFor(expansions: Set<Expansion>): List<Category> =
            entries.filter { it.expansion == null || it.expansion in expansions }
    }
}
