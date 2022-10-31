package com.what3words.components.compose.components

import androidx.annotation.StyleRes
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
    @StyleRes style: Int,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = {
            W3WAutoSuggestPicker(
                ContextThemeWrapper(
                    it,
                    style
                )
            )
        }, modifier = modifier,
        update = {
            state.defaultSuggestionPicker = it
        })
}