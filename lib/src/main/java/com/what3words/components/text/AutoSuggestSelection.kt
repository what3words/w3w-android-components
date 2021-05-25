package com.what3words.components.text

import com.what3words.androidwrapper.What3WordsV3
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.request.SourceApi
import com.what3words.javawrapper.response.Suggestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AutoSuggestOptions {
    var voiceLanguage: String? = null
    var focus: Coordinates? = null
    var source: SourceApi? = null
    var language: String? = null
    var nResults: Int? = null
    var nFocusResults: Int? = null
    var clipToCountry: Array<String>? = null
    var clipToCircle: Coordinates? = null
    var clipToCircleRadius: Double? = null
    var clipToBoundingBox: BoundingBox? = null
    var clipToPolygon: Array<Coordinates>? = null
}

internal fun populateQueryOptions(
    sourceApi: SourceApi,
    voiceLanguage: String?,
    focus: Coordinates?,
    language: String?,
    nResults: Int?,
    nFocusResults: Int?,
    clipToCountry: Array<String>?,
    clipToCircle: Coordinates?,
    clipToCircleRadius: Double?,
    clipToBoundingBox: BoundingBox?,
    clipToPolygon: Array<Coordinates>?
): AutoSuggestOptions {
    val options = AutoSuggestOptions()
    options.source = sourceApi
    options.voiceLanguage = voiceLanguage
    options.focus = focus
    options.language = language
    options.nResults = nResults
    options.nFocusResults = nFocusResults
    options.clipToCountry = clipToCountry
    options.clipToCircle = clipToCircle
    options.clipToCircleRadius = clipToCircleRadius
    options.clipToBoundingBox = clipToBoundingBox
    options.clipToPolygon = clipToPolygon
    return options
}

internal fun handleSelectionTrack(
    suggestion: Suggestion,
    input: String,
    options: AutoSuggestOptions,
    wrapper: What3WordsV3
) {
    CoroutineScope(Dispatchers.IO).launch {
        wrapper.autosuggestionSelection(input, suggestion.words, suggestion.rank, options.source).apply {
            options.focus?.let {
                this.focus(it)
            }
            options.language?.let {
                this.language(it)
            }
            options.nResults?.let {
                this.nResults(it)
            }
            options.nFocusResults?.let {
                this.nFocusResults(it)
            }
            options.clipToCountry?.let {
                this.clipToCountry(*it)
            }
            options.clipToCircle?.let {
                this.clipToCircle(it, options.clipToCircleRadius ?: 0.0)
            }
            options.clipToBoundingBox?.let {
                this.clipToBoundingBox(it)
            }
            options.clipToPolygon?.let { coordinates ->
                this.clipToPolygon(*coordinates)
            }
        }.execute()
    }
}
