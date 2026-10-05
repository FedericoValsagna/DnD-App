package com.valsagnapps.dndapp.domain

/** Same validation limits the server enforces on character creation. */
object CharacterRules {
    const val NAME_MAX_LENGTH = 100
    val LEVEL_RANGE = 1..20
    val ABILITY_SCORE_RANGE = 1..30
    val MAX_HIT_POINTS_RANGE = 1..999

    fun isValidName(name: String): Boolean = name.isNotBlank() && name.length <= NAME_MAX_LENGTH

    fun isValidLevel(level: Int): Boolean = level in LEVEL_RANGE

    fun isValidMaxHitPoints(maxHitPoints: Int): Boolean = maxHitPoints in MAX_HIT_POINTS_RANGE

    fun isValidAbilityScore(score: Int): Boolean = score in ABILITY_SCORE_RANGE
}
