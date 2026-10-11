package com.valsagnapps.dndapp.data.remote

import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.AbilityScore
import com.valsagnapps.dndapp.domain.ArmorProficiency
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassFeature
import com.valsagnapps.dndapp.domain.ClassInfo
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.domain.HitDice
import com.valsagnapps.dndapp.domain.NewCharacter
import com.valsagnapps.dndapp.domain.Proficiencies
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.SavingThrow
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.domain.SkillChoice
import com.valsagnapps.dndapp.domain.SkillValue
import com.valsagnapps.dndapp.domain.Subclass
import com.valsagnapps.dndapp.domain.ToolCategory
import com.valsagnapps.dndapp.domain.ToolChoice
import com.valsagnapps.dndapp.domain.ToolProficiency
import com.valsagnapps.dndapp.domain.WeaponProficiency
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
             "passivePerception":17,
             "classes":[{"class":"FIGHTER","level":3,"hitDie":10},{"class":"CLERIC","level":2,"hitDie":8}],
             "maxHitPoints":44,
             "hitDice":[{"die":10,"count":3},{"die":8,"count":2}],
             "savingThrows":{"STRENGTH":{"proficiency":"PROFICIENT","bonus":6},
                             "WISDOM":{"proficiency":"NONE","bonus":1}}}
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
                classes = listOf(ClassLevel(CharacterClass.FIGHTER, 3), ClassLevel(CharacterClass.CLERIC, 2)),
                maxHitPoints = 44,
                hitDice = listOf(HitDice(10, 3), HitDice(8, 2)),
                savingThrows = mapOf(
                    Ability.STRENGTH to SavingThrow(Proficiency.PROFICIENT, 6),
                    Ability.WISDOM to SavingThrow(Proficiency.NONE, 1),
                ),
                features = mapOf(CharacterClass.FIGHTER to emptyList(), CharacterClass.CLERIC to emptyList()),
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
    fun `reads a character without classes, hit points or saving throws from an older server`() {
        val body = """{"id":"1","name":"Tordek","level":1,"proficiencyBonus":2,"abilities":{}}"""

        val character = json.decodeFromString<CharacterDto>(body).toDomain()

        assertEquals(emptyList<ClassLevel>(), character.classes)
        assertEquals(null, character.maxHitPoints)
        assertEquals(emptyList<HitDice>(), character.hitDice)
        assertEquals(emptyMap<Ability, SavingThrow>(), character.savingThrows)
    }

    @Test
    fun `ignores classes, abilities or proficiencies the app does not know`() {
        val dto = CharacterDto(
            id = "1",
            name = "Tordek",
            level = 4,
            proficiencyBonus = 2,
            abilities = emptyMap(),
            classes = listOf(ClassLevelDto("ARTIFICER", 1, 8), ClassLevelDto("ROGUE", 3, 8)),
            savingThrows = mapOf(
                "DEXTERITY" to SavingThrowDto("PROFICIENT", 4),
                "LUCK" to SavingThrowDto("PROFICIENT", 4),
                "WISDOM" to SavingThrowDto("HALF", 1),
            ),
        )

        val character = dto.toDomain()

        assertEquals(listOf(ClassLevel(CharacterClass.ROGUE, 3)), character.classes)
        assertEquals(mapOf(Ability.DEXTERITY to SavingThrow(Proficiency.PROFICIENT, 4)), character.savingThrows)
    }

    @Test
    fun `serializes a new character with the keys the server expects`() {
        val newCharacter = NewCharacter(
            name = "Tordek",
            classes = listOf(ClassLevel(CharacterClass.FIGHTER, 5)),
            maxHitPoints = 44,
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
            {"name":"Tordek","classes":[{"class":"FIGHTER","level":5}],"maxHitPoints":44,
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

    @Test
    fun `maps the subclass of each class`() {
        val body = """
            {"id":"1","name":"Jozan","level":5,"proficiencyBonus":3,"abilities":{},
             "classes":[{"class":"CLERIC","level":3,"subclassLevel":1,
                         "subclass":{"id":"LIFE","name":"Life Domain","source":"PHB"}},
                        {"class":"FIGHTER","level":2,"subclassLevel":3,"subclass":null}]}
        """.trimIndent()

        val character = json.decodeFromString<CharacterDto>(body).toDomain()

        assertEquals(
            listOf(
                ClassLevel(CharacterClass.CLERIC, 3, Subclass("LIFE", "Life Domain")),
                ClassLevel(CharacterClass.FIGHTER, 2),
            ),
            character.classes,
        )
    }

    @Test
    fun `maps the features of each class`() {
        val body = """
            {"id":"1","name":"Jozan","level":3,"proficiencyBonus":2,"abilities":{},
             "classes":[{"class":"CLERIC","level":2,
                         "features":[{"id":"CLERIC_CHANNEL_DIVINITY","name":"Channel Divinity","level":2,
                                      "summary":"Once per rest.","srdText":"At 2nd level...","source":"PHB"},
                                     {"id":"NATURE_BONUS_PROFICIENCY","name":"Bonus Proficiency","level":1,
                                      "summary":"Heavy armor.","srdText":null,"source":"PHB"}]},
                        {"class":"FIGHTER","level":1,"features":[]}]}
        """.trimIndent()

        val character = json.decodeFromString<CharacterDto>(body).toDomain()

        assertEquals(
            mapOf(
                CharacterClass.CLERIC to listOf(
                    ClassFeature("CLERIC_CHANNEL_DIVINITY", "Channel Divinity", 2, "Once per rest.", "At 2nd level..."),
                    ClassFeature("NATURE_BONUS_PROFICIENCY", "Bonus Proficiency", 1, "Heavy armor."),
                ),
                CharacterClass.FIGHTER to emptyList(),
            ),
            character.features,
        )
    }

    @Test
    fun `classes without features from an older server have none`() {
        val body = """{"id":"1","name":"Jozan","level":1,"proficiencyBonus":2,"abilities":{},
            "classes":[{"class":"CLERIC","level":1}]}"""

        val character = json.decodeFromString<CharacterDto>(body).toDomain()

        assertEquals(mapOf(CharacterClass.CLERIC to emptyList<ClassFeature>()), character.features)
    }

    @Test
    fun `maps the class catalog ignoring classes the app does not know`() {
        val body = """
            [{"class":"CLERIC","hitDie":8,"subclassLevel":1,"source":"PHB",
              "subclasses":[{"id":"LIFE","name":"Life Domain","source":"PHB"}]},
             {"class":"ARTIFICER","hitDie":8,"subclassLevel":3,"source":"TCE","subclasses":[]}]
        """.trimIndent()

        val catalog = json.decodeFromString<List<ClassDto>>(body).toDomain()

        assertEquals(listOf(ClassInfo(CharacterClass.CLERIC, 1, listOf(Subclass("LIFE", "Life Domain")))), catalog)
    }

    @Test
    fun `serializes a subclass update with null to remove it`() {
        assertEquals(
            json.parseToJsonElement("""{"subclass":null}"""),
            json.parseToJsonElement(json.encodeToString(UpdateSubclassRequest(null))),
        )
    }

    @Test
    fun `serializes a classes update with the class key the server expects`() {
        val classes = listOf(ClassLevel(CharacterClass.RANGER, 5), ClassLevel(CharacterClass.ROGUE, 2))

        val encoded = json.encodeToString(classes.toUpdateClassesRequest())

        assertEquals(
            json.parseToJsonElement("""{"classes":[{"class":"RANGER","level":5},{"class":"ROGUE","level":2}]}"""),
            json.parseToJsonElement(encoded),
        )
    }

    @Test
    fun `maps the skill choices of each class and the proficiencies`() {
        val body = """
            {"id":"1","name":"Lidda","level":3,"proficiencyBonus":2,"abilities":{},
             "classes":[{"class":"FIGHTER","level":2,"skillChoices":{"count":2,"options":["ATHLETICS","PERCEPTION"]}},
                        {"class":"CLERIC","level":1,"skillChoices":{"count":0,"options":[]}}],
             "proficiencies":{"armor":["LIGHT","MEDIUM","HEAVY","SHIELDS"],"weapons":["SIMPLE","MARTIAL"],
                              "tools":["THIEVES_TOOLS"],
                              "toolChoices":[{"count":3,"options":["MUSICAL_INSTRUMENT"]}]}}
        """.trimIndent()

        val character = json.decodeFromString<CharacterDto>(body).toDomain()

        assertEquals(
            mapOf(
                CharacterClass.FIGHTER to SkillChoice(2, setOf(Skill.ATHLETICS, Skill.PERCEPTION)),
                CharacterClass.CLERIC to SkillChoice(0, emptySet()),
            ),
            character.skillChoices,
        )
        assertEquals(
            Proficiencies(
                armor = ArmorProficiency.entries,
                weapons = listOf(WeaponProficiency.SIMPLE, WeaponProficiency.MARTIAL),
                tools = listOf(ToolProficiency.THIEVES_TOOLS),
                toolChoices = listOf(ToolChoice(3, listOf(ToolCategory.MUSICAL_INSTRUMENT))),
            ),
            character.proficiencies,
        )
    }

    @Test
    fun `ignores proficiencies and skill options the app does not know`() {
        val dto = CharacterDto(
            id = "1",
            name = "Tordek",
            level = 1,
            proficiencyBonus = 2,
            abilities = emptyMap(),
            classes = listOf(
                ClassLevelDto("FIGHTER", 1, skillChoices = ChoiceDto(1, listOf("ATHLETICS", "SPELUNKING"))),
                ClassLevelDto("ARTIFICER", 1, skillChoices = ChoiceDto(2, listOf("ARCANA"))),
            ),
            proficiencies = ProficienciesDto(
                armor = listOf("LIGHT", "MITHRAL"),
                weapons = listOf("SIMPLE", "FIREARMS"),
                tools = listOf("TINKERS_TOOLS"),
                toolChoices = listOf(ChoiceDto(1, listOf("GAMING_SET")), ChoiceDto(1, listOf("ARTISANS_TOOLS"))),
            ),
        )

        val character = dto.toDomain()

        assertEquals(mapOf(CharacterClass.FIGHTER to SkillChoice(1, setOf(Skill.ATHLETICS))), character.skillChoices)
        assertEquals(
            Proficiencies(
                armor = listOf(ArmorProficiency.LIGHT),
                weapons = listOf(WeaponProficiency.SIMPLE),
                toolChoices = listOf(ToolChoice(1, listOf(ToolCategory.ARTISANS_TOOLS))),
            ),
            character.proficiencies,
        )
    }

    @Test
    fun `an old server without proficiencies maps to none`() {
        val body = """{"id":"1","name":"Tordek","level":1,"proficiencyBonus":2,"abilities":{},
            "classes":[{"class":"FIGHTER","level":1}]}"""

        val character = json.decodeFromString<CharacterDto>(body).toDomain()

        assertEquals(null, character.proficiencies)
        assertEquals(emptyMap<CharacterClass, SkillChoice>(), character.skillChoices)
    }
}
