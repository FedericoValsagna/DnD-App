package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.valsagnapps.dndapp.ui.theme.SheetType

/** Title of a group inside a screen, in red small caps. Screens have no title of their own. */
@Composable
internal fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = SheetType.sectionLabel,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

/** A value in a box with a background (a bonus, damage, a pool), so it doesn't read as plain text. */
@Composable
internal fun ValueChip(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = SheetType.chipValue,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

/**
 * A number with its label in a rounded box (Level, PB, Max HP). If [onClick] is not null it can be
 * tapped (to edit the value).
 */
@Composable
internal fun StatCell(label: String, value: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    val content = @Composable {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(value, style = SheetType.statValue, maxLines = 1)
        }
    }
    val shape = RoundedCornerShape(12.dp)
    val color = MaterialTheme.colorScheme.surfaceContainerHighest
    if (onClick != null) {
        Surface(onClick = onClick, modifier = modifier, shape = shape, color = color, content = content)
    } else {
        Surface(modifier = modifier, shape = shape, color = color, content = content)
    }
}

/** A full-width strip at the foot of a screen: a value with its label (Passive Perception). */
@Composable
internal fun StatStrip(label: String, value: String, modifier: Modifier = Modifier) {
    StripBox(MaterialTheme.colorScheme.surfaceContainerHighest, modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SectionLabel(label)
            Text(value, style = SheetType.statValue)
        }
    }
}

/** The box of [StatStrip], also used by the edit mode hint so that it takes the same place. */
@Composable
internal fun StripBox(color: Color, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        content()
    }
}
