package com.valsagnapps.dndapp.data.remote

import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.AbilityScore
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.NewCharacter
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterMappersTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `maps a character response from the server to the domain`() {
        val body = """
            {"id":"7f84dde5-bb79-44d0-85d7-6a8034c2d131","name":"Tordek","level":5,"proficiencyBonus":3,
             "abilities":{"STRENGTH":{"score":16,"modifier":3},"DEXTERITY":{"score":12,"modifier":1},
             "CONSTITUTION":{"score":15,"modifier":2},"INTELLIGENCE":{"score":10,"modifier":0},
             "WISDOM":{"score":13,"modifier":1},"CHARISMA":{"score":8,"modifier":-1}}}
        """.trimIndent()

        val character = json.decodeFromString<CharacterDto>(body).toDomain()

        assertEquals(
            Character(
                id = "7f84dde5-bb79-44d0-85d7-6a8034c2d131",
                name = "Tordek",
                level = 5,
                proficiencyBonus = 3,
                abilities = mapOf(
                    Ability.STRENGTH to AbilityScore(16, 3),
                    Ability.DEXTERITY to AbilityScore(12, 1),
                    Ability.CONSTITUTION to AbilityScore(15, 2),
                    Ability.INTELLIGENCE to AbilityScore(10, 0),
                    Ability.WISDOM to AbilityScore(13, 1),
                    Ability.CHARISMA to AbilityScore(8, -1),
                ),
            ),
            character,
        )
    }

    @Test
    fun `ignores abilities the app does not know`() {
        val dto = CharacterDto(
            id = "1",
            name = "Tordek",
            level = 1,
            proficiencyBonus = 2,
            abilities = mapOf("STRENGTH" to AbilityDto(10, 0), "LUCK" to AbilityDto(18, 4)),
        )

        assertEquals(mapOf(Ability.STRENGTH to AbilityScore(10, 0)), dto.toDomain().abilities)
    }

    @Test
    fun `serializes a new character with the keys the server expects`() {
        val newCharacter = NewCharacter(
            name = "Tordek",
            level = 5,
            abilityScores = mapOf(
                Ability.STRENGTH to 16,
                Ability.DEXTERITY to 12,
                Ability.CONSTITUTION to 15,
                Ability.INTELLIGENCE to 10,
                Ability.WISDOM to 13,
                Ability.CHARISMA to 8,
            ),
        )

        val encoded = json.encodeToString(newCharacter.toRequest())

        val expected = """
            {"name":"Tordek","level":5,
             "abilityScores":{"strength":16,"dexterity":12,"constitution":15,
                              "intelligence":10,"wisdom":13,"charisma":8}}
        """.trimIndent()
        assertEquals(json.parseToJsonElement(expected), json.parseToJsonElement(encoded))
    }
}
