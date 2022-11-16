package com.what3words.components.compose.utils

import android.content.Context
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.what3words.components.compose.wrapper.W3WAutoSuggestTextFieldState
import com.what3words.components.models.AutosuggestLogicManager
import com.what3words.components.text.W3WAutoSuggestEditText

/**
 * a composable that observes changes to the state attributes in the [W3WAutoSuggestTextFieldState]
 * and uses those values to configure the internal [W3WAutoSuggestEditText]
 * **/
@Composable
internal fun ConfigureAutoSuggest(state: W3WAutoSuggestTextFieldState) {
    LaunchedEffect(
        state.internalW3WAutoSuggestEditText,
        state.allowFlexibleDelimiters,
        state.searchFlowEnabled,
        state.allowInvalid3wa,
        state.displayUnit,
        state.hideSelectedIcon,
        state.correctionMessage,
        state.toggleVoice,
        state.display,
        state.voicePlaceHolder,
        state.options,
        state.errorMessage,
        state.invalidSelectionMessage,
        state.voiceEnabled,
        state.returnCoordinates,
        state.voiceScreenType,
        state.micIcon,
        state.hint,
        state.voiceLanguage,
        state.language,
        state.focus,
        state.nFocusResults,
        state.nResults,
        state.clipToCircle,
        state.clipToCountry,
        state.clipToCircleRadius,
        state.clipToBoundingBox,
        state.clipToPolygon,
        state.preferLand,
        block = {
            state.internalW3WAutoSuggestEditText?.apply {
                allowFlexibleDelimiters(isAllowed = state.allowFlexibleDelimiters)
                searchFlowEnabled(isEnabled = state.searchFlowEnabled)
                allowInvalid3wa(isAllowed = state.allowInvalid3wa)
                hideSelectedIcon(b = state.hideSelectedIcon)
                returnCoordinates(enabled = state.returnCoordinates)
                voiceEnabled(
                    enabled = state.voiceEnabled,
                    type = state.voiceScreenType,
                    micIcon = state.micIcon
                )
                hideSelectedIcon(b = state.hideSelectedIcon)
                clipToCircle(centre = state.clipToCircle, radius = state.clipToCircleRadius)
                clipToCountry(countryCodes = state.clipToCountry ?: listOf())
                clipToBoundingBox(boundingBox = state.clipToBoundingBox)
                clipToPolygon(polygon = state.clipToPolygon ?: listOf())
                returnCoordinates(enabled = state.returnCoordinates)
                preferLand(isPreferred = state.preferLand)

                state.invalidSelectionMessage?.let { invalidSelectionMessage(message = state.invalidSelectionMessage!!) }
                state.correctionMessage?.let { correctionMessage(message = state.correctionMessage!!) }
                state.displayUnit?.let { displayUnit(units = state.displayUnit!!) }
                state.options?.let { options(options = state.options!!) }
                state.errorMessage?.let { errorMessage(message = state.errorMessage!!) }
                state.correctionMessage?.let { correctionMessage(message = state.correctionMessage!!) }
                state.displayUnit?.let { displayUnit(units = state.displayUnit!!) }
                state.display?.let { display(suggestion = state.display!!) }
                state.voicePlaceHolder?.let {
                    voicePlaceholder(placeholder = state.voicePlaceHolder!!)
                }
                state.hint?.let { state.internalW3WAutoSuggestEditText?.hint = state.hint }
                state.voiceLanguage?.let { voiceLanguage(language = state.voiceLanguage!!) }
                if (state.toggleVoice) {
                    toggleVoice()
                    state.toggleVoice = false
                }
            }
        })
}

/**
 * @param apiKey your API key from what3words developer dashboard
 * @param context the base context
 * @param style the resource ID of the theme to be applied on top of
 *                   the base context's theme
 * **/
internal fun W3WAutoSuggestTextFieldState.createW3WAutoSuggestEditText(
    apiKey: String,
    context: Context,
    style: Int
): W3WAutoSuggestEditText {
    return W3WAutoSuggestEditText(
        ContextThemeWrapper(
            context,
            style
        )
    )
        .apiKey(apiKey)
        .voiceEnabled(
            enabled = voiceEnabledByDefault,
            type = voiceScreenTypeByDefault,
            micIcon = micIcon
        ).apply {
            if (!defaultText.isNullOrEmpty()) this.setText(defaultText!!)
        }
}

/**
 * @param sdk logicManager manager created using SDK instead of API
 * @param context the base context
 * @param style the resource ID of the theme to be applied on top of
 *                   the base context's theme
 * **/
internal fun W3WAutoSuggestTextFieldState.createW3WAutoSuggestEditText(
    sdk: AutosuggestLogicManager,
    context: Context,
    style: Int
): W3WAutoSuggestEditText {
    return W3WAutoSuggestEditText(
        ContextThemeWrapper(
            context,
            style
        )
    )
        .sdk(logicManager = sdk)
        .voiceEnabled(
            enabled = voiceEnabledByDefault,
            type = voiceScreenTypeByDefault,
            micIcon = micIcon
        ).apply {
            if (!defaultText.isNullOrEmpty()) this.setText(defaultText!!)
        }
}