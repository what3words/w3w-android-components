package com.what3words.components.compose.components

import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.what3words.components.R
import com.what3words.components.compose.wrapper.W3WAutoSuggestTextFieldState
import com.what3words.components.error.W3WAutoSuggestErrorMessage

@Composable
internal fun W3WErrorMessage(
    state: W3WAutoSuggestTextFieldState,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = {
            W3WAutoSuggestErrorMessage(
                ContextThemeWrapper(
                    it,
                    R.style.W3WAutoSuggestErrorMessage
                )
            )
        },
        modifier = modifier,
        update = {
            state.defaultErrorView = it
        }
    )
}