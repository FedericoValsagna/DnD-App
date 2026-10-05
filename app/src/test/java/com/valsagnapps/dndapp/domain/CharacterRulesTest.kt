package com.valsagnapps.dndapp.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CharacterRulesTest {

    @Test
    fun `accepts names from 1 to 100 characters`() {
        assertTrue(CharacterRules.isValidName("T"))
        assertTrue(CharacterRules.isValidName("a".repeat(100)))
    }

    @Test
    fun `rejects blank or too long names`() {
        assertFalse(CharacterRules.isValidName(""))
        assertFalse(CharacterRules.isValidName("   "))
        assertFalse(CharacterRules.isValidName("a".repeat(101)))
    }

    @Test
    fun `accepts levels from 1 to 20 only`() {
        assertTrue(CharacterRules.isValidLevel(1))
        assertTrue(CharacterRules.isValidLevel(20))
        assertFalse(CharacterRules.isValidLevel(0))
        assertFalse(CharacterRules.isValidLevel(21))
    }

    @Test
    fun `accepts ability scores from 1 to 30 only`() {
        assertTrue(CharacterRules.isValidAbilityScore(1))
        assertTrue(CharacterRules.isValidAbilityScore(30))
        assertFalse(CharacterRules.isValidAbilityScore(0))
        assertFalse(CharacterRules.isValidAbilityScore(31))
    }

    @Test
    fun `accepts max hit points from 1 to 999 only`() {
        assertTrue(CharacterRules.isValidMaxHitPoints(1))
        assertTrue(CharacterRules.isValidMaxHitPoints(999))
        assertFalse(CharacterRules.isValidMaxHitPoints(0))
        assertFalse(CharacterRules.isValidMaxHitPoints(1000))
    }
}
