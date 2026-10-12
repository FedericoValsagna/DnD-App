package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.ActionType
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassFeature
import com.valsagnapps.dndapp.domain.Recharge
import com.valsagnapps.dndapp.domain.Resource
import com.valsagnapps.dndapp.domain.UsesDisplay
import com.valsagnapps.dndapp.domain.combatActionList
import com.valsagnapps.dndapp.domain.resourceOf
import com.valsagnapps.dndapp.ui.theme.DnDAppTheme

/**
 * The character's actions under Action, Bonus Action and Reaction, and at the end Otros (resources no action
 * spends). Each row shows the name and its uses: points that are tapped right there, or a pool as a number.
 * Tapping the row opens its sheet. Only this list scrolls: what's above stays.
 */
@Composable
internal fun CombatActionsList(
    character: Character,
    enabled: Boolean,
    actions: CombatActions,
    modifier: Modifier = Modifier,
) {
    val list = character.combatActionList()
    if (list.isEmpty) {
        Text(
            text = stringResource(R.string.combat_no_actions),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(top = 4.dp),
        )
        return
    }
    Column(modifier.verticalScroll(rememberScrollState())) {
        list.byType.forEach { (type, features) ->
            ActionSection(stringResource(type.label), features, character, enabled, actions)
        }
        if (list.others.isNotEmpty()) {
            ActionSection(stringResource(R.string.combat_others), list.others, character, enabled, actions)
        }
    }
}

internal val ActionType.label: Int
    get() = when (this) {
        ActionType.ACTION -> R.string.action_type_action
        ActionType.BONUS_ACTION -> R.string.action_type_bonus_action
        ActionType.REACTION -> R.string.action_type_reaction
    }

@Composable
private fun ActionSection(
    title: String,
    features: List<ClassFeature>,
    character: Character,
    enabled: Boolean,
    actions: CombatActions,
) {
    SectionLabel(title, Modifier.padding(top = 12.dp, bottom = 2.dp))
    features.forEach { feature ->
        ActionRow(
            name = feature.name,
            resource = character.resourceOf(feature),
            enabled = enabled,
            onClick = { actions.onOpenFeature(feature) },
            onUsedChange = { resource, used -> actions.onResourceUsedChange(resource.id, used) },
        )
    }
}

/** The name and, on the right, the uses: a pool as "left/max", few uses as points. */
@Composable
private fun ActionRow(
    name: String,
    resource: Resource?,
    enabled: Boolean,
    onClick: () -> Unit,
    onUsedChange: (Resource, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(enabled = enabled, onClick = onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            when {
                resource == null -> Unit
                resource.display == UsesDisplay.POOL -> ValueChip(poolText(resource))
                else -> UsePips(resource, enabled, onUsedChange = { onUsedChange(resource, it) })
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** "20/25": what's left of the max. */
@Composable
internal fun poolText(resource: Resource): String =
    stringResource(R.string.pool_value, resource.remaining, resource.max)

internal val previewFighterFeatures = listOf(
    ClassFeature(
        "FIGHTER_SECOND_WIND",
        "Second Wind",
        1,
        "Regain 1d10 + level HP.",
        null,
        ActionType.BONUS_ACTION,
        "SECOND_WIND",
    ),
    ClassFeature("FIGHTER_ACTION_SURGE", "Action Surge", 2, "One more action this turn.", resource = "ACTION_SURGE"),
    ClassFeature("FIGHTER_EXTRA_ATTACK", "Extra Attack", 5, "Attack twice."),
)

private val previewPaladin = Character(
    id = "1",
    name = "Aldric",
    level = 5,
    proficiencyBonus = 3,
    abilities = emptyMap(),
    features = mapOf(
        CharacterClass.PALADIN to listOf(
            ClassFeature("PALADIN_DIVINE_SENSE", "Divine Sense", 1, "", null, ActionType.ACTION, "DIVINE_SENSE"),
            ClassFeature("PALADIN_LAY_ON_HANDS", "Lay on Hands", 1, "", null, ActionType.ACTION, "LAY_ON_HANDS"),
            ClassFeature("VENGEANCE_ABJURE_ENEMY", "Abjure Enemy", 3, "", null, ActionType.ACTION, "CHANNEL_DIVINITY"),
            ClassFeature(
                "VENGEANCE_VOW_OF_ENMITY",
                "Vow of Enmity",
                3,
                "",
                null,
                ActionType.BONUS_ACTION,
                "CHANNEL_DIVINITY",
            ),
        ),
    ),
    resources = listOf(
        Resource("DIVINE_SENSE", "Divine Sense", 4, 1, Recharge.LONG_REST, UsesDisplay.PIPS),
        Resource("LAY_ON_HANDS", "Lay on Hands", 25, 5, Recharge.LONG_REST, UsesDisplay.POOL),
        Resource("CHANNEL_DIVINITY", "Channel Divinity", 1, 0, Recharge.SHORT_REST, UsesDisplay.PIPS),
    ),
)

@Preview(showBackground = true)
@Composable
private fun CombatActionsListPreview() {
    DnDAppTheme {
        CombatActionsList(previewPaladin, enabled = true, actions = CombatActions(), modifier = Modifier.padding(16.dp))
    }
}
