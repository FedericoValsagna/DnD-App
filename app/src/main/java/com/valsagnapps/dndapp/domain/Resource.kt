package com.valsagnapps.dndapp.domain

/**
 * A resource with uses (Rage, Ki, Channel Divinity, ...). The server works out [max] and [recharge]; the player
 * marks the [used] ones. [id] is the server's (the app doesn't list them): features point to it.
 */
data class Resource(
    val id: String,
    val name: String,
    val max: Int,
    val used: Int,
    /** Null if the server sent one the app doesn't know. */
    val recharge: Recharge?,
    val display: UsesDisplay,
) {
    val remaining: Int get() = max - used
}

enum class Recharge { SHORT_REST, LONG_REST }

/** Points to tap (few uses) or a number spent with the keypad (Ki, Lay on Hands). The server decides. */
enum class UsesDisplay { PIPS, POOL }
