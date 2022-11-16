package com.what3words.components.compose.wrapper

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.what3words.components.compose.utils.W3WTextFieldStateKeys
import com.what3words.components.models.AutosuggestLogicManager
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
 * @param voiceEnabledByDefault if voice should be enabled
 * @param voiceScreenType can choose among [VoiceScreenType.Fullscreen], [VoiceScreenType.AnimatedPopup] and [VoiceScreenType.Inline]
 */
@Composable
fun rememberW3WAutoSuggestTextFieldState(
    voiceEnabledByDefault: Boolean = false,
    voiceScreenType: VoiceScreenType = VoiceScreenType.Fullscreen
): W3WAutoSuggestTextFieldState {
    return rememberSaveable(
        saver = W3WAutoSuggestTextFieldState.Saver,
        inputs = arrayOf(voiceEnabledByDefault, voiceScreenType)
    ) {
        W3WAutoSuggestTextFieldState(
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
    internal val voiceEnabledByDefault: Boolean = false,
    internal val voiceScreenTypeByDefault: VoiceScreenType = VoiceScreenType.Fullscreen
) {
    // ui variables
    internal var internalW3WAutoSuggestEditText: W3WAutoSuggestEditText? by mutableStateOf(value = null)
    internal var defaultSuggestionPicker: W3WAutoSuggestPicker? by mutableStateOf(value = null)
    internal var defaultErrorView: AppCompatTextView? by mutableStateOf(value = null)
    internal var defaultInvalidAddressMessageView: AppCompatTextView? by mutableStateOf(value = null)
    internal var defaultCorrectionPicker: W3WAutoSuggestCorrectionPicker? by mutableStateOf(value = null)

    // auto suggest properties
    var allowFlexibleDelimiters: Boolean by mutableStateOf(value = false)
        private set

    var allowInvalid3wa: Boolean by mutableStateOf(value = false)
        private set

    var searchFlowEnabled: Boolean by mutableStateOf(value = false)
        private set

    var returnCoordinates: Boolean by mutableStateOf(value = false)
        private set

    var invalidSelectionMessage: String? by mutableStateOf(value = null)
        private set

    var hideSelectedIcon: Boolean by mutableStateOf(value = false)
        private set

    var voiceLanguage: String? by mutableStateOf(value = null)
        private set

    var hint: String? by mutableStateOf(value = null)
        private set

    var language: String? by mutableStateOf(value = null)
        private set

    var focus: Coordinates? by mutableStateOf(value = null)
        private set

    var nResults: Int? by mutableStateOf(value = 3)
        private set

    var nFocusResults: Int? by mutableStateOf(value = null)
        private set

    var clipToCountry: List<String>? by mutableStateOf(value = null)
        private set

    var clipToCircle: Coordinates? by mutableStateOf(value = null)
        private set

    var clipToCircleRadius: Double? by mutableStateOf(value = null)
        private set

    var clipToBoundingBox: BoundingBox? by mutableStateOf(value = null)
        private set

    var clipToPolygon: List<Coordinates>? by mutableStateOf(
        value = null
    )
        private set

    var preferLand: Boolean by mutableStateOf(value = true)
        private set

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
     * see [W3WAutoSuggestEditText.allowFlexibleDelimiters]
     * **/
    fun allowFlexibleDelimiters(isAllowed: Boolean): W3WAutoSuggestTextFieldState {
        this.allowFlexibleDelimiters = isAllowed
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.allowInvalid3wa]
     * **/
    fun allowInvalid3wa(isAllowed: Boolean): W3WAutoSuggestTextFieldState {
        this.allowInvalid3wa = isAllowed
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.searchFlowEnabled]
     * **/
    fun searchFlowEnabled(isEnabled: Boolean): W3WAutoSuggestTextFieldState {
        this.searchFlowEnabled = isEnabled
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.returnCoordinates]
     * **/
    fun returnCoordinates(enabled: Boolean): W3WAutoSuggestTextFieldState {
        this.returnCoordinates = enabled
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.hideSelectedIcon]
     * **/
    fun hideSelectedIcon(isHidden: Boolean): W3WAutoSuggestTextFieldState {
        this.hideSelectedIcon = isHidden
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.voiceLanguage]
     * **/
    fun voiceLanguage(language: String): W3WAutoSuggestTextFieldState {
        this.voiceLanguage = language
        return this
    }

    /**
     * see [AppCompatTextView.setHint]
     * **/
    fun hint(chars: String): W3WAutoSuggestTextFieldState {
        this.hint = chars
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.focus]
     * **/
    fun focus(coordinates: Coordinates?): W3WAutoSuggestTextFieldState {
        this.focus = coordinates
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.nResults]
     * **/
    fun nResults(n: Int?): W3WAutoSuggestTextFieldState {
        this.nResults = n
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.nFocusResults]
     * **/
    fun nFocusResults(n: Int?): W3WAutoSuggestTextFieldState {
        this.nFocusResults = n
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.clipToCountry]
     * **/
    fun clipToCountry(countryCodes: List<String>): W3WAutoSuggestTextFieldState {
        this.clipToCountry = countryCodes
        return this
    }


    /**
     * see [W3WAutoSuggestEditText.clipToCircle]
     * **/
    fun clipToCircle(
        centre: Coordinates?,
        radius: Double?
    ): W3WAutoSuggestTextFieldState {
        this.clipToCircle = centre
        this.clipToCircleRadius = radius
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.clipToBoundingBox]
     * **/
    fun clipToBoundingBox(boundingBox: BoundingBox?): W3WAutoSuggestTextFieldState {
        this.clipToBoundingBox = boundingBox
        return this
    }


    /**
     * see [W3WAutoSuggestEditText.clipToPolygon]
     * **/
    fun clipToPolygon(polygon: List<Coordinates>): W3WAutoSuggestTextFieldState {
        this.clipToPolygon = polygon
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.language]
     * **/
    fun language(language: String): W3WAutoSuggestTextFieldState {
        this.language = language
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.preferLand]
     * **/
    fun preferLand(isPreferred: Boolean): W3WAutoSuggestTextFieldState {
        this.preferLand = isPreferred
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.invalidSelectionMessage]
     * **/
    fun invalidSelectionMessage(message: String): W3WAutoSuggestTextFieldState {
        invalidSelectionMessage = message
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.toggleVoice]
     * **/
    fun toggleVoice(): W3WAutoSuggestTextFieldState {
        this.toggleVoice = true
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.correctionMessage]
     * **/
    fun correctionMessage(message: String): W3WAutoSuggestTextFieldState {
        this.correctionMessage = message
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.displayUnits]
     **/
    fun displayUnit(units: DisplayUnits): W3WAutoSuggestTextFieldState {
        this.displayUnit = units
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.errorMessage]
     * */
    fun errorMessage(message: String): W3WAutoSuggestTextFieldState {
        this.errorMessage = message
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.display]
     * **/
    fun display(suggestion: SuggestionWithCoordinates): W3WAutoSuggestTextFieldState {
        this.display = suggestion
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.options]
     * **/
    fun options(options: AutosuggestOptions): W3WAutoSuggestTextFieldState {
        this.options = options
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.voicePlaceholder]
     * **/
    fun voicePlaceholder(placeholder: String): W3WAutoSuggestTextFieldState {
        this.voicePlaceHolder = placeholder
        return this
    }

    /**
     * see [W3WAutoSuggestEditText.voiceEnabled]
     * **/
    fun voiceEnabled(
        enabled: Boolean,
        type: VoiceScreenType = VoiceScreenType.Fullscreen,
        micIcon: Drawable? = null
    ): W3WAutoSuggestTextFieldState {
        this.voiceEnabled = enabled
        this.voiceScreenType = type
        this.micIcon = micIcon
        return this
    }

    internal companion object {
        val Saver: Saver<W3WAutoSuggestTextFieldState, Any> = mapSaver(
            save = {
                mapOf(
                    W3WTextFieldStateKeys.VOICE_ENABLED_BY_DEFAULT to it.voiceEnabledByDefault,
                    W3WTextFieldStateKeys.VOICE_SCREEN_TYPE_BY_DEFAULT to it.voiceScreenTypeByDefault,
                    W3WTextFieldStateKeys.ALLOW_FLEXIBLE_DELIMITERS to it.allowFlexibleDelimiters,
                    W3WTextFieldStateKeys.ALLOW_INVALID_3WA to it.allowInvalid3wa,
                    W3WTextFieldStateKeys.SEARCH_FLOW_ENABLED to it.searchFlowEnabled,
                    W3WTextFieldStateKeys.RETURN_COORDINATES to it.returnCoordinates,
                    W3WTextFieldStateKeys.VOICE_ENABLED to it.voiceEnabled,
                    W3WTextFieldStateKeys.INVALID_SELECTION_MESSAGE to it.invalidSelectionMessage,
                    W3WTextFieldStateKeys.HIDE_SELECTED_ICON to it.hideSelectedIcon,
                    W3WTextFieldStateKeys.TOGGLE_VOICE to it.toggleVoice,
                    W3WTextFieldStateKeys.ERROR_MESSAGE to it.errorMessage,
                    W3WTextFieldStateKeys.CORRECTION_MESSAGE to it.correctionMessage,
                    W3WTextFieldStateKeys.DISPLAY_UNIT to it.displayUnit,
                    W3WTextFieldStateKeys.VOICE_PLACEHOLDER to it.voicePlaceHolder,
                    W3WTextFieldStateKeys.VOICE_SCREEN_TYPE to it.voiceScreenType,
                    W3WTextFieldStateKeys.HINT to it.hint,
                    W3WTextFieldStateKeys.DEFAULT_TEXT to if (it.internalW3WAutoSuggestEditText != null) it.internalW3WAutoSuggestEditText!!.text.toString() else null,
                    W3WTextFieldStateKeys.LANGUAGE to it.language,
                    W3WTextFieldStateKeys.N_RESULTS to it.nResults,
                    W3WTextFieldStateKeys.N_FOCUS_RESULTS to it.nFocusResults,
                    W3WTextFieldStateKeys.PREFER_LAND to it.preferLand,
                    W3WTextFieldStateKeys.FOCUS_LAT to it.focus?.lat,
                    W3WTextFieldStateKeys.FOCUS_LNG to it.focus?.lng,
                    W3WTextFieldStateKeys.CLIP_TO_COUNTRY to it.clipToCountry,
                    W3WTextFieldStateKeys.CLIP_TO_CIRCLE_LAT to it.clipToCircle?.lat,
                    W3WTextFieldStateKeys.CLIP_TO_CIRCLE_LNG to it.clipToCircle?.lng,
                    W3WTextFieldStateKeys.CLIP_TO_CIRCLE_RADIUS to it.clipToCircleRadius,
                    W3WTextFieldStateKeys.CLIP_TO_BOUNDING_BOX_NE_LAT to it.clipToBoundingBox?.ne?.lat,
                    W3WTextFieldStateKeys.CLIP_TO_BOUNDING_BOX_NE_LNG to it.clipToBoundingBox?.ne?.lng,
                    W3WTextFieldStateKeys.CLIP_TO_BOUNDING_BOX_SW_LAT to it.clipToBoundingBox?.sw?.lat,
                    W3WTextFieldStateKeys.CLIP_TO_BOUNDING_BOX_SW_LNG to it.clipToBoundingBox?.sw?.lng,
                    W3WTextFieldStateKeys.AutoSuggestOptionsKey.LANGUAGE to it.options?.language,
                    W3WTextFieldStateKeys.AutoSuggestOptionsKey.N_RESULTS to it.options?.nResults,
                    W3WTextFieldStateKeys.AutoSuggestOptionsKey.N_FOCUS_RESULTS to it.options?.nFocusResults,
                    W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_COUNTRY to it.options?.clipToCountry,
                    W3WTextFieldStateKeys.AutoSuggestOptionsKey.PREFER_LAND to it.options?.preferLand,
                    W3WTextFieldStateKeys.AutoSuggestOptionsKey.INPUT_TYPE to it.options?.inputType,
                    W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_CIRCLE_RADIUS to it.options?.clipToCircleRadius,
                    W3WTextFieldStateKeys.AutoSuggestOptionsKey.SOURCE to it.options?.source,
                    W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_CIRCLE_LAT to it.options?.clipToCircle?.lat,
                    W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_CIRCLE_LNG to it.options?.clipToCircle?.lng,
                    W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_SW_LAT to it.options?.clipToBoundingBox?.sw?.lat,
                    W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_SW_LNG to it.options?.clipToBoundingBox?.sw?.lng,
                    W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_NE_LAT to it.options?.clipToBoundingBox?.ne?.lat,
                    W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_NE_LNG to it.options?.clipToBoundingBox?.ne?.lng,
                    W3WTextFieldStateKeys.AutoSuggestOptionsKey.FOCUS_LAT to it.options?.focus?.lat,
                    W3WTextFieldStateKeys.AutoSuggestOptionsKey.FOCUS_LNG to it.options?.focus?.lng
                )

            },
            restore = { savedMap: Map<String, Any?> ->
                W3WAutoSuggestTextFieldState(
                    voiceEnabledByDefault = savedMap[W3WTextFieldStateKeys.VOICE_ENABLED_BY_DEFAULT] as Boolean,
                    voiceScreenTypeByDefault = savedMap[W3WTextFieldStateKeys.VOICE_SCREEN_TYPE_BY_DEFAULT] as VoiceScreenType
                ).apply {
                    allowFlexibleDelimiters =
                        savedMap[W3WTextFieldStateKeys.ALLOW_FLEXIBLE_DELIMITERS] as Boolean
                    allowInvalid3wa = savedMap[W3WTextFieldStateKeys.ALLOW_INVALID_3WA] as Boolean
                    searchFlowEnabled =
                        savedMap[W3WTextFieldStateKeys.SEARCH_FLOW_ENABLED] as Boolean
                    returnCoordinates =
                        savedMap[W3WTextFieldStateKeys.RETURN_COORDINATES] as Boolean
                    voiceEnabled = savedMap[W3WTextFieldStateKeys.VOICE_ENABLED] as Boolean
                    invalidSelectionMessage =
                        savedMap[W3WTextFieldStateKeys.INVALID_SELECTION_MESSAGE] as String?
                    hideSelectedIcon = savedMap[W3WTextFieldStateKeys.HIDE_SELECTED_ICON] as Boolean
                    toggleVoice = savedMap[W3WTextFieldStateKeys.TOGGLE_VOICE] as Boolean
                    errorMessage = savedMap[W3WTextFieldStateKeys.ERROR_MESSAGE] as String?
                    correctionMessage =
                        savedMap[W3WTextFieldStateKeys.CORRECTION_MESSAGE] as String?
                    displayUnit = savedMap[W3WTextFieldStateKeys.DISPLAY_UNIT] as DisplayUnits?
                    voicePlaceHolder = savedMap[W3WTextFieldStateKeys.VOICE_PLACEHOLDER] as String?
                    defaultText = savedMap[W3WTextFieldStateKeys.DEFAULT_TEXT] as String
                    hint = savedMap[W3WTextFieldStateKeys.HINT] as String?
                    voiceScreenType =
                        savedMap[W3WTextFieldStateKeys.VOICE_SCREEN_TYPE] as VoiceScreenType
                    voiceLanguage = savedMap[W3WTextFieldStateKeys.VOICE_LANGUAGE] as String?

                    language = savedMap[W3WTextFieldStateKeys.LANGUAGE] as String?
                    nResults = savedMap[W3WTextFieldStateKeys.N_RESULTS] as Int?
                    nFocusResults = savedMap[W3WTextFieldStateKeys.N_FOCUS_RESULTS] as Int?
                    preferLand = savedMap[W3WTextFieldStateKeys.PREFER_LAND] as Boolean

                    if (savedMap[W3WTextFieldStateKeys.FOCUS_LAT] != null && savedMap[W3WTextFieldStateKeys.FOCUS_LNG] != null) {
                        focus =
                            Coordinates(
                                savedMap[W3WTextFieldStateKeys.FOCUS_LAT] as Double,
                                savedMap[W3WTextFieldStateKeys.FOCUS_LNG] as Double
                            )
                    }

                    if (savedMap[W3WTextFieldStateKeys.CLIP_TO_BOUNDING_BOX_SW_LAT] != null && savedMap[W3WTextFieldStateKeys.CLIP_TO_BOUNDING_BOX_SW_LNG] != null
                        && savedMap[W3WTextFieldStateKeys.CLIP_TO_BOUNDING_BOX_NE_LAT] != null && savedMap[W3WTextFieldStateKeys.CLIP_TO_BOUNDING_BOX_NE_LNG] != null
                    ) {
                        clipToBoundingBox = BoundingBox(
                            Coordinates(
                                savedMap[W3WTextFieldStateKeys.CLIP_TO_BOUNDING_BOX_SW_LAT] as Double,
                                savedMap[W3WTextFieldStateKeys.CLIP_TO_BOUNDING_BOX_SW_LNG] as Double
                            ),
                            Coordinates(
                                savedMap[W3WTextFieldStateKeys.CLIP_TO_BOUNDING_BOX_NE_LAT] as Double,
                                savedMap[W3WTextFieldStateKeys.CLIP_TO_BOUNDING_BOX_NE_LNG] as Double
                            )
                        )
                    }

                    if (savedMap[W3WTextFieldStateKeys.CLIP_TO_CIRCLE_LAT] != null && savedMap[W3WTextFieldStateKeys.CLIP_TO_CIRCLE_LNG] != null
                        && savedMap[W3WTextFieldStateKeys.CLIP_TO_CIRCLE_RADIUS] != null
                    ) {
                        clipToCircle =
                            Coordinates(
                                savedMap[W3WTextFieldStateKeys.CLIP_TO_CIRCLE_LAT] as Double,
                                savedMap[W3WTextFieldStateKeys.CLIP_TO_CIRCLE_LNG] as Double
                            )
                        clipToCircleRadius =
                            savedMap[W3WTextFieldStateKeys.CLIP_TO_CIRCLE_RADIUS] as Double
                    }

                    clipToCountry = savedMap[W3WTextFieldStateKeys.CLIP_TO_COUNTRY] as List<String>?

                    val options = AutosuggestOptions()
                    (savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.LANGUAGE] as String?)?.let {
                        options.language = it
                    }
                    (savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.N_RESULTS] as Int?)?.let {
                        options.nResults = it
                    }
                    (savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.N_FOCUS_RESULTS] as Int?)?.let {
                        options.nFocusResults = it
                    }
                    (savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_COUNTRY] as List<String>?)?.let {
                        options.clipToCountry = it
                    }
                    (savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.PREFER_LAND] as Boolean?)?.let {
                        options.preferLand = it
                    }
                    (savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.INPUT_TYPE] as AutosuggestInputType?)?.let {
                        options.inputType = it
                    }
                    (savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_CIRCLE_RADIUS] as Double?)?.let {
                        options.clipToCircleRadius = it
                    }
                    (savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.SOURCE] as SourceApi?)?.let {
                        options.source = it
                    }
                    if (savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_CIRCLE_LAT] != null && savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_CIRCLE_LNG] != null) {
                        options.clipToCircle =
                            Coordinates(
                                savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_CIRCLE_LAT] as Double,
                                savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_CIRCLE_LNG] as Double
                            )
                    }
                    if (savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_SW_LAT] != null && savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_SW_LNG] != null && savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_NE_LAT] != null && savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_NE_LNG] != null) {
                        options.clipToBoundingBox = BoundingBox(
                            Coordinates(
                                savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_SW_LAT] as Double,
                                savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_SW_LNG] as Double
                            ),
                            Coordinates(
                                savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_NE_LAT] as Double,
                                savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.CLIP_TO_BOUNDING_BOX_NE_LNG] as Double
                            )
                        )
                    }
                    if (savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.FOCUS_LAT] != null && savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.FOCUS_LNG] != null) {
                        options.focus =
                            Coordinates(
                                savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.FOCUS_LAT] as Double,
                                savedMap[W3WTextFieldStateKeys.AutoSuggestOptionsKey.FOCUS_LNG] as Double
                            )
                    }
                    options(options = options)
                }
            }
        )
    }
}
