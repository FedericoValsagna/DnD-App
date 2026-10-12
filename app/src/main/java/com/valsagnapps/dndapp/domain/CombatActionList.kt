package com.valsagnapps.dndapp.domain

/**
 * What Combate lists: the features that are actions, grouped by [ActionType] in its order (types without
 * features are left out), and [others]: for each resource that no action spends (Action Surge, Arcane
 * Recovery), the feature that gives it. Passive features don't go.
 */
data class CombatActionList(val byType: Map<ActionType, List<ClassFeature>>, val others: List<ClassFeature>) {
    val isEmpty: Boolean get() = byType.isEmpty() && others.isEmpty()
}

fun Character.combatActionList(): CombatActionList {
    val all = features.values.flatten()
    val actions = all.filter { it.actionType != null }
    val spentByActions = actions.mapNotNull { it.resource }.toSet()
    val tracked = resources.map { it.id }.toSet()
    return CombatActionList(
        byType = ActionType.entries
            .associateWith { type -> actions.filter { it.actionType == type } }
            .filterValues { it.isNotEmpty() },
        others = all
            .filter { it.actionType == null && it.resource in tracked && it.resource !in spentByActions }
            .distinctBy { it.resource },
    )
}

/** The resource [feature] spends or gives, if the character tracks it (one without limit isn't). */
fun Character.resourceOf(feature: ClassFeature): Resource? =
    feature.resource?.let { id -> resources.find { it.id == id } }
