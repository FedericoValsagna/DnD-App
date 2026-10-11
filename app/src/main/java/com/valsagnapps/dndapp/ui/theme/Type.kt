package com.valsagnapps.dndapp.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.valsagnapps.dndapp.R

/** Material 3's scale, in Roboto: everything but what identifies the sheet. */
val Typography = Typography()

// Literata is a variable font: pin it to 600, the only weight the design uses (thinner looks too faint).
@OptIn(ExperimentalTextApi::class)
private val Literata = FontFamily(
    Font(
        resId = R.font.literata,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(FontWeight.SemiBold.weight)),
    ),
)

private fun literata(size: Int, lineHeight: Int, letterSpacing: Double = 0.0) = TextStyle(
    fontFamily = Literata,
    fontWeight = FontWeight.SemiBold,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

/**
 * The sheet's own styles, in Literata: name, numbers and section titles. The same in every theme.
 * Never below 12sp.
 */
object SheetType {
    val characterName = literata(size = 18, lineHeight = 24)
    val statValue = literata(size = 22, lineHeight = 28)
    val saveValue = literata(size = 16, lineHeight = 20)
    val abilityModifier = literata(size = 20, lineHeight = 24)
    val abilityScore = literata(size = 12, lineHeight = 16)

    /** Uppercase is up to the component. */
    val sectionLabel = literata(size = 12, lineHeight = 16, letterSpacing = 1.2)
    val chipValue = literata(size = 14, lineHeight = 20)

    /** HP in the header: Roboto, a bit bigger than bodyLarge. */
    val hpValue = TextStyle(fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 24.sp)
}
