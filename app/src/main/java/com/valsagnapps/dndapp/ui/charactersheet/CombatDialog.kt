package com.valsagnapps.dndapp.ui.charactersheet

import com.valsagnapps.dndapp.domain.CharacterRules
import com.valsagnapps.dndapp.domain.HitPointsAction
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
}

/** The dialog with [input] applied. An input from the other dialog leaves it as it is. */
fun CombatDialog.changedBy(input: CombatInput): CombatDialog = when (this) {
    is CombatDialog.HitPoints -> when (input) {
        // Another action keeps the amount, as long as it fits (temporary HP have fewer digits).
        is CombatInput.Action -> copy(action = input.action, amount = amount.toNumericInput(input.action.range))
        is CombatInput.Digit -> copy(
            amount = (amount + input.digit).trimStart('0').ifEmpty {
                "0"
            }.toNumericInput(action.range),
        )
        CombatInput.Backspace -> copy(amount = amount.dropLast(1))
        CombatInput.Clear -> copy(amount = "")
        is CombatInput.Critical -> copy(critical = input.critical)
        is CombatInput.ReviveHitPoints -> this
    }
    is CombatDialog.Revive -> when (input) {
        is CombatInput.ReviveHitPoints -> copy(hitPoints = input.value.toNumericInput(range))
        else -> this
    }
}
