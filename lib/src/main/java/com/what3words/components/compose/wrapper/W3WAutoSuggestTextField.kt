package com.what3words.components.compose.wrapper

import android.graphics.drawable.Drawable
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
import androidx.core.util.Consumer
import com.what3words.components.R
import com.what3words.components.compose.components.W3WAutoSuggestPicker
import com.what3words.components.compose.components.W3WCorrectionPicker
import com.what3words.components.compose.components.W3WErrorMessage
import com.what3words.components.compose.components.W3WInvalidAddressMessage
import com.what3words.components.compose.utils.AttachCorrectionPicker
import com.what3words.components.compose.utils.AttachErrorView
import com.what3words.components.compose.utils.AttachSuggestionPickerAndInvalidMessageView
import com.what3words.components.compose.utils.ConfigureAutoSuggest
import com.what3words.components.compose.utils.createW3WAutoSuggestEditText
import com.what3words.components.error.W3WAutoSuggestErrorMessage
import com.what3words.components.models.AutosuggestLogicManager
import com.what3words.components.picker.W3WAutoSuggestCorrectionPicker
import com.what3words.components.text.W3WAutoSuggestEditText
import com.what3words.javawrapper.response.APIResponse
import com.what3words.javawrapper.response.SuggestionWithCoordinates

/**
 * @param modifier Modifier to be applied to the W3WAutoSuggestEditText.
 * @param ref Represents the [ConstrainedLayoutReference] that was used to constrain the [W3WAutoSuggestTextField] within a [ConstraintLayout].
 * @param onSuggestionWithCoordinates will return the [SuggestionWithCoordinates] picked by the end-user, coordinates will be null if returnCoordinates = false.
 * @param state the state object to be used to set-up the W3WAutoSuggestEditText.
 * @param themes [W3WAutoSuggestTextFieldThemes] that will be used to resolve the styling of this W3WAutoSuggestTextField default components. See [W3WAutoSuggestTextFieldDefaults.themes].
 * @param micIcon drawable to use as Mic Icon
 * @param suggestionPicker instance of [W3WAutoSuggestPicker] to replace the default picker
 * @param errorView custom error view can be any [AppCompatTextView] or [W3WAutoSuggestErrorMessage], default view will show below [W3WAutoSuggestEditText] (this will only show end-user error friendly message or message provided on [W3WAutoSuggestEditText.errorMessage])
 * @param invalidAddressMessageView custom invalid address view can be any [AppCompatTextView] or [W3WAutoSuggestErrorMessage], default view will show below [W3WAutoSuggestEditText]
 * @param correctionPicker custom correct picker view.
 * @param onError will provide any errors [APIResponse.What3WordsError] that might happen during the API call
 * @param onHomeClick see [W3WAutoSuggestEditText.onHomeClick]
 * @param onDisplaySuggestions see [W3WAutoSuggestEditText.onDisplaySuggestions]
 * @param onW3WAutoSuggestEditTextReady callback that exposes [W3WAutoSuggestTextFieldState.internalW3WAutoSuggestEditText] for direct use
 * **/
@Composable
fun ConstraintLayoutScope.W3WAutoSuggestTextField(
    modifier: Modifier,
    ref: ConstrainedLayoutReference,
    configuration: AutoSuggestConfiguration,
    onSuggestionWithCoordinates: ((SuggestionWithCoordinates?) -> Unit),
    state: W3WAutoSuggestTextFieldState = rememberW3WAutoSuggestTextFieldState(),
    themes: W3WAutoSuggestTextFieldThemes = W3WAutoSuggestTextFieldDefaults.themes(),
    micIcon: Drawable? = null,
    suggestionPicker: com.what3words.components.picker.W3WAutoSuggestPicker? = null,
    errorView: AppCompatTextView? = null,
    invalidAddressMessageView: AppCompatTextView? = null,
    correctionPicker: W3WAutoSuggestCorrectionPicker? = null,
    onError: ((APIResponse.What3WordsError) -> Unit)? = null,
    onHomeClick: (() -> Unit)? = null,
    onDisplaySuggestions: (Consumer<Boolean>)? = null,
    onW3WAutoSuggestEditTextReady: ((W3WAutoSuggestEditText) -> Unit)? = null
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
                // first instantiate the internal W3WAutoSuggestEditText before adding it to the frame layout
                state.internalW3WAutoSuggestEditText = state.createW3WAutoSuggestEditText(
                    context = it,
                    themeResId = themes.autoSuggestEditTextTheme(),
                    autoSuggestConfiguration = configuration
                ).apply {
                    onHomeClick?.let { onHomeClick(onHomeClickCallback = onHomeClick) }
                    onDisplaySuggestions?.let { onDisplaySuggestions(onDisplaySuggestions) }
                }

                addView(state.internalW3WAutoSuggestEditText)
                onW3WAutoSuggestEditTextReady?.invoke(state.internalW3WAutoSuggestEditText!!)
            }
        })

    // suggestion picker
    val defaultPicker = createRef()
    if (suggestionPicker == null) {
        W3WAutoSuggestPicker(
            state = state,
            style = themes.autoSuggestPickerTheme(),
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
            themeResId = themes.autoSuggestErrorMessageTheme(),
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
            style = themes.autoSuggestCorrectionPickerTheme(),
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
            themeResId = themes.autoSuggestInvalidAddressMessageTheme(),
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

    fun themes(
        @StyleRes autoSuggestEditTextTheme: Int = R.style.W3WAutoSuggestEditTextTheme,
        @StyleRes autoSuggestPickerStyleTheme: Int = R.style.W3WAutoSuggestPickerTheme,
        @StyleRes autoSuggestCorrectionPickerStyle: Int = R.style.W3WAutoSuggestCorrectionPickerTheme,
        @StyleRes autoSuggestErrorMessageTheme: Int = R.style.W3WAutoSuggestErrorMessage,
        @StyleRes autoSuggestInvalidAddressMessageTheme: Int = R.style.W3WAutoSuggestErrorMessage
    ): W3WAutoSuggestTextFieldThemes = DefaultW3WAutoSuggestTextFieldThemes(
        autoSuggestEditTextTheme = autoSuggestEditTextTheme,
        autoSuggestPickerTheme = autoSuggestPickerStyleTheme,
        autoSuggestCorrectionPickerTheme = autoSuggestCorrectionPickerStyle,
        autoSuggestErrorMessageTheme = autoSuggestErrorMessageTheme,
        autoSuggestInvalidAddressMessageTheme = autoSuggestInvalidAddressMessageTheme
    )
}

@Immutable
private class DefaultW3WAutoSuggestTextFieldThemes(
    @StyleRes private val autoSuggestEditTextTheme: Int,
    @StyleRes private val autoSuggestPickerTheme: Int,
    @StyleRes private val autoSuggestCorrectionPickerTheme: Int,
    @StyleRes private val autoSuggestErrorMessageTheme: Int,
    @StyleRes private val autoSuggestInvalidAddressMessageTheme: Int,
) : W3WAutoSuggestTextFieldThemes {
    override fun autoSuggestEditTextTheme(): Int {
        return autoSuggestEditTextTheme
    }

    override fun autoSuggestPickerTheme(): Int {
        return autoSuggestPickerTheme
    }

    override fun autoSuggestCorrectionPickerTheme(): Int {
        return autoSuggestCorrectionPickerTheme
    }

    override fun autoSuggestErrorMessageTheme(): Int {
        return autoSuggestErrorMessageTheme
    }

    override fun autoSuggestInvalidAddressMessageTheme(): Int {
        return autoSuggestInvalidAddressMessageTheme
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DefaultW3WAutoSuggestTextFieldThemes

        if (autoSuggestEditTextTheme != other.autoSuggestEditTextTheme) return false
        if (autoSuggestPickerTheme != other.autoSuggestPickerTheme) return false
        if (autoSuggestCorrectionPickerTheme != other.autoSuggestCorrectionPickerTheme) return false
        if (autoSuggestErrorMessageTheme != other.autoSuggestErrorMessageTheme) return false
        if (autoSuggestInvalidAddressMessageTheme != other.autoSuggestInvalidAddressMessageTheme) return false

        return true
    }

    override fun hashCode(): Int {
        var result = autoSuggestEditTextTheme.hashCode()
        result = 31 * result + autoSuggestPickerTheme.hashCode()
        result = 31 * result + autoSuggestCorrectionPickerTheme.hashCode()
        result = 31 * result + autoSuggestErrorMessageTheme.hashCode()
        result = 31 * result + autoSuggestInvalidAddressMessageTheme.hashCode()
        return result
    }
}


@Stable
interface W3WAutoSuggestTextFieldThemes {

    /**
     * returns the resource ID of the theme to be applied on the [W3WAutoSuggestEditText] used in the [W3WAutoSuggestTextField]
     * **/
    fun autoSuggestEditTextTheme(): Int

    /**
     * returns the resource ID of the theme to be applied on the default [W3WAutoSuggestPicker] used in the [W3WAutoSuggestTextField]
     * **/
    fun autoSuggestPickerTheme(): Int

    /**
     * returns the resource ID of the theme to be applied on the default [W3WAutoSuggestCorrectionPicker] used in the [W3WAutoSuggestTextField]
     * **/
    fun autoSuggestCorrectionPickerTheme(): Int

    /**
     * returns the resource ID of the theme to be applied on the default [W3WAutoSuggestErrorMessage] used in the [W3WAutoSuggestTextField]
     * **/
    fun autoSuggestErrorMessageTheme(): Int

    /**
     * returns the resource ID of the theme to be applied on the default [W3WAutoSuggestErrorMessage] used as an InvalidAddress View in the [W3WAutoSuggestTextField]
     * **/
    fun autoSuggestInvalidAddressMessageTheme(): Int
}