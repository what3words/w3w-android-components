package com.what3words.components.compose.components

import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.what3words.components.R
import com.what3words.components.compose.wrapper.W3WAutoSuggestTextFieldState
import com.what3words.components.picker.W3WAutoSuggestPicker

@Composable
internal fun W3WAutoSuggestPicker(
    state: W3WAutoSuggestTextFieldState,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = {
            W3WAutoSuggestPicker(
                ContextThemeWrapper(
                    it,
                    R.style.W3WAutoSuggestPickerTheme
                )
            )
        }, modifier = modifier,
        update = {
            state.defaultSuggestionPicker = it
        })
}