package com.what3words.components.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.what3words.components.compose.wrapper.W3WAutoSuggestTextFieldState
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.SuggestionWithCoordinates

@Composable
internal fun AttachCorrectionPicker(state: W3WAutoSuggestTextFieldState) {
    LaunchedEffect(
        state.defaultCorrectionPicker,
        state.w3wAutoSuggestEditText,
        block = {
            if (state.defaultCorrectionPicker != null) {
                state.w3wAutoSuggestEditText?.customCorrectionPicker(
                    customCorrectionPicker = state.defaultCorrectionPicker
                )
            }
        }
    )
}

@Composable
internal fun AttachErrorView(
    state: W3WAutoSuggestTextFieldState,
    onError: ((APIResponse.What3WordsError) -> Unit)?
) {
    LaunchedEffect(
        state.defaultErrorView,
        state.w3wAutoSuggestEditText,
        block = {
            if (state.defaultErrorView != null) {
                state.w3wAutoSuggestEditText?.onError(
                    errorView = state.defaultErrorView,
                    errorCallback = {
                        onError?.invoke(it)
                    }
                )
            }
        }
    )
}

@Composable
internal fun AttachSuggestionPickerAndInvalidMessageView(
    state: W3WAutoSuggestTextFieldState,
    onSuggestionWithCoordinates: ((SuggestionWithCoordinates?) -> Unit)?
) {
    LaunchedEffect(
        state.defaultSuggestionPicker,
        state.defaultInvalidAddressMessageView,
        state.w3wAutoSuggestEditText,
        block = {
            if (state.defaultSuggestionPicker != null || state.defaultInvalidAddressMessageView != null) {
                state.w3wAutoSuggestEditText?.onSuggestionSelected(
                    picker = state.defaultSuggestionPicker,
                    invalidAddressMessageView = state.defaultInvalidAddressMessageView
                ) {
                    onSuggestionWithCoordinates?.invoke(it)
                }
            }
        })
}

object W3WTextFieldStateKeys {
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
    const val HINT = "hint"
    const val VOICE_LANGUAGE = "voiceLanguage"

    const val LANGUAGE = "language"
    const val N_RESULTS = "nResults"
    const val N_FOCUS_RESULTS = "nFocusResults"
    const val CLIP_TO_COUNTRY = "clipToCountry"
    const val PREFER_LAND = "preferLand"
    const val CLIP_TO_CIRCLE_RADIUS = "clipToCircleRadius"
    const val CLIP_TO_CIRCLE_LAT = "clipToCircleLat"
    const val CLIP_TO_CIRCLE_LNG = "clipToCircleLng"
    const val CLIP_TO_BOUNDING_BOX_SW_LAT = "clipToBoundingBoxSWLat"
    const val CLIP_TO_BOUNDING_BOX_SW_LNG = "clipToBoundingBoxSWLng"
    const val CLIP_TO_BOUNDING_BOX_NE_LAT = "clipToBoundingBoxNELat"
    const val CLIP_TO_BOUNDING_BOX_NE_LNG = "clipToBoundingBoxNELng"
    const val FOCUS_LAT = "focusLat"
    const val FOCUS_LNG = "focusLng"


    // keys for attributes in AutoSuggestOptions
    object AutoSuggestOptionsKey {
        const val LANGUAGE = "optionsLanguage"
        const val N_RESULTS = "optionsNResults"
        const val N_FOCUS_RESULTS = "optionsNFocusResults"
        const val CLIP_TO_COUNTRY = "optionsClipToCountry"
        const val PREFER_LAND = "optionsPreferLand"
        const val INPUT_TYPE = "optionsInputType"
        const val CLIP_TO_CIRCLE_RADIUS = "optionsClipToCircleRadius"
        const val SOURCE = "optionsSource"
        const val CLIP_TO_CIRCLE_LAT = "optionsClipToCircleLat"
        const val CLIP_TO_CIRCLE_LNG = "optionsClipToCircleLng"
        const val CLIP_TO_BOUNDING_BOX_SW_LAT = "optionsClipToBoundingBoxSWLat"
        const val CLIP_TO_BOUNDING_BOX_SW_LNG = "optionsClipToBoundingBoxSWLng"
        const val CLIP_TO_BOUNDING_BOX_NE_LAT = "optionsClipToBoundingBoxNELat"
        const val CLIP_TO_BOUNDING_BOX_NE_LNG = "optionsClipToBoundingBoxNELng"
        const val FOCUS_LAT = "optionsFocusLat"
        const val FOCUS_LNG = "optionsFocusLng"
    }
}