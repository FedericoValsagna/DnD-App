package com.valsagnapps.dndapp.domain

/**
 * CONSCIOUS: HP above 0. At 0 HP the death saves tell: DEAD with three failures, STABLE with three
 * successes (doesn't roll until healed or hit) and DYING otherwise. The server works it out.
 */
enum class LifeStatus { CONSCIOUS, DYING, STABLE, DEAD }

/** Marked by the player: the app doesn't roll dice. Three of each at most, never both at three. */
data class DeathSaves(val successes: Int = 0, val failures: Int = 0) {
    companion object {
        const val MAX = 3
    }
}

/** What the HP sheet does with the amount. */
enum class HitPointsAction { DAMAGE, HEALING, TEMPORARY }

/** What a change would leave, shown before sending it. The server applies the rules: this only previews them. */
sealed interface HitPointsPreview {
    data class Points(val current: Int, val temporary: Int) : HitPointsPreview

    /** Damage at 0 HP: death save failures it adds. */
    data class Failures(val count: Int) : HitPointsPreview

    data object Dead : HitPointsPreview
}

/**
 * The preview of [action] with [amount], or null if the server didn't send the HP. Temporary HP
 * absorb damage first; damage left over after 0 that reaches the max kills; at 0, damage is a
 * failure (two if [critical]); healing stops at the max; new temporary HP replace the old ones.
 */
fun Character.previewHitPoints(action: HitPointsAction, amount: Int, critical: Boolean = false): HitPointsPreview? {
    val current = currentHitPoints ?: return null
    val max = maxHitPoints ?: return null
    return when (action) {
        HitPointsAction.DAMAGE -> previewDamage(current, max, amount, critical)
        HitPointsAction.HEALING -> HitPointsPreview.Points(minOf(max, current + amount), temporaryHitPoints)
        HitPointsAction.TEMPORARY -> HitPointsPreview.Points(current, amount)
    }
}

private fun Character.previewDamage(current: Int, max: Int, amount: Int, critical: Boolean): HitPointsPreview {
    val absorbed = minOf(temporaryHitPoints, amount)
    val remaining = amount - absorbed
    val failures = if (critical) 2 else 1
    return when {
        remaining == 0 -> HitPointsPreview.Points(current, temporaryHitPoints - absorbed)
        remaining - current >= max -> HitPointsPreview.Dead
        current > 0 -> HitPointsPreview.Points(maxOf(0, current - remaining), temporaryHitPoints - absorbed)
        deathSaves.failures + failures >= DeathSaves.MAX -> HitPointsPreview.Dead
        else -> HitPointsPreview.Failures(failures)
    }
}
