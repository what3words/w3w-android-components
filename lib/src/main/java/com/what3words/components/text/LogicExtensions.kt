package com.what3words.components.text

import android.Manifest
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.widget.AppCompatEditText
import com.intentfilter.androidpermissions.PermissionManager
import com.intentfilter.androidpermissions.models.DeniedPermissions
import com.what3words.androidwrapper.voice.Microphone
import com.what3words.components.R
import com.what3words.components.error.showError
import com.what3words.components.text.W3WAutoSuggestEditText.Companion.dym_regex
import com.what3words.components.text.W3WAutoSuggestEditText.Companion.regex
import com.what3words.javawrapper.request.SourceApi
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.Suggestion
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import java.util.regex.Pattern

internal fun isValid3wa(query: String): Boolean {
    Pattern.compile(regex).also {
        return it.matcher(query).find()
    }
}

internal fun W3WAutoSuggestEditText.isPossible3wa(query: String): Boolean {
    val queryFormatted = query.replace(context.getString(R.string.w3w_slash), "").lowercase(
        Locale.getDefault()
    )
    Pattern.compile(dym_regex).also {
        return it.matcher(queryFormatted).find()
    }
}

internal fun W3WAutoSuggestEditText.getPossible3wa(query: String): String {
    return query.replace(context.getString(R.string.w3w_slash), "").lowercase(
        Locale.getDefault()
    )
}

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

internal fun W3WAutoSuggestEditText.handleAutoSuggest(
    searchText: String,
    searchFor: String,
    isDYM: Boolean = false
) {
    CoroutineScope(Dispatchers.IO).launch {
        delay(W3WAutoSuggestEditText.DEBOUNCE_MS)  //debounce timeOut
        if (searchText != searchFor)
            return@launch

        options = populateQueryOptions(
            SourceApi.TEXT,
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
            wrapper!!.autosuggest(searchFor.replace("/", "")).apply {
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
                    if (isDYM) {
                        res.suggestions.firstOrNull { it.words == searchText }?.let {
                            getCorrectionPicker().setSuggestion(it)
                            getCorrectionPicker().visibility = VISIBLE
                        } ?: run {
                            getCorrectionPicker().visibility = GONE
                        }
                    } else {
                        getPicker().visibility =
                            if (res.suggestions.isEmpty()) GONE else VISIBLE
                        getPicker().refreshSuggestions(
                            res.suggestions,
                            searchFor,
                            options,
                            returnCoordinates
                        )
                    }
                }
            }
        }
    }
}

internal fun W3WAutoSuggestEditText.handleAddressPicked(
    suggestion: SuggestionWithCoordinates?
) {
    if (getPicker().visibility == VISIBLE && suggestion == null) {
        getInvalidAddressView().showError(invalidSelectionMessageText)
    }
    showImages(suggestion != null)
    getPicker().refreshSuggestions(emptyList(), null, AutoSuggestOptions(), returnCoordinates)
    getPicker().visibility = GONE
    getCorrectionPicker().setSuggestion(null)
    getCorrectionPicker().visibility = GONE
    clearFocus()
    if (suggestion != null) {
        setText(context.getString(R.string.w3w_slashes_with_address, suggestion.words))
    } else {
        text = null
    }

    callback?.accept(suggestion)
}

internal fun W3WAutoSuggestEditText.handleAddressAutoPicked(suggestion: Suggestion?) {
    if (suggestion == null && !allowInvalid3wa) {
        getInvalidAddressView().showError(invalidSelectionMessageText)
    }
    showImages(suggestion != null)
    getPicker().refreshSuggestions(emptyList(), null, AutoSuggestOptions(), returnCoordinates)
    getPicker().visibility = GONE
    getCorrectionPicker().setSuggestion(null)
    getCorrectionPicker().visibility = GONE
    clearFocus()
    val originalQuery = text.toString()
    if (suggestion != null) {
        setText(context.getString(R.string.w3w_slashes_with_address, suggestion.words))
    } else {
        if (!allowInvalid3wa) {
            text = null
        }
    }
    if (suggestion == null) callback?.accept(null)
    else {
        if (!isEnterprise && wrapper != null) handleSelectionTrack(
            suggestion,
            originalQuery,
            options,
            wrapper!!
        )
        if (!returnCoordinates) callback?.accept(SuggestionWithCoordinates(suggestion))
        else {
            CoroutineScope(Dispatchers.IO).launch {
                val res = wrapper!!.convertToCoordinates(suggestion.words).execute()
                CoroutineScope(Dispatchers.Main).launch {
                    callback?.accept(SuggestionWithCoordinates(suggestion, res.coordinates))
                }
            }
        }
    }
}


internal fun W3WAutoSuggestEditText.handleVoice() {
    if (builder?.isListening() == true) {
        builder?.stopListening()
        inlineVoicePulseLayout?.setIsVoiceRunning(false)
        return
    }

    options = populateQueryOptions(
        SourceApi.VOICE,
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
                    AutoSuggestOptions(),
                    returnCoordinates
                )
                getPicker().visibility = GONE
                val microphone = Microphone()
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
                            this@handleVoice.setText(
                                context.getString(
                                    R.string.w3w_slashes_with_address,
                                    suggestions.minByOrNull { it.rank }!!.words
                                )
                            )
                            getPicker().visibility = AppCompatEditText.VISIBLE
                            //Query empty because we don't want to highlight when using voice.
                            getPicker().refreshSuggestions(
                                suggestions,
                                "",
                                options,
                                returnCoordinates
                            )
                            showKeyboard()
                        }
                        if (voiceFullscreen) {
                            voicePulseLayout?.setIsVoiceRunning(false, shouldAnimate = true)
                        } else inlineVoicePulseLayout?.setIsVoiceRunning(false)
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
                        else inlineVoicePulseLayout?.setIsVoiceRunning(false)
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
                    inlineVoicePulseLayout?.setup(builder!!, microphone)
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