package com.valsagnapps.dndapp.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.Proficiency
import com.valsagnapps.dndapp.domain.Skill
import com.valsagnapps.dndapp.ui.theme.DnDAppTheme

/**
 * A skill with its proficiency marker. Tapping it opens a menu to pick the proficiency.
 * [trailing] is shown at the end (the bonus in the sheet; nothing in the creation form).
 */
@Composable
fun SkillRow(
    skill: Skill,
    proficiency: Proficiency,
    onProficiencyChange: (Proficiency) -> Unit,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    enabled: Boolean = true,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { menuExpanded = true }
                .padding(vertical = 10.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ProficiencyMarker(proficiency)
            Text(stringResource(skill.nameRes()), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = stringResource(R.string.skill_ability, stringResource(skill.ability.shortNameRes())),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            trailing?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            Proficiency.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(stringResource(option.nameRes())) },
                    leadingIcon = { ProficiencyMarker(option) },
                    onClick = {
                        menuExpanded = false
                        if (option != proficiency) onProficiencyChange(option)
                    },
                )
            }
        }
    }
}

/** ○ not proficient, ● proficient, ◉ expertise (like the circles on a paper sheet). */
@Composable
private fun ProficiencyMarker(proficiency: Proficiency, modifier: Modifier = Modifier) {
    val description = stringResource(proficiency.nameRes())
    Text(
        text = stringResource(
            when (proficiency) {
                Proficiency.NONE -> R.string.proficiency_marker_none
                Proficiency.PROFICIENT -> R.string.proficiency_marker_proficient
                Proficiency.EXPERTISE -> R.string.proficiency_marker_expertise
            },
        ),
        style = MaterialTheme.typography.titleMedium,
        color = if (proficiency == Proficiency.NONE) {
            MaterialTheme.colorScheme.outline
        } else {
            MaterialTheme.colorScheme.primary
        },
        modifier = modifier
            .width(20.dp)
            .semantics { contentDescription = description },
    )
}

@Preview(showBackground = true)
@Composable
private fun SkillRowPreview() {
    DnDAppTheme {
        Column {
            SkillRow(Skill.ATHLETICS, Proficiency.NONE, {}, trailing = "+3")
            SkillRow(Skill.PERCEPTION, Proficiency.PROFICIENT, {}, trailing = "+4")
            SkillRow(Skill.STEALTH, Proficiency.EXPERTISE, {}, trailing = "+7")
        }
    }
}
