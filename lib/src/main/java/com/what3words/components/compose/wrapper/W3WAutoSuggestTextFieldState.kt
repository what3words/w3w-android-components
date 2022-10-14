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
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.request.BoundingBox
import com.what3words.javawrapper.request.Coordinates
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
     * see [W3WAutoSuggestEditText.nResults]
     **/
    var nResults: Int by mutableStateOf(value = 3)

    /**
     * see [W3WAutoSuggestEditText.focus]
     * **/
    var focus: Coordinates? by mutableStateOf(value = null)

    /**
     * see [W3WAutoSuggestEditText.nFocusResults]
     * **/
    var nFocusResults: Int by mutableStateOf(value = nResults)

    /**
     * see [W3WAutoSuggestEditText.clipToCountry]
     * **/
    var clipToCountry: List<String> by mutableStateOf(value = listOf())

    /**
     * see [W3WAutoSuggestEditText.clipToPolygon]
     * **/
    var clipToPolygon: List<Coordinates> by mutableStateOf(value = listOf())

    /**
     * see [W3WAutoSuggestEditText.clipToBoundingBox]
     * **/
    var clipToBoundingBox: BoundingBox? by mutableStateOf(value = null)

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
     * see [W3WAutoSuggestEditText.preferLand]
     * **/
    var preferLand: Boolean by mutableStateOf(value = true)

    /**
     * used to save text state for [W3WAutoSuggestEditText] in the case of a recomposition/configuration change
     * **/
    internal var defaultText: String? = null

    internal var options: AutosuggestOptions? by mutableStateOf(value = null)
    internal var clipToCircle: Pair<Coordinates?, Double?>? by mutableStateOf(value = null)
    internal var toggleVoice: Boolean by mutableStateOf(value = false)
    internal var errorMessage: String? by mutableStateOf(value = null)
    internal var correctionMessage: String? by mutableStateOf(value = null)
    internal var displayUnit: DisplayUnits? by mutableStateOf(value = null)
    internal var display: SuggestionWithCoordinates? by mutableStateOf(value = null)
    internal var voicePlaceHolder: String? by mutableStateOf(value = null)


    /**
     * see [W3WAutoSuggestEditText.clipToCircle]
     * **/
    fun clipToCircle(centre: Coordinates?, radius: Double?) {
        this.clipToCircle = Pair(first = centre, second = radius)
    }

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
                    it.nResults,
                    it.nFocusResults,
                    it.clipToCountry,
                    it.returnCoordinates,
                    it.voiceEnabled,
                    it.invalidSelectionMessage,
                    it.hideSelectedIcon,
                    it.preferLand,
                    it.toggleVoice,
                    it.errorMessage,
                    it.correctionMessage,
                    it.displayUnit,
                    it.voicePlaceHolder,
                    if (it.internalW3WAutoSuggestEditText != null) it.internalW3WAutoSuggestEditText!!.text.toString() else null
                )
            },
            restore = {
                W3WAutoSuggestTextFieldState(
                    apiKey = it[0] as String,
                    voiceEnabledByDefault = it[1] as Boolean,
                    voiceScreenType = it[2] as VoiceScreenType
                ).apply {
                    allowFlexibleDelimiters = it[3] as Boolean
                    allowInvalid3wa = it[4] as Boolean
                    searchFlowEnabled = it[5] as Boolean
                    nResults = it[6] as Int
                    nFocusResults = it[7] as Int
                    clipToCountry = it[8] as List<String>
                    returnCoordinates = it[9] as Boolean
                    voiceEnabled = it[10] as Boolean
                    invalidSelectionMessage = it[11] as String?
                    hideSelectedIcon = it[12] as Boolean
                    preferLand = it[13] as Boolean
                    toggleVoice = it[14] as Boolean
                    errorMessage = it[15] as String?
                    correctionMessage = it[16] as String?
                    displayUnit = it[17] as DisplayUnits?
                    voicePlaceHolder = it[18] as String?
                    defaultText = it[19] as String?
                }
            }
        )
    }
}