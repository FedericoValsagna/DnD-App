package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.DeathSaves

/** Combat actions: the server applies the rules and returns the character as it is left. */
interface CombatRepository {
    /** [critical] counts two death save failures at 0 HP. */
    suspend fun takeDamage(id: String, amount: Int, critical: Boolean): RepositoryResult<Character>

    suspend fun heal(id: String, amount: Int): RepositoryResult<Character>

    /** Replaces the temporary HP there were; 0 removes them. */
    suspend fun setTemporaryHitPoints(id: String, temporaryHitPoints: Int): RepositoryResult<Character>

    /** The whole counters, as they are after marking or unmarking one; only at 0 HP. */
    suspend fun updateDeathSaves(id: String, deathSaves: DeathSaves): RepositoryResult<Character>

    /** Only a dead character; comes back with [hitPoints]. */
    suspend fun revive(id: String, hitPoints: Int): RepositoryResult<Character>

    /** Null goes back to the calculated AC. */
    suspend fun updateArmorClassOverride(id: String, armorClassOverride: Int?): RepositoryResult<Character>

    /** The speed without the class bonuses, which the server adds. */
    suspend fun updateBaseSpeed(id: String, baseSpeed: Int): RepositoryResult<Character>
}
