package com.valsagnapps.dndapp.data.remote

import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.AbilityScore
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.NewCharacter
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.domain.SkillValue
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
             "WISDOM":{"score":13,"modifier":1},"CHARISMA":{"score":8,"modifier":-1}},
             "skills":{"ATHLETICS":{"ability":"STRENGTH","proficiency":"PROFICIENT","bonus":6},
                       "PERCEPTION":{"ability":"WISDOM","proficiency":"EXPERTISE","bonus":7},
                       "STEALTH":{"ability":"DEXTERITY","proficiency":"NONE","bonus":1}},
             "passivePerception":17}
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
                skills = mapOf(
                    Skill.ATHLETICS to SkillValue(Proficiency.PROFICIENT, 6),
                    Skill.PERCEPTION to SkillValue(Proficiency.EXPERTISE, 7),
                    Skill.STEALTH to SkillValue(Proficiency.NONE, 1),
                ),
                passivePerception = 17,
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
    fun `ignores skills or proficiencies the app does not know`() {
        val dto = CharacterDto(
            id = "1",
            name = "Tordek",
            level = 1,
            proficiencyBonus = 2,
            abilities = emptyMap(),
            skills = mapOf(
                "ARCANA" to SkillDto("INTELLIGENCE", "PROFICIENT", 2),
                "COOKING" to SkillDto("WISDOM", "PROFICIENT", 2),
                "HISTORY" to SkillDto("INTELLIGENCE", "MASTERY", 4),
            ),
        )

        assertEquals(mapOf(Skill.ARCANA to SkillValue(Proficiency.PROFICIENT, 2)), dto.toDomain().skills)
    }

    @Test
    fun `reads a character without skills from an older server`() {
        val body = """{"id":"1","name":"Tordek","level":1,"proficiencyBonus":2,"abilities":{}}"""

        val character = json.decodeFromString<CharacterDto>(body).toDomain()

        assertEquals(emptyMap<Skill, SkillValue>(), character.skills)
        assertEquals(null, character.passivePerception)
        assertEquals(Proficiency.NONE, character.skillProficiencies.getValue(Skill.STEALTH))
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
            skills = mapOf(
                Skill.SLEIGHT_OF_HAND to Proficiency.EXPERTISE,
                Skill.ATHLETICS to Proficiency.PROFICIENT,
                Skill.ARCANA to Proficiency.NONE,
            ),
        )

        val encoded = json.encodeToString(newCharacter.toRequest())

        val expected = """
            {"name":"Tordek","level":5,
             "abilityScores":{"strength":16,"dexterity":12,"constitution":15,
                              "intelligence":10,"wisdom":13,"charisma":8},
             "skills":{"SLEIGHT_OF_HAND":"EXPERTISE","ATHLETICS":"PROFICIENT"}}
        """.trimIndent()
        assertEquals(json.parseToJsonElement(expected), json.parseToJsonElement(encoded))
    }

    @Test
    fun `serializes a skills update with only the skills that have proficiency`() {
        val skills = mapOf(Skill.STEALTH to Proficiency.PROFICIENT, Skill.ARCANA to Proficiency.NONE)

        val encoded = json.encodeToString(skills.toUpdateSkillsRequest())

        assertEquals(
            json.parseToJsonElement("""{"skills":{"STEALTH":"PROFICIENT"}}"""),
            json.parseToJsonElement(encoded),
        )
    }
}
