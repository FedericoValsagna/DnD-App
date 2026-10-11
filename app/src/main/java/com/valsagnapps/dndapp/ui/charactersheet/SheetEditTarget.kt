package com.valsagnapps.dndapp.ui.charactersheet

/** What a dialog of the sheet's edit mode opens for. */
enum class SheetEditTarget {
    NAME,
    CLASS,
    MAX_HIT_POINTS,
    ABILITY_SCORES,

    /** Not an edit: the confirmation to delete the character, which goes through the same dialog. */
    DELETE_CHARACTER,
}
