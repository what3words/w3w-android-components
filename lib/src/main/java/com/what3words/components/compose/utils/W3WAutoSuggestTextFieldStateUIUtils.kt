package com.what3words.components.compose.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.what3words.components.compose.wrapper.W3WAutoSuggestTextFieldState
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.SuggestionWithCoordinates

@Composable
internal fun AttachCorrectionPicker(state: W3WAutoSuggestTextFieldState) {
    LaunchedEffect(
        key1 = state.defaultCorrectionPicker,
        key2 = state.internalW3WAutoSuggestEditText,
        block = {
            if (state.defaultCorrectionPicker != null) {
                state.internalW3WAutoSuggestEditText?.customCorrectionPicker(
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
        key1 = state.defaultErrorView,
        key2 = state.internalW3WAutoSuggestEditText,
        block = {
            if (state.defaultErrorView != null) {
                state.internalW3WAutoSuggestEditText?.onError(
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
        key1 = state.defaultSuggestionPicker,
        key2 = state.defaultInvalidAddressMessageView,
        key3 = state.internalW3WAutoSuggestEditText,
        block = {
            if (state.defaultSuggestionPicker != null || state.defaultInvalidAddressMessageView != null) {
                state.internalW3WAutoSuggestEditText?.onSuggestionSelected(
                    picker = state.defaultSuggestionPicker,
                    invalidAddressMessageView = state.defaultInvalidAddressMessageView
                ) {
                    onSuggestionWithCoordinates?.invoke(it)
                }
            }
        })
}