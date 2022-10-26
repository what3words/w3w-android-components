package com.what3words.components.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.what3words.components.compose.wrapper.W3WAutoSuggestTextFieldState
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
                if (state.toggleVoice) {
                    toggleVoice()
                    state.toggleVoice = false
                }
            }
        })
}