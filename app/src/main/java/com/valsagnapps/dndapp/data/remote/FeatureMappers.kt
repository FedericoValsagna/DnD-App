package com.valsagnapps.dndapp.data.remote

import com.valsagnapps.dndapp.domain.ActionType
import com.valsagnapps.dndapp.domain.ClassFeature
import com.valsagnapps.dndapp.domain.Recharge
import com.valsagnapps.dndapp.domain.Resource
import com.valsagnapps.dndapp.domain.UsesDisplay

// Features and resources of the character; values the app doesn't know are left out.

internal fun ClassFeatureDto.toDomain() = ClassFeature(
    id = id,
    name = name,
    level = level,
    summary = summary,
    srdText = srdText,
    actionType = actionType?.let { enumValueOrNull<ActionType>(it) },
    resource = resource,
)

// A display the app doesn't know is shown as points.
internal fun ResourceDto.toDomain() = Resource(
    id = id,
    name = name,
    max = max,
    used = used,
    recharge = recharge?.let { enumValueOrNull<Recharge>(it) },
    display = display?.let { enumValueOrNull<UsesDisplay>(it) } ?: UsesDisplay.PIPS,
)
