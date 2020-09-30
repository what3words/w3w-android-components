package com.what3words.autosuggest.util

internal interface FlagResourceTranslator {
    fun translate(countryCode: String): Int
}