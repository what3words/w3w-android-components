//[lib](../../../index.md)/[com.what3words.components.text](../index.md)/[W3WAutoSuggestEditText](index.md)/[onSelected](on-selected.md)

# onSelected

[androidJvm]\
fun [onSelected](on-selected.md)(picker: [W3WAutoSuggestPicker](../../com.what3words.components.picker/-w3-w-auto-suggest-picker/index.md)? = null, invalidAddressMessageView: [AppCompatTextView](https://developer.android.com/reference/kotlin/androidx/appcompat/widget/AppCompatTextView.html)? = null, callback: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<SuggestionWithCoordinates?>): [W3WAutoSuggestEditText](index.md)

Will provide the user selected 3 word address, if user selects an invalid 3 word address SuggestionWithCoordinates will be null.

#### Return

same [W3WAutoSuggestEditText](index.md) instance

## Parameters

androidJvm

| | |
|---|---|
| picker | set custom 3 word address picker view [W3WAutoSuggestPicker](../../com.what3words.components.picker/-w3-w-auto-suggest-picker/index.md), default picker will show below [W3WAutoSuggestEditText](index.md) |
| invalidAddressMessageView | set custom invalid address view can be any [AppCompatTextView](https://developer.android.com/reference/kotlin/androidx/appcompat/widget/AppCompatTextView.html) or [W3WAutoSuggestErrorMessage](../../com.what3words.components.error/-w3-w-auto-suggest-error-message/index.md), default view will show below [W3WAutoSuggestEditText](index.md) |
| callback | will return the SuggestionWithCoordinates picked by the end-user, coordinates will be null if returnCoordinates = false. |
