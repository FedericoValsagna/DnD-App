package com.valsagnapps.dndapp.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.CharacterRules

/** A field for each ability score, in two columns: STR/DEX, CON/INT, WIS/CHA. */
@Composable
fun AbilityScoreGrid(
    scores: Map<Ability, String>,
    onScoreChange: (Ability, String) -> Unit,
    showError: (Ability) -> Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Ability.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { ability ->
                    NumberField(
                        value = scores[ability].orEmpty(),
                        onValueChange = { onScoreChange(ability, it) },
                        label = stringResource(ability.nameRes()),
                        range = CharacterRules.ABILITY_SCORE_RANGE,
                        showError = showError(ability),
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
