package com.what3words.components.compose.components

import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.what3words.components.compose.wrapper.W3WAutoSuggestTextFieldState
import com.what3words.components.error.W3WAutoSuggestErrorMessage

@Composable
internal fun W3WInvalidAddressMessage(
    state: W3WAutoSuggestTextFieldState,
    @StyleRes themeResId: Int,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = {
            W3WAutoSuggestErrorMessage(
                ContextThemeWrapper(
                    it,
                    themeResId
                )
            )
        }, modifier = modifier,
        update = {
            state.defaultInvalidAddressMessageView = it
        }
    )
}