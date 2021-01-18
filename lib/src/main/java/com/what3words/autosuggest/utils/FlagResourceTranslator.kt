package com.what3words.autosuggest.utils

internal interface FlagResourceTranslator {
    fun translate(countryCode: String): Int
}