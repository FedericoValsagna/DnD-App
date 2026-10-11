package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.DeathSaves
import com.valsagnapps.dndapp.domain.HitPointsAction
import com.valsagnapps.dndapp.domain.HitPointsPreview
import com.valsagnapps.dndapp.domain.LifeStatus
import com.valsagnapps.dndapp.domain.previewHitPoints

/**
 * Combat on the characters of [characters], for ViewModel tests. The rules are simplified (from the HP
 * preview): the real ones are the server's. It fails and waits as [characters] does ([failWith], [gate]).
 */
class FakeCombatRepository(private val characters: FakeCharacterRepository) : CombatRepository {
    /** Every call, as a readable line: "damage 1 8 critical=false". */
    val calls = mutableListOf<String>()

    override suspend fun takeDamage(id: String, amount: Int, critical: Boolean) =
        respond(id, "damage $id $amount critical=$critical") {
            it.withPreview(it.previewHitPoints(HitPointsAction.DAMAGE, amount, critical))
        }

    override suspend fun heal(id: String, amount: Int) = respond(id, "heal $id $amount") {
        it.withPreview(it.previewHitPoints(HitPointsAction.HEALING, amount))
            .copy(lifeStatus = LifeStatus.CONSCIOUS, deathSaves = DeathSaves())
    }

    override suspend fun setTemporaryHitPoints(id: String, temporaryHitPoints: Int) =
        respond(id, "temporary $id $temporaryHitPoints") { it.copy(temporaryHitPoints = temporaryHitPoints) }

    override suspend fun updateDeathSaves(id: String, deathSaves: DeathSaves) =
        respond(id, "death saves $id ${deathSaves.successes}/${deathSaves.failures}") {
            it.copy(deathSaves = deathSaves, lifeStatus = deathSaves.status())
        }

    override suspend fun revive(id: String, hitPoints: Int) = respond(id, "revive $id $hitPoints") {
        it.copy(
            currentHitPoints = hitPoints,
            temporaryHitPoints = 0,
            lifeStatus = LifeStatus.CONSCIOUS,
            deathSaves = DeathSaves(),
        )
    }

    override suspend fun updateArmorClassOverride(id: String, armorClassOverride: Int?) =
        respond(id, "armor class $id $armorClassOverride") {
            it.copy(armorClassOverride = armorClassOverride, armorClass = armorClassOverride ?: CALCULATED_AC)
        }

    override suspend fun updateBaseSpeed(id: String, baseSpeed: Int) =
        respond(id, "speed $id $baseSpeed") { it.copy(baseSpeed = baseSpeed, speed = baseSpeed) }

    private suspend fun respond(
        id: String,
        call: String,
        change: (Character) -> Character,
    ): RepositoryResult<Character> {
        characters.gate?.await()
        characters.failWith?.let { return RepositoryResult.Failure(it) }
        calls += call
        val index = characters.characters.indexOfFirst { it.id == id }
        return RepositoryResult.Success(change(characters.characters[index]).also { characters.characters[index] = it })
    }

    companion object {
        /** The AC the fake gives back when the override is removed. */
        const val CALCULATED_AC = 11
    }
}

private fun Character.withPreview(preview: HitPointsPreview?): Character = when (preview) {
    is HitPointsPreview.Points -> copy(
        currentHitPoints = preview.current,
        temporaryHitPoints = preview.temporary,
        lifeStatus = if (preview.current > 0) LifeStatus.CONSCIOUS else LifeStatus.DYING,
    )
    is HitPointsPreview.Failures -> deathSaves.copy(failures = deathSaves.failures + preview.count)
        .let { copy(deathSaves = it, lifeStatus = it.status()) }
    HitPointsPreview.Dead -> copy(
        currentHitPoints = 0,
        lifeStatus = LifeStatus.DEAD,
        deathSaves = DeathSaves(failures = DeathSaves.MAX),
    )
    null -> this
}

private fun DeathSaves.status() = when {
    failures == DeathSaves.MAX -> LifeStatus.DEAD
    successes == DeathSaves.MAX -> LifeStatus.STABLE
    else -> LifeStatus.DYING
}
