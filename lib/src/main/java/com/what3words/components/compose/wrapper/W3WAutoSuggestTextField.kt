package com.what3words.components.compose.wrapper

import android.graphics.drawable.Drawable
import androidx.appcompat.view.ContextThemeWrapper
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
import com.what3words.components.R
import com.what3words.components.error.W3WAutoSuggestErrorMessage
import com.what3words.components.picker.W3WAutoSuggestCorrectionPicker
import com.what3words.components.picker.W3WAutoSuggestPicker
import com.what3words.components.text.W3WAutoSuggestEditText
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.SuggestionWithCoordinates

/**
 * @param modifier Modifier to be applied to the W3WAutoSuggestEditText.
 * @param state the state object to be used to set-up the W3WAutoSuggestEditText.
 * @param ref Represents the [ConstrainedLayoutReference] that was used to constrain the [W3WAutoSuggestTextField] within a [ConstraintLayout].
 * @param micIcon drawable to use as Mic Icon
 * @param suggestionPicker instance of [W3WAutoSuggestPicker] to replace the default picker
 * @param errorView custom error view can be any [AppCompatTextView] or [W3WAutoSuggestErrorMessage], default view will show below [W3WAutoSuggestEditText] (this will only show end-user error friendly message or message provided on [W3WAutoSuggestEditText.errorMessage])
 * @param invalidAddressMessageView custom invalid address view can be any [AppCompatTextView] or [W3WAutoSuggestErrorMessage], default view will show below [W3WAutoSuggestEditText]
 * @param correctionPicker custom correct picker view.
 * @param onSuggestionWithCoordinates will return the [SuggestionWithCoordinates] picked by the end-user, coordinates will be null if returnCoordinates = false.
 * @param onError will provide any errors [APIResponse.What3WordsError] that might happen during the API call
 * **/
@Composable
fun ConstraintLayoutScope.W3WAutoSuggestTextField(
    modifier: Modifier,
    state: W3WAutoSuggestTextFieldState,
    ref: ConstrainedLayoutReference,
    micIcon: Drawable? = null,
    suggestionPicker: W3WAutoSuggestPicker? = null,
    errorView: AppCompatTextView? = null,
    invalidAddressMessageView: AppCompatTextView? = null,
    correctionPicker: W3WAutoSuggestCorrectionPicker? = null,
    onSuggestionWithCoordinates: ((SuggestionWithCoordinates?) -> Unit)? = null,
    onError: ((APIResponse.What3WordsError) -> Unit)? = null,
) {
    // setUp auto suggest functions
    ConfigureAutoSuggest(state = state)

    // attach auto suggest edit-text dependent components
    AttachSuggestionPickerAndInvalidMessageView(
        state = state,
        onSuggestionWithCoordinates = onSuggestionWithCoordinates
    )
    AttachErrorView(state = state, onError = onError)
    AttachCorrectionPicker(state = state)


    // w3w auto suggest edit text
    AndroidView(modifier = modifier
        .fillMaxWidth(),
        factory = {
            FrameLayout(it).apply {
                state.internalW3WAutoSuggestEditText =
                    W3WAutoSuggestEditText(
                        ContextThemeWrapper(
                            it,
                            R.style.W3WAutoSuggestEditTextTheme
                        )
                    )
                        .apiKey(key = state.apiKey)
                        .voiceEnabled(
                            enabled = state.voiceEnabledByDefault,
                            type = state.voiceScreenType,
                            micIcon = micIcon
                        ).apply {
                            if (!state.defaultText.isNullOrEmpty()) this.setText(state.defaultText!!)
                        }
                addView(state.internalW3WAutoSuggestEditText)
            }
        })

    // suggestion picker
    if (suggestionPicker == null) {
        val defaultPicker = createRef()
        W3WAutoSuggestPicker(
            state = state,
            modifier = Modifier
                .zIndex(zIndex = Float.MAX_VALUE)
                .constrainAs(defaultPicker) {
                    linkTo(ref.start, ref.end)
                    top.linkTo(ref.bottom)
                    width = Dimension.fillToConstraints
                },
        )
    } else {
        state.defaultSuggestionPicker = suggestionPicker
    }

    // error view
    if (errorView == null) {
        val defaultErrorView = createRef()
        W3WErrorMessage(state = state,
            modifier = Modifier
                .zIndex(zIndex = Float.MAX_VALUE)
                .constrainAs(defaultErrorView) {
                    linkTo(ref.start, ref.end)
                    top.linkTo(ref.bottom)
                    width = Dimension.fillToConstraints
                })
    } else {
        state.defaultErrorView = errorView
    }

    // correction picker
    if (correctionPicker == null) {
        val defaultCorrectionPicker = createRef()
        W3WCorrectionPicker(state = state,
            modifier = Modifier
                .zIndex(zIndex = Float.MAX_VALUE)
                .constrainAs(defaultCorrectionPicker) {
                    linkTo(ref.start, ref.end)
                    top.linkTo(ref.bottom)
                    width = Dimension.fillToConstraints
                })
    } else {
        state.defaultCorrectionPicker = correctionPicker
    }

    // invalid address message view
    if (invalidAddressMessageView == null) {
        val defaultInvalidAddressMessageView = createRef()
        W3WInvalidAddressMessageView(state = state,
            modifier = Modifier
                .zIndex(Float.MAX_VALUE)
                .constrainAs(defaultInvalidAddressMessageView) {
                    linkTo(ref.start, ref.end)
                    top.linkTo(ref.bottom)
                    width = Dimension.fillToConstraints
                })
    } else {
        state.defaultInvalidAddressMessageView = invalidAddressMessageView
    }
}
