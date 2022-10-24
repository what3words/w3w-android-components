package com.what3words.components.compose.utils

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.what3words.components.compose.wrapper.W3WAutoSuggestTextFieldState
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.SuggestionWithCoordinates

@Composable
internal fun AttachCorrectionPicker(state: W3WAutoSuggestTextFieldState) {
    LaunchedEffect(
        state.defaultCorrectionPicker,
        state.internalW3WAutoSuggestEditText,
        state.customCorrectionPicker,
        block = {
            if (state.defaultCorrectionPicker != null || state.customCorrectionPicker != null) {
                state.internalW3WAutoSuggestEditText?.customCorrectionPicker(
                    customCorrectionPicker = state.customCorrectionPicker
                        ?: state.defaultCorrectionPicker
                )
                if (state.customCorrectionPicker != null) state.defaultCorrectionPicker?.visibility =
                    View.GONE
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
        state.defaultSuggestionPicker,
        state.defaultInvalidAddressMessageView,
        state.internalW3WAutoSuggestEditText,
        state.customSuggestionPicker,
        block = {
            if (state.defaultSuggestionPicker != null || state.defaultInvalidAddressMessageView != null || state.customSuggestionPicker != null) {
                state.internalW3WAutoSuggestEditText?.onSuggestionSelected(
                    picker = state.customSuggestionPicker ?: state.defaultSuggestionPicker,
                    invalidAddressMessageView = state.defaultInvalidAddressMessageView
                ) {
                    onSuggestionWithCoordinates?.invoke(it)
                }
                if (state.customSuggestionPicker != null) state.defaultSuggestionPicker?.visibility =
                    View.GONE
            }
        })
}