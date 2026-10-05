package com.valsagnapps.dndapp.ui.common

import androidx.annotation.StringRes
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill

@StringRes
fun Skill.nameRes(): Int = when (this) {
    Skill.ACROBATICS -> R.string.skill_acrobatics
    Skill.ANIMAL_HANDLING -> R.string.skill_animal_handling
    Skill.ARCANA -> R.string.skill_arcana
    Skill.ATHLETICS -> R.string.skill_athletics
    Skill.DECEPTION -> R.string.skill_deception
    Skill.HISTORY -> R.string.skill_history
    Skill.INSIGHT -> R.string.skill_insight
    Skill.INTIMIDATION -> R.string.skill_intimidation
    Skill.INVESTIGATION -> R.string.skill_investigation
    Skill.MEDICINE -> R.string.skill_medicine
    Skill.NATURE -> R.string.skill_nature
    Skill.PERCEPTION -> R.string.skill_perception
    Skill.PERFORMANCE -> R.string.skill_performance
    Skill.PERSUASION -> R.string.skill_persuasion
    Skill.RELIGION -> R.string.skill_religion
    Skill.SLEIGHT_OF_HAND -> R.string.skill_sleight_of_hand
    Skill.STEALTH -> R.string.skill_stealth
    Skill.SURVIVAL -> R.string.skill_survival
}

@StringRes
fun Proficiency.nameRes(): Int = when (this) {
    Proficiency.NONE -> R.string.proficiency_none
    Proficiency.PROFICIENT -> R.string.proficiency_proficient
    Proficiency.EXPERTISE -> R.string.proficiency_expertise
}
