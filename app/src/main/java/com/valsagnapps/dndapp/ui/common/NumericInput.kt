package com.valsagnapps.dndapp.ui.common

/** Keeps only digits, at most as many as the largest valid value has (e.g. two for 1..20). */
fun String.toNumericInput(range: IntRange): String = filter(Char::isDigit).take(range.last.toString().length)
