package com.what3words.components.compose.wrapper

import android.graphics.drawable.Drawable
import androidx.appcompat.view.ContextThemeWrapper
import android.widget.FrameLayout
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import androidx.constraintlayout.compose.Dimension
import com.what3words.components.R
import com.what3words.components.compose.components.W3WAutoSuggestPicker
import com.what3words.components.compose.components.W3WCorrectionPicker
import com.what3words.components.compose.components.W3WErrorMessage
import com.what3words.components.compose.components.W3WInvalidAddressMessage
import com.what3words.components.compose.utils.AttachCorrectionPicker
import com.what3words.components.compose.utils.AttachErrorView
import com.what3words.components.compose.utils.AttachSuggestionPickerAndInvalidMessageView
import com.what3words.components.compose.utils.ConfigureAutoSuggest
import com.what3words.components.error.W3WAutoSuggestErrorMessage
import com.what3words.components.picker.W3WAutoSuggestCorrectionPicker
import com.what3words.components.text.W3WAutoSuggestEditText
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import java.time.format.TextStyle

/**
 * @param modifier Modifier to be applied to the W3WAutoSuggestEditText.
 * @param state the state object to be used to set-up the W3WAutoSuggestEditText.
 * @param ref Represents the [ConstrainedLayoutReference] that was used to constrain the [W3WAutoSuggestTextField] within a [ConstraintLayout].
 * @param onSuggestionWithCoordinates will return the [SuggestionWithCoordinates] picked by the end-user, coordinates will be null if returnCoordinates = false.
 * @param styles [W3WAutoSuggestTextFieldStyles] that will be used to resolve the styling of this W3WAutoSuggestTextField default components. See [W3WAutoSuggestTextFieldDefaults.styles].
 * @param micIcon drawable to use as Mic Icon
 * @param suggestionPicker instance of [W3WAutoSuggestPicker] to replace the default picker
 * @param errorView custom error view can be any [AppCompatTextView] or [W3WAutoSuggestErrorMessage], default view will show below [W3WAutoSuggestEditText] (this will only show end-user error friendly message or message provided on [W3WAutoSuggestEditText.errorMessage])
 * @param invalidAddressMessageView custom invalid address view can be any [AppCompatTextView] or [W3WAutoSuggestErrorMessage], default view will show below [W3WAutoSuggestEditText]
 * @param correctionPicker custom correct picker view.
 * @param onError will provide any errors [APIResponse.What3WordsError] that might happen during the API call
 * **/
@Composable
fun ConstraintLayoutScope.W3WAutoSuggestTextField(
    modifier: Modifier,
    state: W3WAutoSuggestTextFieldState,
    ref: ConstrainedLayoutReference,
    onSuggestionWithCoordinates: ((SuggestionWithCoordinates?) -> Unit),
    styles: W3WAutoSuggestTextFieldStyles = W3WAutoSuggestTextFieldDefaults.styles(),
    micIcon: Drawable? = null,
    suggestionPicker: com.what3words.components.picker.W3WAutoSuggestPicker? = null,
    errorView: AppCompatTextView? = null,
    invalidAddressMessageView: AppCompatTextView? = null,
    correctionPicker: W3WAutoSuggestCorrectionPicker? = null,
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
                state.micIcon = micIcon
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
                            type = state.voiceScreenTypeByDefault,
                            micIcon = micIcon
                        ).apply {
                            if (!state.defaultText.isNullOrEmpty()) this.setText(state.defaultText!!)
                        }
                addView(state.internalW3WAutoSuggestEditText)
            }
        })

    // suggestion picker
    val defaultPicker = createRef()
    if (suggestionPicker == null) {
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
    val defaultErrorView = createRef()
    if (errorView == null) {
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
    val defaultCorrectionPicker = createRef()
    if (correctionPicker == null) {
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
    val defaultInvalidAddressMessageView = createRef()
    if (invalidAddressMessageView == null) {
        W3WInvalidAddressMessage(state = state,
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

object W3WAutoSuggestTextFieldDefaults {

    fun styles(
        @StyleRes autoSuggestEditTextStyle: Int = R.style.W3WAutoSuggestEditTextTheme,
        @StyleRes autoSuggestPickerStyle: Int = R.style.W3WAutoSuggestPickerTheme,
        @StyleRes autoSuggestCorrectionPickerStyle: Int = R.style.W3WAutoSuggestCorrectionPickerTheme,
        @StyleRes autoSuggestErrorMessageStyle: Int = R.style.W3WAutoSuggestErrorMessage
    ): W3WAutoSuggestTextFieldStyles = DefaultW3WAutoSuggestTextFieldStyles(
        autoSuggestEditTextStyle = autoSuggestEditTextStyle,
        autoSuggestPickerStyle = autoSuggestPickerStyle,
        autoSuggestCorrectionPickerStyle = autoSuggestCorrectionPickerStyle,
        autoSuggestErrorMessageStyle = autoSuggestErrorMessageStyle
    )
}

@Immutable
private class DefaultW3WAutoSuggestTextFieldStyles(
    @StyleRes private val autoSuggestEditTextStyle: Int,
    @StyleRes private val autoSuggestPickerStyle: Int,
    @StyleRes private val autoSuggestCorrectionPickerStyle: Int,
    @StyleRes private val autoSuggestErrorMessageStyle: Int,
) : W3WAutoSuggestTextFieldStyles {
    override fun autoSuggestEditTextStyle(): Int {
        return autoSuggestEditTextStyle
    }

    override fun autoSuggestPickerStyle(): Int {
        return autoSuggestPickerStyle
    }

    override fun autoSuggestCorrectionPickerStyle(): Int {
        return autoSuggestCorrectionPickerStyle
    }

    override fun autoSuggestErrorMessageStyle(): Int {
        return autoSuggestErrorMessageStyle
    }
}


@Stable
interface W3WAutoSuggestTextFieldStyles {

    fun autoSuggestEditTextStyle(): Int

    fun autoSuggestPickerStyle(): Int

    fun autoSuggestCorrectionPickerStyle(): Int

    fun autoSuggestErrorMessageStyle(): Int
}