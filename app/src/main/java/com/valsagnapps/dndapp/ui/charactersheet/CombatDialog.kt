package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.domain.CharacterRules
import com.valsagnapps.dndapp.domain.CombatAction
import com.valsagnapps.dndapp.domain.HitDice
import com.valsagnapps.dndapp.domain.HitPointsAction
import com.valsagnapps.dndapp.domain.Resource
import com.valsagnapps.dndapp.domain.UsesDisplay
import com.valsagnapps.dndapp.ui.common.toNumericInput

/** What a combat dialog is filling in. Numbers are kept as text, as in [SheetEdit]. */
sealed interface CombatDialog {
    val isValid: Boolean

    /** The HP sheet (opened by tapping the HP): an amount from the keypad and what to do with it. */
    data class HitPoints(
        val action: HitPointsAction = HitPointsAction.DAMAGE,
        val amount: String = "",
        /** Only matters for damage at 0 HP. */
        val critical: Boolean = false,
    ) : CombatDialog {
        val amountValue: Int? get() = amount.toIntOrNull()?.takeIf { it in action.range }
        override val isValid: Boolean get() = amountValue != null
    }

    /** How many HP a dead character comes back with: 1 to the max, 1 by default. */
    data class Revive(val maxHitPoints: Int, val hitPoints: String = "1") : CombatDialog {
        val range: IntRange get() = 1..maxHitPoints
        override val isValid: Boolean get() = hitPoints.toIntOrNull()?.let { it in range } == true
    }

    /**
     * An action's sheet (opened by tapping its row in Combate): its text, or its options if several features share
     * the resource, and the uses as they were when it opened. Points are spent or recovered right away; a pool
     * is spent with the keypad: [amount] to spend or recover ([recovering]), only what's left or what's spent.
     */
    data class Action(val action: CombatAction, val recovering: Boolean = false, val amount: String = "") :
        CombatDialog {
        val resource: Resource? get() = action.resource
        val isPool: Boolean get() = resource?.display == UsesDisplay.POOL
        val amountValue: Int?
            get() {
                val resource = resource ?: return null
                return amount.toIntOrNull()?.takeIf { it in 1..limit(resource) }
            }

        /** The used uses after applying [amountValue], or null if there's nothing valid to apply. */
        val newUsed: Int?
            get() {
                val used = resource?.used ?: return null
                return amountValue?.let { if (recovering) used - it else used + it }
            }
        override val isValid: Boolean get() = isPool && amountValue != null

        private fun limit(resource: Resource) = if (recovering) resource.used else resource.remaining
    }

    /**
     * The short rest sheet: how many [HitDice] of each die to spend ([spent], die to how many) and the [roll], the
     * total of the dice without the CON. Without dice it only recovers the short rest resources. At 0 HP
     * ([canSpendDice] false) no dice can be spent: the rule asks for 1 HP.
     */
    data class ShortRest(
        val hitDice: List<HitDice>,
        val currentHitPoints: Int,
        val maxHitPoints: Int,
        val constitutionModifier: Int,
        val canSpendDice: Boolean,
        val spent: Map<Int, Int> = emptyMap(),
        val roll: String = "",
    ) : CombatDialog {
        val diceCount: Int get() = spent.values.sum()

        /** From one per die to the sum of their faces. */
        val rollRange: IntRange get() = diceCount..spent.entries.sumOf { (die, count) -> die * count }

        /** 0 without dice; null if the roll isn't possible with the dice spent. */
        val rollValue: Int? get() = if (diceCount == 0) 0 else roll.toIntOrNull()?.takeIf { it in rollRange }

        /** The HP after resting, as the server computes them: the CON per die, at least 0, up to the max. */
        val hitPointsAfter: Int
            get() {
                val healing = rollValue?.let { maxOf(0, it + diceCount * constitutionModifier) } ?: 0
                return minOf(maxHitPoints, currentHitPoints + healing)
            }
        override val isValid: Boolean get() = rollValue != null

        fun spentOf(die: Int): Int = spent[die] ?: 0
    }
}

/** Damage and healing start at 1; temporary HP at 0, which removes them. */
val HitPointsAction.range: IntRange
    get() = when (this) {
        HitPointsAction.DAMAGE, HitPointsAction.HEALING -> CharacterRules.HIT_POINTS_CHANGE_RANGE
        HitPointsAction.TEMPORARY -> CharacterRules.TEMPORARY_HIT_POINTS_RANGE
    }

/** A change the user makes in a combat dialog. */
sealed interface CombatInput {
    data class Action(val action: HitPointsAction) : CombatInput

    /** A keypad key, 0 to 9. */
    data class Digit(val digit: Int) : CombatInput

    data object Backspace : CombatInput

    data object Clear : CombatInput

    data class Critical(val critical: Boolean) : CombatInput

    data class ReviveHitPoints(val value: String) : CombatInput

    /** In a pool: spend (false) or recover (true). */
    data class Recovering(val recovering: Boolean) : CombatInput

    /** In the short rest: how many of the hit dice of [die] to spend. */
    data class SpentHitDice(val die: Int, val count: Int) : CombatInput
}

/** The dialog with [input] applied. An input from the other dialog leaves it as it is. */
fun CombatDialog.changedBy(input: CombatInput): CombatDialog = when (this) {
    is CombatDialog.HitPoints -> when (input) {
        // Another action keeps the amount, as long as it fits (temporary HP have fewer digits).
        is CombatInput.Action -> copy(action = input.action, amount = amount.toNumericInput(input.action.range))
        is CombatInput.Digit -> copy(amount = amount.typed(input.digit, action.range))
        CombatInput.Backspace -> copy(amount = amount.dropLast(1))
        CombatInput.Clear -> copy(amount = "")
        is CombatInput.Critical -> copy(critical = input.critical)
        is CombatInput.ReviveHitPoints, is CombatInput.Recovering, is CombatInput.SpentHitDice -> this
    }
    is CombatDialog.Revive -> when (input) {
        is CombatInput.ReviveHitPoints -> copy(hitPoints = input.value.toNumericInput(range))
        else -> this
    }
    is CombatDialog.Action -> changedBy(input)
    is CombatDialog.ShortRest -> changedBy(input)
}

/** Dice from 0 to the ones left, and none at 0 HP. The roll has at most the digits of every die left. */
private fun CombatDialog.ShortRest.changedBy(input: CombatInput): CombatDialog.ShortRest {
    val maxRoll = hitDice.sumOf { it.die * it.remaining }
    return when (input) {
        is CombatInput.SpentHitDice -> {
            val left = hitDice.find { it.die == input.die }?.remaining ?: 0
            if (!canSpendDice || input.count !in 0..left) this else copy(spent = spent + (input.die to input.count))
        }
        is CombatInput.Digit -> copy(roll = roll.typed(input.digit, 0..maxRoll))
        CombatInput.Backspace -> copy(roll = roll.dropLast(1))
        CombatInput.Clear -> copy(roll = "")
        else -> this
    }
}

private fun CombatDialog.Action.changedBy(input: CombatInput): CombatDialog.Action {
    val max = resource?.max ?: return this
    return when (input) {
        is CombatInput.Digit -> copy(amount = amount.typed(input.digit, 0..max))
        CombatInput.Backspace -> copy(amount = amount.dropLast(1))
        CombatInput.Clear -> copy(amount = "")
        is CombatInput.Recovering -> copy(recovering = input.recovering)
        else -> this
    }
}

/** [this] with [digit] typed at the end, without leading zeros and with at most the digits of [range]. */
private fun String.typed(digit: Int, range: IntRange): String =
    (this + digit).trimStart('0').ifEmpty { "0" }.toNumericInput(range)
