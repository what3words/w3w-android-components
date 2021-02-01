package com.what3words.components.utils

import com.what3words.javawrapper.response.Suggestion

data class W3WSuggestion(
    val suggestion: Suggestion,
    val coordinates: com.what3words.javawrapper.response.Coordinates? = null
)