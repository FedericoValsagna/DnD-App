package com.valsagnapps.dndapp.ui.charactersheet

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.valsagnapps.dndapp.R
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.CharacterClass
import com.valsagnapps.dndapp.domain.ClassFeature
import com.valsagnapps.dndapp.domain.ClassLevel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeaturesSectionTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private val channelDivinity = ClassFeature(
        id = "CLERIC_CHANNEL_DIVINITY",
        name = "Channel Divinity",
        level = 2,
        summary = "Once per short or long rest.",
        srdText = "At 2nd level, you gain the ability to channel divine energy.",
    )

    private val dampenElements = ClassFeature(
        id = "NATURE_DAMPEN_ELEMENTS",
        name = "Dampen Elements",
        level = 6,
        summary = "Reaction: grant resistance.",
    )

    private val fjor = Character(
        id = "1",
        name = "Fjör",
        level = 6,
        proficiencyBonus = 3,
        abilities = emptyMap(),
        classes = listOf(ClassLevel(CharacterClass.CLERIC, 6)),
        features = mapOf(CharacterClass.CLERIC to listOf(channelDivinity, dampenElements)),
    )

    private fun show(character: Character) {
        composeRule.setContent {
            CharacterSheetContent(CharacterSheetUiState.Content(character), {}, { _, _ -> }, {})
        }
        composeRule.onNodeWithText(context.getString(R.string.features)).performScrollTo().performClick()
    }

    @Test
    fun `shows each feature with its level and summary`() {
        show(fjor)

        composeRule.onNodeWithText("Channel Divinity").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.level_value, 2)).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Once per short or long rest.").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Dampen Elements").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Reaction: grant resistance.").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `shows the SRD text only when asked and only for SRD features`() {
        show(fjor)

        composeRule.onAllNodesWithText(channelDivinity.srdText!!).assertCountEquals(0)
        composeRule.onAllNodesWithText(context.getString(R.string.show_srd_text)).assertCountEquals(1)

        composeRule.onNodeWithText(context.getString(R.string.show_srd_text)).performScrollTo().performClick()

        composeRule.onNodeWithText(channelDivinity.srdText!!).performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.hide_srd_text)).performScrollTo().performClick()
        composeRule.onAllNodesWithText(channelDivinity.srdText!!).assertCountEquals(0)
    }

    @Test
    fun `says when a class has no features loaded`() {
        show(fjor.copy(features = emptyMap()))

        composeRule.onNodeWithText(context.getString(R.string.no_features)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `with several classes groups the features under each class`() {
        show(
            fjor.copy(
                classes = listOf(ClassLevel(CharacterClass.CLERIC, 6), ClassLevel(CharacterClass.FIGHTER, 1)),
                features = mapOf(
                    CharacterClass.CLERIC to listOf(channelDivinity),
                    CharacterClass.FIGHTER to emptyList(),
                ),
            ),
        )

        composeRule.onNodeWithText("Cleric").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("Fighter").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.no_features)).performScrollTo().assertIsDisplayed()
    }
}
