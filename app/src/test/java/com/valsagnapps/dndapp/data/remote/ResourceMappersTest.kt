package com.valsagnapps.dndapp.data.remote

import com.valsagnapps.dndapp.domain.ActionType
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.Recharge
import com.valsagnapps.dndapp.domain.Resource
import com.valsagnapps.dndapp.domain.UsesDisplay
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResourceMappersTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun character(fields: String) = json.decodeFromString<CharacterDto>(
        """{"id":"1","name":"Ember","level":5,"proficiencyBonus":3,"abilities":{}$fields}""",
    ).toDomain()

    @Test
    fun `maps the resources and the action type and resource of each feature`() {
        val character = character(
            """
                ,"classes":[{"class":"MONK","level":5,"features":[
                  {"id":"MONK_FLURRY_OF_BLOWS","name":"Flurry of Blows","level":2,"summary":"...",
                   "actionType":"BONUS_ACTION","resource":"KI"},
                  {"id":"MONK_EVASION","name":"Evasion","level":7,"summary":"...","actionType":null,"resource":null}]}],
                "resources":[{"id":"KI","name":"Ki","max":5,"used":2,"recharge":"SHORT_REST","display":"POOL"}]
            """,
        )

        val (flurry, evasion) = character.features.getValue(CharacterClass.MONK)
        assertEquals(ActionType.BONUS_ACTION, flurry.actionType)
        assertEquals("KI", flurry.resource)
        assertNull(evasion.actionType)
        assertNull(evasion.resource)
        assertEquals(listOf(Resource("KI", "Ki", 5, 2, Recharge.SHORT_REST, UsesDisplay.POOL)), character.resources)
        assertEquals(3, character.resources.single().remaining)
    }

    @Test
    fun `values the app doesn't know are left out, and an unknown display shows points`() {
        val character = character(
            """
                ,"classes":[{"class":"MONK","level":5,"features":[
                  {"id":"X","name":"X","level":1,"summary":"...","actionType":"LEGENDARY_ACTION"}]}],
                "resources":[{"id":"X","name":"X","max":1,"used":0,"recharge":"DAWN","display":"BAR"}]
            """,
        )

        assertNull(character.features.getValue(CharacterClass.MONK).single().actionType)
        assertNull(character.resources.single().recharge)
        assertEquals(UsesDisplay.PIPS, character.resources.single().display)
    }

    @Test
    fun `an older server without resources has none`() {
        assertEquals(emptyList<Resource>(), character("").resources)
    }

    @Test
    fun `serializes a resource update with the key the server expects`() {
        assertEquals("""{"used":3}""", json.encodeToString(UpdateResourceRequest(3)))
    }
}
