package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.domain.ActionType
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassFeature
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.domain.Recharge
import com.valsagnapps.dndapp.domain.Resource
import com.valsagnapps.dndapp.domain.UsesDisplay

val kiFeature = ClassFeature("MONK_KI", "Ki", 2, "Ki points.", resource = "KI")
val flurryOfBlows =
    ClassFeature(
        "MONK_FLURRY_OF_BLOWS",
        "Flurry of Blows",
        2,
        "Two unarmed strikes.",
        null,
        ActionType.BONUS_ACTION,
        "KI",
    )
val deflectMissiles =
    ClassFeature("MONK_DEFLECT_MISSILES", "Deflect Missiles", 3, "Reduce ranged damage.", null, ActionType.REACTION)
val wholenessOfBody = ClassFeature(
    "OPEN_HAND_WHOLENESS_OF_BODY",
    "Wholeness of Body",
    6,
    "Heal three times your monk level.",
    null,
    ActionType.ACTION,
    "WHOLENESS_OF_BODY",
)
val kiPool = Resource("KI", "Ki", max = 6, used = 1, recharge = Recharge.SHORT_REST, display = UsesDisplay.POOL)
val wholeness = Resource("WHOLENESS_OF_BODY", "Wholeness of Body", 1, 0, Recharge.LONG_REST, UsesDisplay.PIPS)

/** A 6th level Open Hand monk with Ki (a pool, 5 of 6 left) and Wholeness of Body (one point). */
fun sampleMonk(id: String = "1") = sampleCharacter(id = id, name = "Ember", level = 6).copy(
    classes = listOf(ClassLevel(CharacterClass.MONK, 6)),
    currentHitPoints = 44,
    features = mapOf(CharacterClass.MONK to listOf(kiFeature, flurryOfBlows, deflectMissiles, wholenessOfBody)),
    resources = listOf(kiPool, wholeness),
)
