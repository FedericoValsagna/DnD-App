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
    private val stunning = feature("MONK_STUNNING_STRIKE", resource = "KI")
    private val deflect = feature("MONK_DEFLECT_MISSILES", ActionType.REACTION)
    private val stillness = feature("MONK_STILLNESS_OF_MIND", ActionType.ACTION)
    private val evasion = feature("MONK_EVASION")
    private val actionSurge = feature("FIGHTER_ACTION_SURGE", resource = "ACTION_SURGE")
    private val secondWind = feature("FIGHTER_SECOND_WIND", ActionType.BONUS_ACTION, "SECOND_WIND")

    private val monkFighter = sampleCharacter().copy(
        features = mapOf(
            CharacterClass.MONK to listOf(ki, flurry, deflect, stunning, stillness, evasion),
            CharacterClass.FIGHTER to listOf(secondWind, actionSurge),
        ),
        resources = listOf(resource("KI", 7), resource("SECOND_WIND", 1), resource("ACTION_SURGE", 1)),
    )

    @Test
    fun `groups the actions by type in order, with every class`() {
        val list = monkFighter.combatActionList()

        assertEquals(listOf(ActionType.ACTION, ActionType.BONUS_ACTION, ActionType.REACTION), list.byType.keys.toList())
        assertEquals(listOf(stillness), list.byType[ActionType.ACTION])
        assertEquals(listOf(flurry, secondWind), list.byType[ActionType.BONUS_ACTION])
        assertEquals(listOf(deflect), list.byType[ActionType.REACTION])
    }

    @Test
    fun `others has the resources no action spends, and no passive features`() {
        assertEquals(listOf(actionSurge), monkFighter.combatActionList().others)
    }

    @Test
    fun `a resource the character doesn't track is not in others`() {
        val character = monkFighter.copy(resources = monkFighter.resources.filter { it.id != "ACTION_SURGE" })

        assertEquals(emptyList<ClassFeature>(), character.combatActionList().others)
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

    @Test
    fun `finds the resource of a feature, if it is tracked`() {
        assertEquals(7, monkFighter.resourceOf(flurry)?.max)
        assertNull(monkFighter.resourceOf(stillness))
        assertNull(monkFighter.copy(resources = emptyList()).resourceOf(flurry))
    }
}
