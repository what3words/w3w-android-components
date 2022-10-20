package com.what3words.compose.sample.ui.screen

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.what3words.components.compose.wrapper.W3WAutoSuggestTextField
import com.what3words.components.compose.wrapper.rememberW3WAutoSuggestTextFieldState
import com.what3words.components.text.W3WAutoSuggestEditText
import com.what3words.compose.sample.BuildConfig
import com.what3words.compose.sample.R

/**
 * A jetpack compose screen that's intends to be replica of the W3WAutoSuggestComponent Sample(view-specific) app.
 * Using the [W3WAutoSuggestTextField] as the jetpack compose's substitute for [W3WAutoSuggestEditText]
 * **/
@Composable
fun W3WTextFieldInConstraintLayoutScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedInfo: MutableState<String?> = remember { mutableStateOf(null) }

    ConstraintLayout(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(horizontal = dimensionResource(id = R.dimen.normal_100))
    ) {
        val (headerTxt, w3wTextField, selectedInfoTxt, settingsColumn) = createRefs()

        // welcome header
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = stringResource(R.string.txt_label_welcome_to_autosuggest_sample),
                modifier = Modifier
                    .padding(
                        top = dimensionResource(id = R.dimen.normal_125),
                        bottom = dimensionResource(
                            id = R.dimen.small_25
                        )
                    )
                    .constrainAs(headerTxt) {
                        linkTo(start = parent.start, end = parent.end)
                        top.linkTo(anchor = parent.top)
                        width = Dimension.fillToConstraints
                    },
                style = MaterialTheme.typography.body2
            )
        }

        //  what3words autosuggest text component for compose
        val w3wTextFieldState = rememberW3WAutoSuggestTextFieldState(apiKey = BuildConfig.W3W_API_KEY)

        W3WAutoSuggestTextField(
            modifier = Modifier.constrainAs(w3wTextField) {
                linkTo(start = parent.start, end = parent.end)
                top.linkTo(anchor = headerTxt.bottom)
            },
            state = w3wTextFieldState,
            ref = w3wTextField,
            onSuggestionWithCoordinates = {
                if (it != null) {
                    selectedInfo.value = context.resources.getString(
                        R.string.suggestion_info_placeholder,
                        it.words,
                        it.country,
                        it.nearestPlace,
                        if (it.distanceToFocusKm == null) "N/A" else "${it.distanceToFocusKm}km",
                        it.coordinates?.lat.toString(),
                        it.coordinates?.lng.toString()
                    )
                } else {
                    selectedInfo.value = ""
                }
            }
        )

        // selected address info text
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = selectedInfo.value ?: "",
                modifier = Modifier
                    .defaultMinSize(minHeight = dimensionResource(id = R.dimen.large_200))
                    .constrainAs(selectedInfoTxt) {
                        linkTo(start = parent.start, end = parent.end)
                        top.linkTo(w3wTextField.bottom)
                        width = Dimension.fillToConstraints
                    },
                fontWeight = FontWeight.Bold
            )
        }

        // group of components to configure the settings of the auto suggest edit text
        CustomizeAutoSuggestSettingsScreen(
            autoSuggestTextFieldState = w3wTextFieldState,
            modifier = Modifier
                .padding(top = dimensionResource(id = R.dimen.normal_200))
                .constrainAs(settingsColumn) {
                    linkTo(start = parent.start, end = parent.end)
                    linkTo(top = selectedInfoTxt.bottom, bottom = parent.bottom)
                    height = Dimension.fillToConstraints
                }
        )
    }
}
