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

// Literata is a variable font: each weight the design uses (400 for text, 500 for names and tabs, 600 for what
// identifies the sheet) is the same file pinned to that weight.
@OptIn(ExperimentalTextApi::class)
private fun literataFont(weight: FontWeight) = Font(
    resId = R.font.literata,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private val Literata = FontFamily(
    literataFont(FontWeight.Normal),
    literataFont(FontWeight.Medium),
    literataFont(FontWeight.SemiBold),
)

private fun TextStyle.inLiterata() = copy(fontFamily = Literata)

/** Material 3's scale, all in Literata: the whole app is serif, like a manual. */
val Typography = Typography().run {
    copy(
        displayLarge = displayLarge.inLiterata(),
        displayMedium = displayMedium.inLiterata(),
        displaySmall = displaySmall.inLiterata(),
        headlineLarge = headlineLarge.inLiterata(),
        headlineMedium = headlineMedium.inLiterata(),
        headlineSmall = headlineSmall.inLiterata(),
        titleLarge = titleLarge.inLiterata(),
        titleMedium = titleMedium.inLiterata(),
        titleSmall = titleSmall.inLiterata(),
        bodyLarge = bodyLarge.inLiterata(),
        bodyMedium = bodyMedium.inLiterata(),
        bodySmall = bodySmall.inLiterata(),
        labelLarge = labelLarge.inLiterata(),
        labelMedium = labelMedium.inLiterata(),
        labelSmall = labelSmall.inLiterata(),
    )
}

private fun literata(size: Int, lineHeight: Int, letterSpacing: Double = 0.0) = TextStyle(
    fontFamily = Literata,
    fontWeight = FontWeight.SemiBold,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

/**
 * The sheet's own styles, in Literata 600: name, numbers and section titles. The same in every theme.
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

    /** The amount typed in the HP sheet's keypad. */
    val hitPointsAmount = literata(size = 40, lineHeight = 48)

    /** HP in the header: Literata 500, a bit bigger than bodyLarge. */
    val hpValue = TextStyle(fontFamily = Literata, fontWeight = FontWeight.Medium, fontSize = 17.sp, lineHeight = 24.sp)
}
