package com.valsagnapps.dndapp.domain

import com.valsagnapps.dndapp.data.sampleCharacter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CombatActionListTest {

    private fun feature(id: String, actionType: ActionType? = null, resource: String? = null) =
        ClassFeature(id, id, level = 1, summary = "", actionType = actionType, resource = resource)

    private fun resource(id: String, max: Int = 2) =
        Resource(id, id, max, used = 0, recharge = Recharge.SHORT_REST, display = UsesDisplay.PIPS)

    private val ki = feature("MONK_KI", resource = "KI")
    private val flurry = feature("MONK_FLURRY_OF_BLOWS", ActionType.BONUS_ACTION, "KI")
    private val patientDefense = feature("MONK_PATIENT_DEFENSE", ActionType.BONUS_ACTION, "KI")
    private val stunning = feature("MONK_STUNNING_STRIKE", resource = "KI")
    private val emptyBody = feature("MONK_EMPTY_BODY", ActionType.ACTION, "KI")
    private val deflect = feature("MONK_DEFLECT_MISSILES", ActionType.REACTION)
    private val stillness = feature("MONK_STILLNESS_OF_MIND", ActionType.ACTION)
    private val evasion = feature("MONK_EVASION")
    private val actionSurge = feature("FIGHTER_ACTION_SURGE", resource = "ACTION_SURGE")
    private val secondWind = feature("FIGHTER_SECOND_WIND", ActionType.BONUS_ACTION, "SECOND_WIND")

    private val kiPool = resource("KI", 7)
    private val monkFighter = sampleCharacter().copy(
        features = mapOf(
            CharacterClass.MONK to listOf(ki, flurry, deflect, patientDefense, stunning, stillness, evasion, emptyBody),
            CharacterClass.FIGHTER to listOf(secondWind, actionSurge),
        ),
        resources = listOf(kiPool, resource("SECOND_WIND", 1), resource("ACTION_SURGE", 1)),
    )
    private val kiRow = CombatAction("KI", listOf(ki, flurry, patientDefense, stunning, emptyBody), kiPool)

    @Test
    fun `features that share a resource go in one row titled with it`() {
        val list = monkFighter.combatActionList()

        assertEquals(listOf(stillness.single()), list.byType[ActionType.ACTION])
        assertEquals(listOf(kiRow, secondWind.single(resource("SECOND_WIND", 1))), list.byType[ActionType.BONUS_ACTION])
        assertEquals(listOf(deflect.single()), list.byType[ActionType.REACTION])
    }

    @Test
    fun `a shared resource goes with the type most of its options have`() {
        assertEquals(ActionType.BONUS_ACTION, kiRow.actionType)
        assertTrue(kiRow.isGroup)
    }

    @Test
    fun `a tie goes to the first type`() {
        val row = CombatAction("CD", listOf(emptyBody, flurry), null)

        assertEquals(ActionType.ACTION, row.actionType)
    }

    @Test
    fun `others has the resources no action spends, and no passive features`() {
        assertEquals(listOf(actionSurge.single(resource("ACTION_SURGE", 1))), monkFighter.combatActionList().others)
    }

    @Test
    fun `without a tracked resource the features are rows of their own`() {
        val character = monkFighter.copy(resources = emptyList())
        val list = character.combatActionList()

        assertEquals(listOf(stillness.single(), emptyBody.single()), list.byType[ActionType.ACTION])
        assertEquals(emptyList<CombatAction>(), list.others)
        assertNull(list.byType.getValue(ActionType.BONUS_ACTION).first().resource)
    }

    @Test
    fun `types without actions are left out`() {
        val character = sampleCharacter().copy(features = mapOf(CharacterClass.MONK to listOf(deflect, evasion)))

        assertEquals(listOf(ActionType.REACTION), character.combatActionList().byType.keys.toList())
    }

    @Test
    fun `a character without features has no actions`() {
        assertTrue(sampleCharacter().copy(features = emptyMap()).combatActionList().isEmpty)
    }

    private fun ClassFeature.single(resource: Resource? = null) = CombatAction(name, listOf(this), resource)
}
