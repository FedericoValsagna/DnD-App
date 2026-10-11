package com.valsagnapps.dndapp.data.remote

import com.valsagnapps.dndapp.domain.DeathSaves
import com.valsagnapps.dndapp.domain.LifeStatus
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CombatMappersTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun character(combatFields: String) = json.decodeFromString<CharacterDto>(
        """
            {"id":"1","name":"Aldric","level":5,"proficiencyBonus":3,"abilities":{},"maxHitPoints":44
             $combatFields}
        """.trimIndent(),
    ).toDomain()

    @Test
    fun `maps the combat state from the server`() {
        val character = character(
            """
                ,"currentHitPoints":0,"temporaryHitPoints":5,"lifeStatus":"DYING",
                "deathSaves":{"successes":1,"failures":2},"armorClass":18,"armorClassOverride":18,
                "initiative":-1,"speed":40,"baseSpeed":30
            """,
        )

        assertEquals(0, character.currentHitPoints)
        assertEquals(5, character.temporaryHitPoints)
        assertEquals(LifeStatus.DYING, character.lifeStatus)
        assertEquals(DeathSaves(successes = 1, failures = 2), character.deathSaves)
        assertEquals(18, character.armorClass)
        assertEquals(18, character.armorClassOverride)
        assertEquals(-1, character.initiative)
        assertEquals(40, character.speed)
        assertEquals(30, character.baseSpeed)
    }

    @Test
    fun `a server without combat leaves the combat state empty`() {
        val character = character("")

        assertNull(character.currentHitPoints)
        assertEquals(0, character.temporaryHitPoints)
        assertEquals(LifeStatus.CONSCIOUS, character.lifeStatus)
        assertEquals(DeathSaves(), character.deathSaves)
        assertNull(character.armorClass)
        assertNull(character.initiative)
        assertNull(character.speed)
    }

    @Test
    fun `an unknown life status counts as conscious`() {
        assertEquals(LifeStatus.CONSCIOUS, character(""","lifeStatus":"PETRIFIED"""").lifeStatus)
    }
}
