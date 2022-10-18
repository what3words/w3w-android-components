package com.what3words.components.compose.wrapper

import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.what3words.components.models.DisplayUnits
import com.what3words.components.models.VoiceScreenType
import com.what3words.components.picker.W3WAutoSuggestCorrectionPicker
import com.what3words.components.picker.W3WAutoSuggestPicker
import com.what3words.components.text.W3WAutoSuggestEditText
import com.what3words.javawrapper.request.AutosuggestInputType
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.request.SourceApi
import com.what3words.javawrapper.response.SuggestionWithCoordinates


/**
 * Creates a [W3WAutoSuggestTextFieldState] that is remembered across compositions.
 *
 * Changes to the provided initial values will **not** result in the state being recreated or
 * changed in any way if it has already been created.
 *
 * @param apiKey your API key from what3words developer dashboard
 * @param voiceEnabledByDefault if voice should be enabled
 * @param voiceScreenType can choose among [VoiceScreenType.Fullscreen], [VoiceScreenType.AnimatedPopup] and [VoiceScreenType.Inline]
 */
@Composable
fun rememberW3WAutoSuggestTextFieldState(
    apiKey: String,
    voiceEnabledByDefault: Boolean = false,
    voiceScreenType: VoiceScreenType = VoiceScreenType.Fullscreen
): W3WAutoSuggestTextFieldState {
    return rememberSaveable(saver = W3WAutoSuggestTextFieldState.Saver) {
        W3WAutoSuggestTextFieldState(
            apiKey = apiKey,
            voiceEnabledByDefault = voiceEnabledByDefault,
            voiceScreenType = voiceScreenType
        )
    }
}

/**
 * A state object that can be used to configure the appearance and functionalities of a [W3WAutoSuggestTextField]
 *
 * @property apiKey your API key from what3words developer dashboard
 * @property voiceEnabledByDefault if voice should be enabled
 * @property voiceScreenType can choose among [VoiceScreenType.Fullscreen], [VoiceScreenType.AnimatedPopup] and [VoiceScreenType.Inline]
 */
class W3WAutoSuggestTextFieldState(
    internal val apiKey: String,
    internal val voiceEnabledByDefault: Boolean = false,
    internal val voiceScreenType: VoiceScreenType = VoiceScreenType.Fullscreen
) {
    // ui variables
    internal var internalW3WAutoSuggestEditText: W3WAutoSuggestEditText? by mutableStateOf(value = null)

    internal var defaultSuggestionPicker: W3WAutoSuggestPicker? by mutableStateOf(value = null)
    internal var defaultErrorView: AppCompatTextView? by mutableStateOf(value = null)
    internal var defaultInvalidAddressMessageView: AppCompatTextView? by mutableStateOf(value = null)
    internal var defaultCorrectionPicker: W3WAutoSuggestCorrectionPicker? by mutableStateOf(value = null)

    // auto suggest properties
    /**
     * see [W3WAutoSuggestEditText.allowFlexibleDelimiters]
     * **/
    var allowFlexibleDelimiters: Boolean by mutableStateOf(value = false)

    /**
     * see [W3WAutoSuggestEditText.allowInvalid3wa]
     * **/
    var allowInvalid3wa: Boolean by mutableStateOf(value = false)

    /**
     * see [W3WAutoSuggestEditText.searchFlowEnabled]
     * **/
    var searchFlowEnabled: Boolean by mutableStateOf(value = false)

    /**
     * see [W3WAutoSuggestEditText.returnCoordinates]
     * **/
    var returnCoordinates: Boolean by mutableStateOf(value = false)

    /**
     * see [W3WAutoSuggestEditText.voiceEnabled]
     * **/
    var voiceEnabled: Boolean by mutableStateOf(value = voiceEnabledByDefault)

    /**
     * see [W3WAutoSuggestEditText.invalidSelectionMessageText]
     * **/
    var invalidSelectionMessage: String? by mutableStateOf(value = null)

    /**
     * see [W3WAutoSuggestEditText.hideSelectedIcon]
     * **/
    var hideSelectedIcon: Boolean by mutableStateOf(value = false)


    /**
     * used to save text state for [W3WAutoSuggestEditText] in the case of a recomposition/configuration change
     * **/
    internal var defaultText: String? = null

    internal var options: AutosuggestOptions? by mutableStateOf(value = null)
    internal var toggleVoice: Boolean by mutableStateOf(value = false)
    internal var errorMessage: String? by mutableStateOf(value = null)
    internal var correctionMessage: String? by mutableStateOf(value = null)
    internal var displayUnit: DisplayUnits? by mutableStateOf(value = null)
    internal var display: SuggestionWithCoordinates? by mutableStateOf(value = null)
    internal var voicePlaceHolder: String? by mutableStateOf(value = null)

    /**
     * see [W3WAutoSuggestEditText.toggleVoice]
     * **/
    fun toggleVoice() {
        this.toggleVoice = true
    }

    /**
     * see [W3WAutoSuggestEditText.correctionMessage]
     * **/
    fun correctionMessage(message: String) {
        this.correctionMessage = message
    }

    /**
     * see [W3WAutoSuggestEditText.displayUnits]
     **/
    fun displayUnit(units: DisplayUnits) {
        this.displayUnit = units
    }

    /**
     * see [W3WAutoSuggestEditText.errorMessage]
     * */
    fun errorMessage(message: String) {
        this.errorMessage = message
    }

    /**
     * see [W3WAutoSuggestEditText.display]
     * **/
    fun display(suggestion: SuggestionWithCoordinates) {
        this.display = suggestion
    }

    /**
     * see [W3WAutoSuggestEditText.options]
     * **/
    fun options(options: AutosuggestOptions) {
        this.options = options
    }

    /**
     * see [W3WAutoSuggestEditText.voicePlaceholder]
     * **/
    fun voicePlaceholder(placeholder: String) {
        this.voicePlaceHolder = placeholder
    }

    internal companion object {
        val Saver: Saver<W3WAutoSuggestTextFieldState, Any> = listSaver(
            save = {
                listOf(
                    it.apiKey,
                    it.voiceEnabledByDefault,
                    it.voiceScreenType,
                    it.allowFlexibleDelimiters,
                    it.allowInvalid3wa,
                    it.searchFlowEnabled,
                    it.returnCoordinates,
                    it.voiceEnabled,
                    it.invalidSelectionMessage,
                    it.hideSelectedIcon,
                    it.toggleVoice,
                    it.errorMessage,
                    it.correctionMessage,
                    it.displayUnit,
                    it.voicePlaceHolder,
                    if (it.internalW3WAutoSuggestEditText != null) it.internalW3WAutoSuggestEditText!!.text.toString() else null,
                    it.options?.language,
                    it.options?.nResults,
                    it.options?.nFocusResults,
                    it.options?.clipToCountry,
                    it.options?.preferLand,
                    it.options?.inputType,
                    it.options?.clipToCircleRadius,
                    it.options?.source,
                    it.options?.clipToCircle?.lat,
                    it.options?.clipToCircle?.lng,
                    it.options?.clipToBoundingBox?.sw?.lat,
                    it.options?.clipToBoundingBox?.sw?.lng,
                    it.options?.clipToBoundingBox?.ne?.lat,
                    it.options?.clipToBoundingBox?.ne?.lng,
                    it.options?.focus?.lat,
                    it.options?.focus?.lng
                )
            },
            restore = { savedList: List<Any?> ->
                W3WAutoSuggestTextFieldState(
                    apiKey = savedList[0] as String,
                    voiceEnabledByDefault = savedList[1] as Boolean,
                    voiceScreenType = savedList[2] as VoiceScreenType
                ).apply {
                    allowFlexibleDelimiters = savedList[3] as Boolean
                    allowInvalid3wa = savedList[4] as Boolean
                    searchFlowEnabled = savedList[5] as Boolean
                    returnCoordinates = savedList[6] as Boolean
                    voiceEnabled = savedList[7] as Boolean
                    invalidSelectionMessage = savedList[8] as String?
                    hideSelectedIcon = savedList[9] as Boolean
                    toggleVoice = savedList[10] as Boolean
                    errorMessage = savedList[11] as String?
                    correctionMessage = savedList[12] as String?
                    displayUnit = savedList[13] as DisplayUnits?
                    voicePlaceHolder = savedList[14] as String?
                    defaultText = savedList[15] as String?

                    val options: AutosuggestOptions = AutosuggestOptions()
                    (savedList[16] as String?)?.let { options.language = it }
                    (savedList[17] as Int?)?.let { options.nResults = it }
                    (savedList[18] as Int?)?.let { options.nFocusResults = it }
                    (savedList[19] as List<String>?)?.let { options.clipToCountry = it }
                    (savedList[20] as Boolean?)?.let { options.preferLand = it }
                    (savedList[21] as AutosuggestInputType?)?.let { options.inputType = it }
                    (savedList[22] as Double?)?.let { options.clipToCircleRadius = it }
                    (savedList[23] as SourceApi?)?.let { options.source = it }
                    if (savedList[24] != null && savedList[25] != null) {
                        options.clipToCircle =
                            Coordinates(savedList[24] as Double, savedList[25] as Double)
                    }
                    if (savedList[25] != null && savedList[26] != null && savedList[27] != null && savedList[28] != null) {
                        options.clipToBoundingBox = BoundingBox(
                            Coordinates(
                                savedList[25] as Double,
                                savedList[26] as Double
                            ), Coordinates(savedList[27] as Double, savedList[28] as Double)
                        )
                    }
                    if (savedList[29] != null && savedList[30] != null) {
                        options.focus =
                            Coordinates(savedList[29] as Double, savedList[30] as Double)
                    }
                    options(options = options)
                }
            }
        )
    }
}