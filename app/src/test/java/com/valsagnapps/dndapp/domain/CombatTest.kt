package com.valsagnapps.dndapp.domain

import com.valsagnapps.dndapp.data.sampleCharacter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CombatTest {

    private fun character(current: Int, temporary: Int = 0, failures: Int = 0) = sampleCharacter().copy(
        maxHitPoints = 44,
        currentHitPoints = current,
        temporaryHitPoints = temporary,
        deathSaves = DeathSaves(failures = failures),
    )

    @Test
    fun `temporary hit points absorb damage first`() {
        assertEquals(
            HitPointsPreview.Points(current = 9, temporary = 0),
            character(current = 12, temporary = 5).previewHitPoints(HitPointsAction.DAMAGE, 8),
        )
    }

    @Test
    fun `damage below the temporary hit points leaves current untouched`() {
        assertEquals(
            HitPointsPreview.Points(current = 12, temporary = 2),
            character(current = 12, temporary = 5).previewHitPoints(HitPointsAction.DAMAGE, 3),
        )
    }

    @Test
    fun `damage stops at 0`() {
        assertEquals(
            HitPointsPreview.Points(current = 0, temporary = 0),
            character(current = 12).previewHitPoints(HitPointsAction.DAMAGE, 20),
        )
    }

    @Test
    fun `damage left over after 0 that reaches the max kills`() {
        assertEquals(HitPointsPreview.Dead, character(current = 12).previewHitPoints(HitPointsAction.DAMAGE, 56))
    }

    @Test
    fun `damage at 0 adds one failure, or two if critical`() {
        assertEquals(
            HitPointsPreview.Failures(1),
            character(current = 0).previewHitPoints(HitPointsAction.DAMAGE, 6),
        )
        assertEquals(
            HitPointsPreview.Failures(2),
            character(current = 0).previewHitPoints(HitPointsAction.DAMAGE, 6, critical = true),
        )
    }

    @Test
    fun `damage at 0 that reaches three failures kills`() {
        assertEquals(
            HitPointsPreview.Dead,
            character(current = 0, failures = 1).previewHitPoints(HitPointsAction.DAMAGE, 6, critical = true),
        )
    }

    @Test
    fun `healing stops at the max`() {
        assertEquals(
            HitPointsPreview.Points(current = 44, temporary = 5),
            character(current = 40, temporary = 5).previewHitPoints(HitPointsAction.HEALING, 10),
        )
    }

    @Test
    fun `new temporary hit points replace the old ones`() {
        assertEquals(
            HitPointsPreview.Points(current = 12, temporary = 3),
            character(current = 12, temporary = 8).previewHitPoints(HitPointsAction.TEMPORARY, 3),
        )
    }

    @Test
    fun `there is no preview without the current hit points`() {
        assertNull(sampleCharacter().previewHitPoints(HitPointsAction.DAMAGE, 3))
    }
}
