package com.valsagnapps.dndapp.domain

/**
 * A row of Combate: one feature, or every feature that shares a resource (Channel Divinity's options, Bardic
 * Inspiration and Cutting Words, the Ki actions), titled with the resource. [resource] is null if it has no uses
 * to track.
 */
data class CombatAction(val name: String, val features: List<ClassFeature>, val resource: Resource?) {
    val isGroup: Boolean get() = features.size > 1

    /** The type most of its features have (ties go in [ActionType]'s order); null if none is an action. */
    val actionType: ActionType?
        get() = features.mapNotNull { it.actionType }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<ActionType, Int>> { it.value }.thenBy { it.key })
            .firstOrNull()
            ?.key
}

/**
 * What Combate lists, grouped by [ActionType] in its order (types without actions are left out), and [others]:
 * resources no action spends (Action Surge, Arcane Recovery). Passive features don't go.
 */
data class CombatActionList(val byType: Map<ActionType, List<CombatAction>>, val others: List<CombatAction>) {
    val isEmpty: Boolean get() = byType.isEmpty() && others.isEmpty()
}

/**
 * The rows of Combate, in the order of the classes and their features. The features that share a tracked
 * resource go together in one row, where the first of them is.
 */
fun Character.combatActionList(): CombatActionList {
    val all = features.values.flatten()
    val tracked = resources.associateBy { it.id }
    val shared = all.groupBy { it.resource }.filterKeys { it in tracked }.filterValues { it.size > 1 }
    val rows = all.mapNotNull { feature ->
        val group = shared[feature.resource]
        val resource = feature.resource?.let { tracked[it] }
        when {
            group != null -> CombatAction(checkNotNull(resource).name, group, resource)
            feature.actionType != null || resource != null -> CombatAction(feature.name, listOf(feature), resource)
            else -> null
        }
    }.distinct()
    return CombatActionList(
        byType = ActionType.entries
            .associateWith { type -> rows.filter { it.actionType == type } }
            .filterValues { it.isNotEmpty() },
        others = rows.filter { it.actionType == null },
    )
}
