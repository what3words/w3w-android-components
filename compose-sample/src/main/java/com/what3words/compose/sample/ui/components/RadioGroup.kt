package com.what3words.compose.sample.ui.components


import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import kotlin.math.max

class RadioGroupState(val items: List<String>, selectedItemIndex: Int) {
    var selected: String? by mutableStateOf(if (items.isNotEmpty()) items[selectedItemIndex] else null)
}

@Composable
fun RadioGroup(
    state: RadioGroupState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        state.items.forEach { item: String ->
            RadioGroupItem(selected = item == state.selected, onClick = {
                state.selected = item
            }, label = item)
        }
    }
}

@Composable
private fun RadioGroupItem(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        val interactionSource = remember {
            MutableInteractionSource()
        }
        RadioButton(
            selected = selected, onClick = onClick, interactionSource = interactionSource,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colors.primary)
        )
        Text(
            text = label,
            modifier = Modifier.clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                onClick()
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}