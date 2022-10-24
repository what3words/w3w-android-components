package com.what3words.compose.sample.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.what3words.components.compose.wrapper.W3WAutoSuggestTextFieldState
import com.what3words.compose.sample.R
import com.what3words.compose.sample.ui.components.LabelCheckBox
import com.what3words.compose.sample.ui.components.MultiLabelTextField
import com.what3words.compose.sample.ui.components.RadioGroup
import com.what3words.compose.sample.ui.components.RadioGroupState
import com.what3words.compose.sample.ui.model.VoiceOption
import com.what3words.javawrapper.request.AutosuggestOptions
import com.what3words.javawrapper.request.Coordinates


class CustomizeAutoSuggestSettingsScreenState() {
    // use custom picker
    var useCustomPicker: Boolean by mutableStateOf(value = false)

    var clipToCountry: String by mutableStateOf(value = "")

    var focus: String by mutableStateOf(value = "")
}

// a group of ui components that is used to configure the w3w autosuggestion settings
@Composable
fun CustomizeAutoSuggestSettingsScreen(
    autoSuggestTextFieldState: W3WAutoSuggestTextFieldState,
    state: CustomizeAutoSuggestSettingsScreenState,
    modifier: Modifier = Modifier
) {
    val autoSuggestOptions = remember {
        (AutosuggestOptions().apply {
            preferLand = true
        })
    }

    LaunchedEffect(key1 = true, block = {
        autoSuggestTextFieldState.options(options = autoSuggestOptions)
    })

    Column(modifier = modifier.fillMaxWidth()) {
        // customize the autosuggest component label
        Text(
            text = stringResource(id = R.string.txt_label_customize_the_autosuggest_component),
            style = MaterialTheme.typography.body2.copy(color = LocalContentColor.current.copy(alpha = 0.5f))
        )

        // return coordinates checkbox
        LabelCheckBox(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            checked = autoSuggestTextFieldState.returnCoordinates,
            onCheckedChange = {
                autoSuggestTextFieldState.returnCoordinates = it
            },
            text = stringResource(id = R.string.txt_label_return_coordinates)
        )

        // prefer land checkbox
        var preferLand by remember { mutableStateOf(value = false) }
        LabelCheckBox(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            checked = preferLand,
            onCheckedChange = {
                preferLand = it
                autoSuggestOptions.preferLand = preferLand
                autoSuggestTextFieldState.options(options = autoSuggestOptions)
            },
            text = stringResource(id = R.string.txt_label_prefer_land)
        )

        // voice options radio group state
        val voiceOptionGroupState = remember {
            RadioGroupState(
                items = VoiceOption.values(),
                selectedItemIndex = 0,
                label = {
                    it.label
                }
            )
        }

        /**
         * observe the selected radio item [RadioGroupState.selected] so that you can update the [W3WAutoSuggestTextFieldState] accordingly
         *  **/
        LaunchedEffect(key1 = voiceOptionGroupState.selected, block = {
            if (voiceOptionGroupState.selected != null) {
                if (voiceOptionGroupState.selected == VoiceOption.DisableVoice) {
                    autoSuggestTextFieldState.voiceEnabled(enabled = false)
                } else {
                    autoSuggestTextFieldState.voiceEnabled(
                        enabled = true,
                        type = voiceOptionGroupState.selected!!.type!!
                    )
                }
            }
        })

        // voice option radio groupd
        RadioGroup(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            state = voiceOptionGroupState
        )

        // use custom picker
        LabelCheckBox(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            checked = state.useCustomPicker,
            onCheckedChange = {
                state.useCustomPicker = it
            },
            text = stringResource(id = R.string.txt_label_use_custom_picker)
        )

        // allow invalid 3wa
        LabelCheckBox(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            checked = autoSuggestTextFieldState.allowInvalid3wa,
            onCheckedChange = {
                autoSuggestTextFieldState.allowInvalid3wa = it
            },
            text = stringResource(id = R.string.txt_label_allow_invalid_3wa)
        )

        // allow flexible delimiters
        LabelCheckBox(
            modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.small_50)),
            checked = autoSuggestTextFieldState.allowFlexibleDelimiters,
            onCheckedChange = {
                autoSuggestTextFieldState.allowFlexibleDelimiters = it
            },
            text = stringResource(id = R.string.txt_label_allow_flexible_delimiters)
        )

        // focus
        MultiLabelTextField(
            text = state.focus,
            onTextChanged = {
                state.focus = it
                val latLong =
                    state.focus.replace("\\s".toRegex(), "").split(",").filter { it.isNotEmpty() }
                val lat = latLong.getOrNull(0)?.toDoubleOrNull()
                val long = latLong.getOrNull(1)?.toDoubleOrNull()
                if (lat != null && long != null) {
                    autoSuggestOptions.focus = Coordinates(lat, long)
                } else {
                    autoSuggestOptions.focus = null
                }
                autoSuggestTextFieldState.options(options = autoSuggestOptions)
            },
            primaryLabel = stringResource(id = R.string.txt_label_focus),
            secondaryLabel = stringResource(id = R.string.txt_label_focus_info)
        )

        // clip to country 
        MultiLabelTextField(
            text = state.clipToCountry,
            onTextChanged = {
                state.clipToCountry = it
                autoSuggestOptions.clipToCountry =
                    state.clipToCountry.replace("\\s".toRegex(), "").split(",")
                        .filter { it.isNotEmpty() }
                autoSuggestTextFieldState.options(options = autoSuggestOptions)
            },
            primaryLabel = stringResource(id = R.string.txt_label_clip_to_country),
            secondaryLabel = stringResource(id = R.string.txt_label_clip_to_country_info)
        )

    }
}