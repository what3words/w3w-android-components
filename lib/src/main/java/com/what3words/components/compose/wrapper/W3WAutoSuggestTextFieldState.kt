package com.what3words.components.compose.wrapper

import android.graphics.drawable.Drawable
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
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
            voiceScreenTypeByDefault = voiceScreenType
        )
    }
}

/**
 * A state object that can be used to configure the appearance and functionalities of a [W3WAutoSuggestTextField]
 *
 * @property apiKey your API key from what3words developer dashboard
 * @property voiceEnabledByDefault if voice should be enabled
 * @property voiceScreenTypeByDefault can choose among [VoiceScreenType.Fullscreen], [VoiceScreenType.AnimatedPopup] and [VoiceScreenType.Inline]
 */
class W3WAutoSuggestTextFieldState(
    internal val apiKey: String,
    internal val voiceEnabledByDefault: Boolean = false,
    internal val voiceScreenTypeByDefault: VoiceScreenType = VoiceScreenType.Fullscreen
) {
    // ui variables
    internal var internalW3WAutoSuggestEditText: W3WAutoSuggestEditText? by mutableStateOf(value = null)

    internal var defaultSuggestionPicker: W3WAutoSuggestPicker? by mutableStateOf(value = null)
    var customSuggestionPicker: W3WAutoSuggestPicker? by mutableStateOf(value = null)
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
    internal var voiceEnabled: Boolean by mutableStateOf(value = voiceEnabledByDefault)
    internal var voiceScreenType: VoiceScreenType by mutableStateOf(voiceScreenTypeByDefault)
    internal var micIcon: Drawable? by mutableStateOf(null)

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

    /**
     * see [W3WAutoSuggestEditText.voiceEnabled]
     * **/
    fun voiceEnabled(
        enabled: Boolean,
        type: VoiceScreenType = VoiceScreenType.Fullscreen,
        micIcon: Drawable? = null
    ) {
        this.voiceEnabled = enabled
        this.voiceScreenType = type
        this.micIcon = micIcon
    }


    internal companion object {
        val Saver: Saver<W3WAutoSuggestTextFieldState, Any> = mapSaver(
            save = {
                mapOf(
                    Keys.API_KEY to it.apiKey,
                    Keys.VOICE_ENABLED_BY_DEFAULT to it.voiceEnabledByDefault,
                    Keys.VOICE_SCREEN_TYPE_BY_DEFAULT to it.voiceScreenTypeByDefault,
                    Keys.ALLOW_FLEXIBLE_DELIMITERS to it.allowFlexibleDelimiters,
                    Keys.ALLOW_INVALID_3WA to it.allowInvalid3wa,
                    Keys.SEARCH_FLOW_ENABLED to it.searchFlowEnabled,
                    Keys.RETURN_COORDINATES to it.returnCoordinates,
                    Keys.VOICE_ENABLED to it.voiceEnabled,
                    Keys.INVALID_SELECTION_MESSAGE to it.invalidSelectionMessage,
                    Keys.HIDE_SELECTED_ICON to it.hideSelectedIcon,
                    Keys.TOGGLE_VOICE to it.toggleVoice,
                    Keys.ERROR_MESSAGE to it.errorMessage,
                    Keys.CORRECTION_MESSAGE to it.correctionMessage,
                    Keys.DISPLAY_UNIT to it.displayUnit,
                    Keys.VOICE_PLACEHOLDER to it.voicePlaceHolder,
                    Keys.VOICE_SCREEN_TYPE to it.voiceScreenType,
                    Keys.DEFAULT_TEXT to if (it.internalW3WAutoSuggestEditText != null) it.internalW3WAutoSuggestEditText!!.text.toString() else null,
                    Keys.AutoSuggestOptionsKey.LANGUAGE to it.options?.language,
                    Keys.AutoSuggestOptionsKey.N_RESULTS to it.options?.nResults,
                    Keys.AutoSuggestOptionsKey.N_FOCUS_RESULTS to it.options?.nFocusResults,
                    Keys.AutoSuggestOptionsKey.CLIP_TO_COUNTRY to it.options?.clipToCountry,
                    Keys.AutoSuggestOptionsKey.PREFER_LAND to it.options?.preferLand,
                    Keys.AutoSuggestOptionsKey.INPUT_TYPE to it.options?.inputType,
                    Keys.AutoSuggestOptionsKey.CLIP_TO_CIRCLE_RADIUS to it.options?.clipToCircleRadius,
                    Keys.AutoSuggestOptionsKey.SOURCE to it.options?.source,
                    Keys.AutoSuggestOptionsKey.CLIP_TO_CIRCLE_LAT to it.options?.clipToCircle?.lat,
                    Keys.AutoSuggestOptionsKey.CLIP_TO_CIRCLE_LNG to it.options?.clipToCircle?.lng,
                    Keys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_SW_LAT to it.options?.clipToBoundingBox?.sw?.lat,
                    Keys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_SW_LNG to it.options?.clipToBoundingBox?.sw?.lng,
                    Keys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_NE_LAT to it.options?.clipToBoundingBox?.ne?.lat,
                    Keys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_NE_LNG to it.options?.clipToBoundingBox?.ne?.lng,
                    Keys.AutoSuggestOptionsKey.FOCUS_LAT to it.options?.focus?.lat,
                    Keys.AutoSuggestOptionsKey.FOCUS_LNG to it.options?.focus?.lng
                )

            },
            restore = { savedMap: Map<String, Any?> ->
                W3WAutoSuggestTextFieldState(
                    apiKey = savedMap[Keys.API_KEY] as String,
                    voiceEnabledByDefault = savedMap[Keys.VOICE_ENABLED_BY_DEFAULT] as Boolean,
                    voiceScreenTypeByDefault = savedMap[Keys.VOICE_SCREEN_TYPE_BY_DEFAULT] as VoiceScreenType
                ).apply {
                    allowFlexibleDelimiters = savedMap[Keys.ALLOW_FLEXIBLE_DELIMITERS] as Boolean
                    allowInvalid3wa = savedMap[Keys.ALLOW_INVALID_3WA] as Boolean
                    searchFlowEnabled = savedMap[Keys.SEARCH_FLOW_ENABLED] as Boolean
                    returnCoordinates = savedMap[Keys.RETURN_COORDINATES] as Boolean
                    voiceEnabled = savedMap[Keys.VOICE_ENABLED] as Boolean
                    invalidSelectionMessage = savedMap[Keys.INVALID_SELECTION_MESSAGE] as String?
                    hideSelectedIcon = savedMap[Keys.HIDE_SELECTED_ICON] as Boolean
                    toggleVoice = savedMap[Keys.TOGGLE_VOICE] as Boolean
                    errorMessage = savedMap[Keys.ERROR_MESSAGE] as String?
                    correctionMessage = savedMap[Keys.CORRECTION_MESSAGE] as String?
                    displayUnit = savedMap[Keys.DISPLAY_UNIT] as DisplayUnits?
                    voicePlaceHolder = savedMap[Keys.VOICE_PLACEHOLDER] as String?
                    defaultText = savedMap[Keys.DEFAULT_TEXT] as String
                    voiceScreenType = savedMap[Keys.VOICE_SCREEN_TYPE] as VoiceScreenType

                    val options: AutosuggestOptions = AutosuggestOptions()
                    (savedMap[Keys.AutoSuggestOptionsKey.LANGUAGE] as String?)?.let {
                        options.language = it
                    }
                    (savedMap[Keys.AutoSuggestOptionsKey.N_RESULTS] as Int?)?.let {
                        options.nResults = it
                    }
                    (savedMap[Keys.AutoSuggestOptionsKey.N_FOCUS_RESULTS] as Int?)?.let {
                        options.nFocusResults = it
                    }
                    (savedMap[Keys.AutoSuggestOptionsKey.CLIP_TO_COUNTRY] as List<String>?)?.let {
                        options.clipToCountry = it
                    }
                    (savedMap[Keys.AutoSuggestOptionsKey.PREFER_LAND] as Boolean?)?.let {
                        options.preferLand = it
                    }
                    (savedMap[Keys.AutoSuggestOptionsKey.INPUT_TYPE] as AutosuggestInputType?)?.let {
                        options.inputType = it
                    }
                    (savedMap[Keys.AutoSuggestOptionsKey.CLIP_TO_CIRCLE_RADIUS] as Double?)?.let {
                        options.clipToCircleRadius = it
                    }
                    (savedMap[Keys.AutoSuggestOptionsKey.SOURCE] as SourceApi?)?.let {
                        options.source = it
                    }
                    if (savedMap[Keys.AutoSuggestOptionsKey.CLIP_TO_CIRCLE_LAT] != null && savedMap[Keys.AutoSuggestOptionsKey.CLIP_TO_CIRCLE_LNG] != null) {
                        options.clipToCircle =
                            Coordinates(
                                savedMap[Keys.AutoSuggestOptionsKey.CLIP_TO_CIRCLE_LAT] as Double,
                                savedMap[Keys.AutoSuggestOptionsKey.CLIP_TO_CIRCLE_LNG] as Double
                            )
                    }
                    if (savedMap[Keys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_SW_LAT] != null && savedMap[Keys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_SW_LNG] != null && savedMap[Keys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_NE_LAT] != null && savedMap[Keys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_NE_LNG] != null) {
                        options.clipToBoundingBox = BoundingBox(
                            Coordinates(
                                savedMap[Keys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_SW_LAT] as Double,
                                savedMap[Keys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_SW_LNG] as Double
                            ),
                            Coordinates(
                                savedMap[Keys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_NE_LAT] as Double,
                                savedMap[Keys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_NE_LNG] as Double
                            )
                        )
                    }
                    if (savedMap[Keys.AutoSuggestOptionsKey.FOCUS_LAT] != null && savedMap[Keys.AutoSuggestOptionsKey.FOCUS_LNG] != null) {
                        options.focus =
                            Coordinates(
                                savedMap[Keys.AutoSuggestOptionsKey.FOCUS_LAT] as Double,
                                savedMap[Keys.AutoSuggestOptionsKey.FOCUS_LNG] as Double
                            )
                    }
                    options(options = options)
                }
            }
        )

        private object Keys {
            const val API_KEY = "apiKey"
            const val VOICE_ENABLED_BY_DEFAULT = "voiceEnabledByDefault"
            const val VOICE_SCREEN_TYPE_BY_DEFAULT = "voiceScreenTypeByDefault"
            const val ALLOW_FLEXIBLE_DELIMITERS = "allowFlexibleDelimiters"
            const val ALLOW_INVALID_3WA = "allowInvalid3WordsAddress"
            const val SEARCH_FLOW_ENABLED = "searchFlowEnabled"
            const val RETURN_COORDINATES = "returnCoordinates"
            const val VOICE_ENABLED = "voiceEnabled"
            const val INVALID_SELECTION_MESSAGE = "invalidSelectionMessage"
            const val HIDE_SELECTED_ICON = "hideSelectedIcon"
            const val TOGGLE_VOICE = "toggleVoice"
            const val ERROR_MESSAGE = "errorMessage"
            const val CORRECTION_MESSAGE = "correctionMessage"
            const val DISPLAY_UNIT = "displayUnit"
            const val VOICE_PLACEHOLDER = "voicePlaceHolder"
            const val DEFAULT_TEXT = "editTextText"
            const val VOICE_SCREEN_TYPE = "voiceScreenType"

            // keys for attributes in AutoSuggestOptions
            object AutoSuggestOptionsKey {
                const val LANGUAGE = "language"
                const val N_RESULTS = "nResults"
                const val N_FOCUS_RESULTS = "nFocusResults"
                const val CLIP_TO_COUNTRY = "clipToCountry"
                const val PREFER_LAND = "preferLand"
                const val INPUT_TYPE = "inputType"
                const val CLIP_TO_CIRCLE_RADIUS = "clipToCircleRadius"
                const val SOURCE = "source"
                const val CLIP_TO_CIRCLE_LAT = "clipToCircleLat"
                const val CLIP_TO_CIRCLE_LNG = "clipToCircleLng"
                const val CLIP_TO_BOUNDING_BOX_SW_LAT = "clipToBoundingBoxSWLat"
                const val CLIP_TO_BOUNDING_BOX_SW_LNG = "clipToBoundingBoxSWLng"
                const val CLIP_TO_BOUNDING_BOX_NE_LAT = "clipToBoundingBoxNELat"
                const val CLIP_TO_BOUNDING_BOX_NE_LNG = "clipToBoundingBoxNELng"
                const val FOCUS_LAT = "focusLat"
                const val FOCUS_LNG = "focusLng"
            }
        }

    }
}