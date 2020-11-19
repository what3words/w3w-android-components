package com.what3words.autosuggest

import android.Manifest
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.widget.AppCompatEditText
import com.intentfilter.androidpermissions.PermissionManager
import com.intentfilter.androidpermissions.models.DeniedPermissions
import com.what3words.androidwrapper.voice.VoiceBuilder
import com.what3words.autosuggest.W3WAutoSuggestEditText.Companion.regex
import com.what3words.javawrapper.response.Suggestion
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.regex.Pattern

internal fun isPossible3wa(query: String): Boolean {
    Pattern.compile(regex).also {
        return it.matcher(query).find()
    }
}

internal fun W3WAutoSuggestEditText.isReal3wa(query: String): Boolean {
    return lastSuggestions.any { it.words == query }
}

internal fun W3WAutoSuggestEditText.handleAutoSuggest(searchText: String, searchFor: String) {
    CoroutineScope(Dispatchers.IO).launch {
        delay(W3WAutoSuggestEditText.DEBOUNCE_MS)  //debounce timeOut
        if (searchText != searchFor)
            return@launch

        if (wrapper == null) throw Exception("Please use apiKey")
        queryMap.clear()
        queryMap["source-api"] = "text"
        val res =
            wrapper!!.autosuggest(searchFor).apply {
                this@handleAutoSuggest.focus?.let {
                    this.focus(it)
                    queryMap["focus"] = it.lat.toString() + "," + it.lng.toString()
                }
                this@handleAutoSuggest.language?.let {
                    this.language(it)
                    queryMap["language"] = it
                }
                this@handleAutoSuggest.nResults?.let {
                    this.nResults(it)
                    queryMap["n-results"] = it.toString()
                }
                this@handleAutoSuggest.nFocusResults?.let {
                    this.nFocusResults(it)
                    queryMap["n-focus-results"] = it.toString()
                }
                this@handleAutoSuggest.clipToCountry?.let {
                    this.clipToCountry(*it)
                    queryMap["clip-to-country"] = it.joinToString(",")
                }
                this@handleAutoSuggest.clipToCircle?.let {
                    this.clipToCircle(it, clipToCircleRadius ?: 0.0)
                    queryMap["clip-to-circle"] =
                        it.lat.toString() + "," + it.lng.toString() + "," + (clipToCircleRadius?.toString()
                            ?: "0")
                }
                this@handleAutoSuggest.clipToBoundingBox?.let {
                    this.clipToBoundingBox(it)
                    queryMap["clip-to-bounding-box"] =
                        it.sw.lat.toString() + "," + it.sw.lng.toString() + "," + it.ne.lat.toString() + "," + it.ne.lng.toString()
                }
                this@handleAutoSuggest.clipToPolygon?.let { coordinates ->
                    this.clipToPolygon(*coordinates)
                    queryMap["clip-to-polygon"] =
                        coordinates.joinToString(",") { "${it.lat},${it.lng}" }
                }
            }.execute()

        CoroutineScope(Dispatchers.Main).launch {
            if (res != null && res.suggestions != null && hasFocus()) {
                lastSuggestions.apply {
                    clear()
                    addAll(res.suggestions)
                }
                recyclerView.visibility =
                    if (res.suggestions.isEmpty()) AppCompatEditText.GONE else VISIBLE
                suggestionsAdapter.refreshSuggestions(res.suggestions, searchFor)
            }
        }
    }
}

internal fun W3WAutoSuggestEditText.handleAddressPicked(suggestion: Suggestion?) {
    if (recyclerView.visibility == VISIBLE && suggestion == null) {
        showErrorMessage()
    } else {
        recyclerView.visibility = AppCompatEditText.GONE
    }
    showImages(suggestion != null)
    suggestionsAdapter.refreshSuggestions(emptyList(), null)
    clearFocus()
    val originalQuery = text.toString()
    setText(suggestion?.words)

    if (suggestion == null) callback?.invoke(null, null, null)
    else {
        if (!isEnterprise) handleSelectionTrack(suggestion, originalQuery, queryMap, key!!)
        if (!returnCoordinates) callback?.invoke(suggestion, null, null)
        else {
            CoroutineScope(Dispatchers.IO).launch {
                val res = wrapper!!.convertToCoordinates(suggestion.words).execute()
                CoroutineScope(Dispatchers.Main).launch {
                    callback?.invoke(
                        suggestion,
                        res.coordinates.lat,
                        res.coordinates.lng
                    )
                }
            }
        }
    }
}

internal fun W3WAutoSuggestEditText.handleVoice() {
    if (wrapper == null) throw Exception("Please use apiKey")
    if (builder?.isListening() == true) {
        builder?.stopListening()
        inlineVoicePulseLayout.setIsVoiceRunning(false)
        return
    }
    queryMap.clear()
    queryMap["n-results"] = nResults.toString()
    queryMap["source-api"] = "voice"
    queryMap["voice-language"] = voiceLanguage
    val permissionManager: PermissionManager = PermissionManager.getInstance(context)
    permissionManager.checkPermissions(
        Collections.singleton(Manifest.permission.RECORD_AUDIO),
        object : PermissionManager.PermissionRequestListener {
            override fun onPermissionGranted() {
                suggestionsAdapter.refreshSuggestions(
                    emptyList(),
                    ""
                )
                recyclerView.visibility = GONE
                val microphone = VoiceBuilder.Microphone()
                builder = wrapper!!.autosuggest(microphone, voiceLanguage).apply {
                    nResults?.let {
                        this.nResults(it)
                        queryMap["n-results"] = it.toString()
                    }
                    focus?.let {
                        this.focus(it)
                        queryMap["focus"] = it.lat.toString() + "," + it.lng.toString()
                    }
                    nFocusResults?.let {
                        this.nFocusResults(it)
                        queryMap["n-focus-results"] = it.toString()
                    }
                    clipToCountry?.let {
                        this.clipToCountry(it.toList())
                        queryMap["clip-to-country"] = it.joinToString(",")
                    }
                    clipToCircle?.let {
                        this.clipToCircle(it, clipToCircleRadius ?: 0.0)
                        queryMap["clip-to-circle"] =
                            it.lat.toString() + "," + it.lng.toString() + "," + (clipToCircleRadius?.toString()
                                ?: "0")
                    }
                    clipToBoundingBox?.let {
                        this.clipToBoundingBox(it)
                        queryMap["clip-to-bounding-box"] =
                            it.sw.lat.toString() + "," + it.sw.lng.toString() + "," + it.ne.lat.toString() + "," + it.ne.lng.toString()
                    }
                    clipToPolygon?.let { coordinates ->
                        this.clipToPolygon(coordinates.toList())
                        queryMap["clip-to-polygon"] =
                            coordinates.joinToString(",") { "${it.lat},${it.lng}" }
                    }
                    val textPlaceholder = this@handleVoice.hint
                    this.onSuggestions { suggestions ->
                        this@handleVoice.hint = textPlaceholder
                        if (suggestions.isEmpty()) {
                            showErrorMessage()
                        } else {
                            pickedFromVoice = true
                            this@handleVoice.setText(suggestions.minByOrNull { it.rank }!!.words)
                            recyclerView.visibility = AppCompatEditText.VISIBLE
                            suggestionsAdapter.refreshSuggestions(
                                suggestions,
                                suggestions.minByOrNull { it.rank }!!.words
                            )
                            showKeyboard()
                        }
                        if (voiceFullscreen) {
                            voicePulseLayout?.setIsVoiceRunning(false, shouldAnimate = true)
                        } else inlineVoicePulseLayout.setIsVoiceRunning(false)
                    }
                    this.onError {
                        this@handleVoice.hint = textPlaceholder
                        showErrorMessage()
                        if (voiceFullscreen) voicePulseLayout?.setIsVoiceRunning(
                            false,
                            shouldAnimate = true
                        )
                        else inlineVoicePulseLayout.setIsVoiceRunning(false)
                    }
                }

                this@handleVoice.setText("")
                hideKeyboard()

                if (voiceFullscreen) {
                    voicePulseLayout?.setup(builder!!, microphone)
                    voicePulseLayout?.onCloseCallback {
                        builder?.stopListening()
                        voicePulseLayout?.setIsVoiceRunning(false, shouldAnimate = true)
                    }
                } else {
                    this@handleVoice.hint = voicePlaceholder
                    inlineVoicePulseLayout.setup(builder!!, microphone)
                }
            }

            override fun onPermissionDenied(deniedPermissions: DeniedPermissions) {
                //TODO
            }
        })
}