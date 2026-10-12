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

    /** How many uses of [resource] are spent, after spending or recovering (0 recovers them all). */
    suspend fun updateResourceUsed(id: String, resource: String, used: Int): RepositoryResult<Character>

    /**
     * Spends [hitDice] (die to how many) and heals [roll], the total of the dice without the CON, which the server
     * adds. Recovers the short rest resources. Without dice, [roll] is 0.
     */
    suspend fun shortRest(id: String, hitDice: Map<Int, Int>, roll: Int): RepositoryResult<Character>

    /** HP to the max, every resource and half the hit dice. */
    suspend fun longRest(id: String): RepositoryResult<Character>
}
