package com.what3words.components.utils

import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates

@Deprecated("", ReplaceWith("com.what3words.javawrapper.response.SuggestionWithCoordinates"))
data class W3WSuggestion(
    val suggestion: Suggestion,
    val coordinates: com.what3words.javawrapper.response.Coordinates? = null
)

@Deprecated("to delete when remove W3WSuggestion completely")
internal fun SuggestionWithCoordinates.backwardCompatible(): W3WSuggestion {
    val suggestion = Suggestion(this.words, this.nearestPlace, this.country, this.distanceToFocusKm, this.rank, this.language)
    return W3WSuggestion(suggestion, this.coordinates)
}
