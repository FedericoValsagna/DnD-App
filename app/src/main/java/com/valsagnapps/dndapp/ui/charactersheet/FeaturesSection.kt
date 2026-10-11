package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassFeature
import com.valsagnapps.dndapp.domain.ClassLevel
import com.valsagnapps.dndapp.ui.common.nameRes
import com.valsagnapps.dndapp.ui.theme.DnDAppTheme

/**
 * Features of each class by level, with their summary. The SRD text, for the features that have it, shows on demand.
 * With more than one class, each one goes under its name.
 */
@Composable
internal fun FeaturesSection(character: Character, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        character.classes.forEach { classLevel ->
            if (character.classes.size > 1) {
                Text(stringResource(classLevel.characterClass.nameRes()), style = MaterialTheme.typography.titleMedium)
            }
            val features = character.features[classLevel.characterClass].orEmpty()
            if (features.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_features),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            features.forEach { FeatureItem(it) }
        }
    }
}

@Composable
private fun FeatureItem(feature: ClassFeature, modifier: Modifier = Modifier) {
    var showSrdText by rememberSaveable(feature.id) { mutableStateOf(false) }
    Column(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(feature.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.level_value, feature.level),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(feature.summary, style = MaterialTheme.typography.bodyMedium)
        feature.srdText?.let { srdText ->
            TextButton(onClick = { showSrdText = !showSrdText }) {
                Text(stringResource(if (showSrdText) R.string.hide_srd_text else R.string.show_srd_text))
            }
            if (showSrdText) {
                Text(srdText, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FeaturesSectionPreview() {
    DnDAppTheme {
        FeaturesSection(
            Character(
                id = "1",
                name = "Tordek",
                level = 5,
                proficiencyBonus = 3,
                abilities = emptyMap(),
                classes = listOf(ClassLevel(CharacterClass.RANGER, level = 5)),
                features = mapOf(
                    CharacterClass.RANGER to listOf(
                        ClassFeature(
                            id = "RANGER_FAVORED_ENEMY",
                            name = "Favored Enemy",
                            level = 1,
                            summary = "Advantage to track your favored enemies.",
                            srdText = "Beginning at 1st level, you have significant experience studying...",
                        ),
                        ClassFeature(
                            id = "RANGER_EXTRA_ATTACK",
                            name = "Extra Attack",
                            level = 5,
                            summary = "Attack twice.",
                        ),
                    ),
                ),
            ),
        )
    }
}
