package com.valsagnapps.dndapp.domain

/**
 * A class or subclass feature, gained at [level] of its class. The sheet shows [summary]; [srdText] is the full SRD
 * text, null if the feature isn't SRD. [id] is stable.
 */
data class ClassFeature(
    val id: String,
    val name: String,
    val level: Int,
    val summary: String,
    val srdText: String? = null,
    /** What using it costs in the turn; null if it's passive (or the server is old or sent a new type). */
    val actionType: ActionType? = null,
    /**
     * Id of the [Resource] it spends or gives, or null. It can be missing from the character's resources: then
     * it has no limit (Rage at 20th level).
     */
    val resource: String? = null,
)

/** In this order in Combate. */
enum class ActionType { ACTION, BONUS_ACTION, REACTION }
