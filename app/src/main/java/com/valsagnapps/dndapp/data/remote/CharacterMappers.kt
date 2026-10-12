package com.valsagnapps.dndapp.data.remote

import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.AbilityScore
import com.valsagnapps.dndapp.domain.ActionType
import com.valsagnapps.dndapp.domain.ArmorProficiency
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassFeature
import com.valsagnapps.dndapp.domain.ClassInfo
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.domain.DeathSaves
import com.valsagnapps.dndapp.domain.HitDice
import com.valsagnapps.dndapp.domain.LifeStatus
import com.valsagnapps.dndapp.domain.NewCharacter
import com.valsagnapps.dndapp.domain.Proficiencies
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Recharge
import com.valsagnapps.dndapp.domain.Resource
import com.valsagnapps.dndapp.domain.SavingThrow
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.domain.SkillChoice
import com.valsagnapps.dndapp.domain.SkillValue
import com.valsagnapps.dndapp.domain.Subclass
import com.valsagnapps.dndapp.domain.ToolCategory
import com.valsagnapps.dndapp.domain.ToolChoice
import com.valsagnapps.dndapp.domain.ToolProficiency
import com.valsagnapps.dndapp.domain.UsesDisplay
import com.valsagnapps.dndapp.domain.WeaponProficiency

fun CharacterDto.toDomain(): Character = Character(
    id = id,
    name = name,
    level = level,
    proficiencyBonus = proficiencyBonus,
    // Abilities and skills the app doesn't know about are ignored instead of failing the whole response.
    abilities = abilities.mapNotNull { (key, dto) ->
        Ability.entries.find { it.name == key }?.let { it to AbilityScore(dto.score, dto.modifier) }
    }.toMap(),
    skills = skills.mapNotNull { (key, dto) ->
        val skill = Skill.entries.find { it.name == key }
        val proficiency = Proficiency.entries.find { it.name == dto.proficiency }
        if (skill == null || proficiency == null) null else skill to SkillValue(proficiency, dto.bonus)
    }.toMap(),
    passivePerception = passivePerception,
    // Classes from books the app doesn't know yet are left out.
    classes = classes.mapNotNull { dto ->
        enumValueOrNull<CharacterClass>(dto.characterClass)?.let { ClassLevel(it, dto.level, dto.subclass?.toDomain()) }
    },
    maxHitPoints = maxHitPoints,
    hitDice = hitDice.map { HitDice(it.die, it.count) },
    savingThrows = savingThrows.mapNotNull { (key, dto) ->
        val ability = enumValueOrNull<Ability>(key)
        val proficiency = enumValueOrNull<Proficiency>(dto.proficiency)
        if (ability == null || proficiency == null) null else ability to SavingThrow(proficiency, dto.bonus)
    }.toMap(),
    skillChoices = classes.mapNotNull { dto ->
        val characterClass = enumValueOrNull<CharacterClass>(dto.characterClass)
        val choice = dto.skillChoices
        if (characterClass == null || choice == null) null else characterClass to choice.toSkillChoice()
    }.toMap(),
    proficiencies = proficiencies?.toDomain(),
    features = classes.mapNotNull { dto ->
        enumValueOrNull<CharacterClass>(dto.characterClass)?.let { characterClass ->
            characterClass to dto.features.map { it.toDomain() }
        }
    }.toMap(),
    currentHitPoints = currentHitPoints,
    temporaryHitPoints = temporaryHitPoints,
    lifeStatus = lifeStatus?.let { enumValueOrNull<LifeStatus>(it) } ?: LifeStatus.CONSCIOUS,
    deathSaves = DeathSaves(deathSaves.successes, deathSaves.failures),
    armorClass = armorClass,
    armorClassOverride = armorClassOverride,
    initiative = initiative,
    speed = speed,
    baseSpeed = baseSpeed,
    resources = resources.map { it.toDomain() },
)

private fun ClassFeatureDto.toDomain() = ClassFeature(
    id = id,
    name = name,
    level = level,
    summary = summary,
    srdText = srdText,
    actionType = actionType?.let { enumValueOrNull<ActionType>(it) },
    resource = resource,
)

// A display the app doesn't know is shown as points.
private fun ResourceDto.toDomain() = Resource(
    id = id,
    name = name,
    max = max,
    used = used,
    recharge = recharge?.let { enumValueOrNull<Recharge>(it) },
    display = display?.let { enumValueOrNull<UsesDisplay>(it) } ?: UsesDisplay.PIPS,
)

// Classes the app doesn't know yet are left out, as in the character.
fun List<ClassDto>.toDomain(): List<ClassInfo> = mapNotNull { dto ->
    enumValueOrNull<CharacterClass>(dto.characterClass)
        ?.let { ClassInfo(it, dto.subclassLevel, dto.subclasses.map { subclass -> subclass.toDomain() }) }
}

private fun SubclassDto.toDomain() = Subclass(id, name)

// Values the app doesn't know (new books) are left out, as with classes.
private fun ProficienciesDto.toDomain() = Proficiencies(
    armor = armor.mapNotNull { enumValueOrNull<ArmorProficiency>(it) },
    weapons = weapons.mapNotNull { enumValueOrNull<WeaponProficiency>(it) },
    tools = tools.mapNotNull { enumValueOrNull<ToolProficiency>(it) },
    toolChoices = toolChoices.mapNotNull { dto ->
        dto.options.mapNotNull { enumValueOrNull<ToolCategory>(it) }
            .takeIf { it.isNotEmpty() }
            ?.let { ToolChoice(dto.count, it) }
    },
)

private fun ChoiceDto.toSkillChoice() = SkillChoice(count, options.mapNotNull { enumValueOrNull<Skill>(it) }.toSet())

private inline fun <reified E : Enum<E>> enumValueOrNull(name: String): E? = enumValues<E>().find { it.name == name }

fun NewCharacter.toRequest(): CreateCharacterRequest = CreateCharacterRequest(
    name = name,
    classes = classes.map { it.toRequest() },
    maxHitPoints = maxHitPoints,
    abilityScores = abilityScores.toAbilityScoresDto(),
    skills = skills.toSkillsDto(),
)

fun List<ClassLevel>.toUpdateClassesRequest(): UpdateClassesRequest = UpdateClassesRequest(map { it.toRequest() })

private fun ClassLevel.toRequest() = ClassLevelRequest(characterClass.name, level)

fun Map<Skill, Proficiency>.toUpdateSkillsRequest(): UpdateSkillsRequest = UpdateSkillsRequest(toSkillsDto())

private fun Map<Skill, Proficiency>.toSkillsDto(): Map<String, String> =
    filterValues { it != Proficiency.NONE }.entries.associate { (skill, proficiency) -> skill.name to proficiency.name }
