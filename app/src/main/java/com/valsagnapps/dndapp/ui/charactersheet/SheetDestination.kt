package com.valsagnapps.dndapp.ui.charactersheet

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.ShortNavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.valsagnapps.dndapp.R

/**
 * The sheet's screens, in the bottom bar: combat on the left, out of combat on the right. Only those
 * with content for now; Spells and Slots come with their features (five at most).
 */
enum class SheetDestination(@param:StringRes val labelRes: Int, @param:DrawableRes val iconRes: Int) {
    COMBAT(R.string.combat, R.drawable.ic_combat),
    SKILLS(R.string.skills, R.drawable.ic_search),
    CHARACTER(R.string.character, R.drawable.ic_person),
}

@Composable
internal fun SheetBottomNav(
    selected: SheetDestination,
    onSelect: (SheetDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        ShortNavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
            SheetDestination.entries.forEach { destination ->
                ShortNavigationBarItem(
                    selected = destination == selected,
                    onClick = { onSelect(destination) },
                    icon = { Icon(painterResource(destination.iconRes), contentDescription = null) },
                    label = { Text(stringResource(destination.labelRes)) },
                    colors = ShortNavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        selectedIndicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}
