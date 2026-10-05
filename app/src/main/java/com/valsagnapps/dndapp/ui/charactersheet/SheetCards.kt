package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.AbilityScore
import com.valsagnapps.dndapp.ui.common.nameRes

/** A labeled value. If [onClick] is not null the card can be tapped (to edit the value). */
@Composable
internal fun StatCard(label: String, value: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier) { StatCardContent(label, value) }
    } else {
        Card(modifier) { StatCardContent(label, value) }
    }
}

@Composable
private fun StatCardContent(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
internal fun AbilityCard(ability: Ability, score: AbilityScore?, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(ability.nameRes()), style = MaterialTheme.typography.labelLarge)
            if (score == null) {
                // The server didn't send this ability.
                Text(stringResource(R.string.missing_value), style = MaterialTheme.typography.headlineMedium)
            } else {
                Text(
                    text = stringResource(R.string.signed_value, score.modifier),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = stringResource(R.string.ability_score_value, score.score),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
