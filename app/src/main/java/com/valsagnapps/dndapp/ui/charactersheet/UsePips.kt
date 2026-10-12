package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.Resource

private const val DISABLED_ALPHA = 0.38f

/**
 * The uses of [resource] as points, available first: full is available, empty is spent. Tapping a full one
 * spends one and tapping an empty one recovers one ([onUsedChange] gets the used uses after the tap).
 */
@Composable
internal fun UsePips(
    resource: Resource,
    enabled: Boolean,
    onUsedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(modifier) {
        repeat(resource.max) { index ->
            val available = index < resource.remaining
            UsePip(
                available = available,
                enabled = enabled,
                description = stringResource(
                    if (available) R.string.use_pip else R.string.use_pip_spent,
                    resource.name,
                    index + 1,
                    resource.max,
                ),
                onToggle = { onUsedChange(if (available) resource.used + 1 else resource.used - 1) },
            )
        }
    }
}

/** A 20dp point inside a 48dp touch area: easy to hit at the table. */
@Composable
private fun UsePip(
    available: Boolean,
    enabled: Boolean,
    description: String,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .size(48.dp)
            .toggleable(value = available, enabled = enabled, onValueChange = { onToggle() })
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(20.dp)
                .alpha(if (enabled) 1f else DISABLED_ALPHA)
                .border(2.dp, color, CircleShape)
                .then(if (available) Modifier.background(color, CircleShape) else Modifier),
        )
    }
}
