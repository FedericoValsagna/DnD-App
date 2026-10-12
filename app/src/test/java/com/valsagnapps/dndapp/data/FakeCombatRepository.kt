package com.valsagnapps.dndapp.data

import com.valsagnapps.dndapp.domain.Ability
import com.valsagnapps.dndapp.domain.Character
import com.valsagnapps.dndapp.domain.DeathSaves
import com.valsagnapps.dndapp.domain.HitPointsAction
import com.valsagnapps.dndapp.domain.HitPointsPreview
import com.valsagnapps.dndapp.domain.LifeStatus
import com.valsagnapps.dndapp.domain.Recharge
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

    override suspend fun updateResourceUsed(id: String, resource: String, used: Int) =
        respond(id, "resource $id $resource $used") { character ->
            character.copy(resources = character.resources.map { if (it.id == resource) it.copy(used = used) else it })
        }

    /** Heals the roll plus the CON per die, spends the dice and recovers the short rest resources. */
    override suspend fun shortRest(id: String, hitDice: Map<Int, Int>, roll: Int) =
        respond(id, "short rest $id ${hitDice.entries.joinToString { "${it.value}d${it.key}" }} $roll") { character ->
            val con = character.abilities[Ability.CONSTITUTION]?.modifier ?: 0
            val healing = maxOf(0, roll + hitDice.values.sum() * con)
            character.copy(
                currentHitPoints = minOf(character.maxHitPoints ?: 0, (character.currentHitPoints ?: 0) + healing),
                hitDice = character.hitDice.map { it.copy(used = it.used + (hitDice[it.die] ?: 0)) },
                resources = character.resources.map {
                    if (it.recharge == Recharge.SHORT_REST) it.copy(used = 0) else it
                },
            )
        }

    /** Recovers everything, hit dice included (the real rule is half of them). */
    override suspend fun longRest(id: String) = respond(id, "long rest $id") { character ->
        character.copy(
            currentHitPoints = character.maxHitPoints,
            temporaryHitPoints = 0,
            hitDice = character.hitDice.map { it.copy(used = 0) },
            resources = character.resources.map { it.copy(used = 0) },
        )
    }

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
