package com.what3words.components.text

import com.what3words.components.R
import com.what3words.javawrapper.response.Suggestion
import java.util.Locale

internal fun W3WAutoSuggestEditText.isReal3wa(query: String): Boolean {
    val queryFormatted = query.replace(context.getString(R.string.w3w_slash), "").lowercase(
        Locale.getDefault()
    )
    return lastSuggestions.any { it.words == queryFormatted }
}

internal fun W3WAutoSuggestEditText.getReal3wa(query: String): Suggestion? {
    val queryFormatted = query.replace(context.getString(R.string.w3w_slash), "").lowercase(
        Locale.getDefault()
    )
    return lastSuggestions.firstOrNull { it.words == queryFormatted }
}


internal fun String?.shouldShowClear(): Boolean {
    if (this.isNullOrEmpty()) return false
    if (this == "/") return false
    if (this == "//") return false
    if (this == "///") return false
    return true
}
