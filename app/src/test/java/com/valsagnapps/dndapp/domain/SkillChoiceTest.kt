package com.valsagnapps.dndapp.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkillChoiceTest {

    private val fighter = SkillChoice(2, setOf(Skill.ATHLETICS, Skill.PERCEPTION, Skill.SURVIVAL))
    private val bard = SkillChoice(3, Skill.entries.toSet())

    @Test
    fun `counts the class skills that have proficiency`() {
        val proficiencies = mapOf(
            Skill.ATHLETICS to Proficiency.PROFICIENT,
            Skill.PERCEPTION to Proficiency.EXPERTISE,
            Skill.SURVIVAL to Proficiency.NONE,
            Skill.STEALTH to Proficiency.PROFICIENT,
        )

        assertEquals(2, fighter.chosenIn(proficiencies))
    }

    @Test
    fun `a choice from every skill is any skill`() {
        assertTrue(bard.isAnySkill)
        assertFalse(fighter.isAnySkill)
    }

    @Test
    fun `class skills join the options of every class except any-skill ones`() {
        val character = Character(
            id = "1",
            name = "Lidda",
            level = 2,
            proficiencyBonus = 2,
            abilities = emptyMap(),
            skillChoices = mapOf(
                CharacterClass.BARD to bard,
                CharacterClass.FIGHTER to fighter,
                CharacterClass.ROGUE to SkillChoice(1, setOf(Skill.STEALTH)),
            ),
        )

        assertEquals(setOf(Skill.ATHLETICS, Skill.PERCEPTION, Skill.SURVIVAL, Skill.STEALTH), character.classSkills)
    }
}
