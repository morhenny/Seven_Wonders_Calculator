package de.morhenn.seven_wonders_calculator.domain

/** Wonder boards. Names are proper nouns and are not translated. */
object Wonders {
    private val base = listOf("Alexandria", "Babylon", "Ephesos", "Gizah", "Halikarnassos", "Olympia", "Rhodos")
    private val fromExpansions = listOf(
        "Roma" to Expansion.LEADERS,
        "Petra" to Expansion.CITIES,
        "Byzantium" to Expansion.CITIES,
        "Siracusa" to Expansion.ARMADA,
    )
    private val wonderPack = listOf("Abu Simbel", "Great Wall", "Manneken Pis", "Stonehenge")

    fun available(expansions: Set<Expansion>): List<String> =
        base + fromExpansions.filter { it.second in expansions }.map { it.first } + wonderPack
}
