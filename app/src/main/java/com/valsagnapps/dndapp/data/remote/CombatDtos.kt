package com.valsagnapps.dndapp.data.remote

import kotlinx.serialization.Serializable

/** [critical] only matters at 0 HP, where it counts two death save failures. */
@Serializable
data class DamageRequest(val amount: Int, val critical: Boolean)

@Serializable
data class HealingRequest(val amount: Int)

/** Replaces the temporary HP there were; 0 removes them. */
@Serializable
data class UpdateTemporaryHitPointsRequest(val temporaryHitPoints: Int)

@Serializable
data class ReviveRequest(val hitPoints: Int)

/** Null goes back to the calculated AC. */
@Serializable
data class UpdateArmorClassRequest(val armorClassOverride: Int?)

@Serializable
data class UpdateSpeedRequest(val baseSpeed: Int)

/** How many uses are spent after spending or recovering; 0 recovers them all. */
@Serializable
data class UpdateResourceRequest(val used: Int)
