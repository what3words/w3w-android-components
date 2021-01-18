package com.what3words.autosuggest.text

import android.Manifest
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.widget.AppCompatEditText
import com.intentfilter.androidpermissions.PermissionManager
import com.intentfilter.androidpermissions.models.DeniedPermissions
import com.what3words.androidwrapper.voice.VoiceBuilder
import com.what3words.autosuggest.error.showError
import com.what3words.autosuggest.text.W3WAutoSuggestEditText.Companion.regex
import com.what3words.autosuggest.utils.W3WSuggestion
import com.what3words.javawrapper.response.APIResponse
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

        populateQueryOptions(
            queryMap,
            "text",
            null,
            focus,
            language,
            nResults,
            nFocusResults,
            clipToCountry,
            clipToCircle,
            clipToCircleRadius,
            clipToBoundingBox,
            clipToPolygon
        )

        val res =
            wrapper!!.autosuggest(searchFor).apply {
                this@handleAutoSuggest.focus?.let {
                    this.focus(it)
                }
                this@handleAutoSuggest.language?.let {
                    this.language(it)
                }
                this@handleAutoSuggest.nResults?.let {
                    this.nResults(it)
                }
                this@handleAutoSuggest.nFocusResults?.let {
                    this.nFocusResults(it)
                }
                this@handleAutoSuggest.clipToCountry?.let {
                    this.clipToCountry(*it)
                }
                this@handleAutoSuggest.clipToCircle?.let {
                    this.clipToCircle(it, clipToCircleRadius ?: 0.0)
                }
                this@handleAutoSuggest.clipToBoundingBox?.let {
                    this.clipToBoundingBox(it)
                }
                this@handleAutoSuggest.clipToPolygon?.let { coordinates ->
                    this.clipToPolygon(*coordinates)
                }
            }.execute()
        if (!res.isSuccessful) {
            CoroutineScope(Dispatchers.Main).launch {
                getErrorView().showError(errorMessageText)
            }
            errorCallback?.accept(res.error) ?: run {
                Log.e("W3WAutoSuggestEditText", res.error.message)
            }
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                if (res != null && res.suggestions != null && hasFocus()) {
                    lastSuggestions.apply {
                        clear()
                        addAll(res.suggestions)
                    }
                    getPicker().visibility =
                        if (res.suggestions.isEmpty()) GONE else VISIBLE
                    getPicker().refreshSuggestions(
                        res.suggestions,
                        searchFor,
                        queryMap,
                        returnCoordinates
                    )
                }
            }
        }
    }
}

internal fun W3WAutoSuggestEditText.handleAddressPicked(
    suggestion: W3WSuggestion?
) {
    if (getPicker().visibility == VISIBLE && suggestion == null) {
        getInvalidAddressView().showError(invalidSelectionMessageText)
    }
    showImages(suggestion != null)
    getPicker().refreshSuggestions(emptyList(), null, emptyMap(), returnCoordinates)
    getPicker().visibility = GONE
    clearFocus()
    setText(suggestion?.suggestion?.words)
    callback?.accept(suggestion)
}

internal fun W3WAutoSuggestEditText.handleAddressAutoPicked(suggestion: Suggestion?) {
    if (getPicker().visibility == VISIBLE && suggestion == null) {
        getInvalidAddressView().showError(invalidSelectionMessageText)
    }
    showImages(suggestion != null)
    getPicker().refreshSuggestions(emptyList(), null, emptyMap(), returnCoordinates)
    getPicker().visibility = GONE
    clearFocus()
    val originalQuery = text.toString()
    setText(suggestion?.words)

    if (suggestion == null) callback?.accept(null)
    else {
        if (!isEnterprise) handleSelectionTrack(suggestion, originalQuery, queryMap, key!!)
        if (!returnCoordinates) callback?.accept(W3WSuggestion(suggestion))
        else {
            CoroutineScope(Dispatchers.IO).launch {
                val res = wrapper!!.convertToCoordinates(suggestion.words).execute()
                CoroutineScope(Dispatchers.Main).launch {
                    callback?.accept(W3WSuggestion(suggestion, res.coordinates))
                }
            }
        }
    }
}


internal fun W3WAutoSuggestEditText.handleVoice() {
    if (builder?.isListening() == true) {
        builder?.stopListening()
        inlineVoicePulseLayout.setIsVoiceRunning(false)
        return
    }

    populateQueryOptions(
        queryMap,
        "voice",
        voiceLanguage,
        focus,
        language,
        nResults,
        nFocusResults,
        clipToCountry,
        clipToCircle,
        clipToCircleRadius,
        clipToBoundingBox,
        clipToPolygon
    )

    val permissionManager: PermissionManager = PermissionManager.getInstance(context)
    permissionManager.checkPermissions(
        Collections.singleton(Manifest.permission.RECORD_AUDIO),
        object : PermissionManager.PermissionRequestListener {
            override fun onPermissionGranted() {
                getPicker().refreshSuggestions(
                    emptyList(),
                    "",
                    emptyMap(),
                    returnCoordinates
                )
                getPicker().visibility = GONE
                val microphone = VoiceBuilder.Microphone()
                builder = wrapper!!.autosuggest(microphone, voiceLanguage).apply {
                    nResults?.let {
                        this.nResults(it)
                    }
                    focus?.let {
                        this.focus(it)
                    }
                    nFocusResults?.let {
                        this.nFocusResults(it)
                    }
                    clipToCountry?.let {
                        this.clipToCountry(it.toList())
                    }
                    clipToCircle?.let {
                        this.clipToCircle(it, clipToCircleRadius ?: 0.0)
                    }
                    clipToBoundingBox?.let {
                        this.clipToBoundingBox(it)
                    }
                    clipToPolygon?.let { coordinates ->
                        this.clipToPolygon(coordinates.toList())
                    }
                    val textPlaceholder = this@handleVoice.hint
                    this.onSuggestions { suggestions ->
                        this@handleVoice.hint = textPlaceholder
                        if (suggestions.isEmpty()) {
                            getInvalidAddressView().showError(invalidSelectionMessageText)
                        } else {
                            pickedFromVoice = true
                            this@handleVoice.setText(suggestions.minByOrNull { it.rank }!!.words)
                            getPicker().visibility = AppCompatEditText.VISIBLE
                            getPicker().refreshSuggestions(
                                suggestions,
                                suggestions.minByOrNull { it.rank }!!.words,
                                queryMap,
                                returnCoordinates
                            )
                            showKeyboard()
                        }
                        if (voiceFullscreen) {
                            voicePulseLayout?.setIsVoiceRunning(false, shouldAnimate = true)
                        } else inlineVoicePulseLayout.setIsVoiceRunning(false)
                    }
                    this.onError {
                        this@handleVoice.hint = textPlaceholder
                        getErrorView().showError(errorMessageText)
                        errorCallback?.accept(it) ?: run {
                            Log.e("W3WAutoSuggestEditText", it.message)
                        }
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
                getErrorView().showError(errorMessageText)
                errorCallback?.accept(APIResponse.What3WordsError.UNKNOWN_ERROR.apply {
                    message = "Microphone permission required"
                })
            }
        })
}