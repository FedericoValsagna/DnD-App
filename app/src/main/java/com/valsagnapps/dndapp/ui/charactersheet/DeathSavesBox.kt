package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.DeathSaves

private const val DISABLED_ALPHA = 0.38f

/**
 * Three points of successes and three of failures, marked first. As with slots, tapping an empty point
 * marks one more and tapping a marked one removes one. Both can't be complete: with three of one kind,
 * the empty points of the other are disabled.
 */
@Composable
internal fun DeathSavesBox(
    deathSaves: DeathSaves,
    enabled: Boolean,
    onChange: (DeathSaves) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            DeathSaveRow(
                label = stringResource(R.string.death_save_successes),
                count = deathSaves.successes,
                color = MaterialTheme.colorScheme.primary,
                canAdd = enabled && deathSaves.failures < DeathSaves.MAX,
                canRemove = enabled,
                onCountChange = { onChange(deathSaves.copy(successes = it)) },
            )
            DeathSaveRow(
                label = stringResource(R.string.death_save_failures),
                count = deathSaves.failures,
                color = MaterialTheme.colorScheme.onSurface,
                canAdd = enabled && deathSaves.successes < DeathSaves.MAX,
                canRemove = enabled,
                onCountChange = { onChange(deathSaves.copy(failures = it)) },
            )
        }
    }
}

@Composable
private fun DeathSaveRow(
    label: String,
    count: Int,
    color: Color,
    canAdd: Boolean,
    canRemove: Boolean,
    onCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Row {
            repeat(DeathSaves.MAX) { index ->
                val marked = index < count
                DeathSavePip(
                    marked = marked,
                    color = color,
                    enabled = if (marked) canRemove else canAdd,
                    description = stringResource(
                        if (marked) R.string.death_save_pip_marked else R.string.death_save_pip,
                        label,
                        index + 1,
                    ),
                    onToggle = { onCountChange(if (marked) count - 1 else count + 1) },
                )
            }
        }
    }
}

/** A 28dp point inside a 48dp touch area: easy to hit at the table. */
@Composable
private fun DeathSavePip(
    marked: Boolean,
    color: Color,
    enabled: Boolean,
    description: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .toggleable(value = marked, enabled = enabled, onValueChange = { onToggle() })
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(28.dp)
                .alpha(if (enabled) 1f else DISABLED_ALPHA)
                .border(2.dp, color, CircleShape)
                .then(if (marked) Modifier.background(color, CircleShape) else Modifier),
        )
    }
}
