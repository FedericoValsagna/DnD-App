package com.valsagnapps.dndapp.domain

/**
 * A class or subclass feature, gained at [level] of its class. The sheet shows [summary]; [srdText] is the full SRD
 * text, null if the feature isn't SRD. [id] is stable.
 */
data class ClassFeature(
    val id: String,
    val name: String,
    val level: Int,
    val summary: String,
    val srdText: String? = null,
)
