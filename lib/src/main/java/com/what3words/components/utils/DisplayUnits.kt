package com.what3words.components.utils

@Deprecated("", replaceWith = ReplaceWith("com.what3words.components.utils.DisplayUnits"))
enum class DisplayUnits {
    SYSTEM, IMPERIAL, METRIC
}

fun DisplayUnits.backwardCompatible(): com.what3words.components.models.DisplayUnits {
    return when (this) {
        DisplayUnits.SYSTEM -> com.what3words.components.models.DisplayUnits.SYSTEM
        DisplayUnits.IMPERIAL -> com.what3words.components.models.DisplayUnits.IMPERIAL
        DisplayUnits.METRIC -> com.what3words.components.models.DisplayUnits.METRIC
    }
}
