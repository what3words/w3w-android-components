[lib](../../index.md) / [com.what3words.autosuggest.text](../index.md) / [W3WAutoSuggestEditText](index.md) / [onSelected](./on-selected.md)

# onSelected

`fun onSelected(picker: `[`W3WAutoSuggestPicker`](../../com.what3words.autosuggest.picker/-w3-w-auto-suggest-picker/index.md)`? = null, invalidAddressMessageView: AppCompatTextView? = null, callback: Consumer<`[`W3WSuggestion`](../../com.what3words.autosuggest.voice/-w3-w-suggestion/index.md)`?>): `[`W3WAutoSuggestEditText`](index.md)

onSelected will provide the user selected 3 word address, if user selects an invalid 3 word address [W3WSuggestion](../../com.what3words.autosuggest.voice/-w3-w-suggestion/index.md) will be null.

### Parameters

`picker` - set custom 3 word address picker view [W3WAutoSuggestPicker](../../com.what3words.autosuggest.picker/-w3-w-auto-suggest-picker/index.md), default picker will show below [W3WAutoSuggestEditText](index.md)

`invalidAddressMessageView` - set custom invalid address view can be any [AppCompatTextView](#) or [W3WAutoSuggestErrorMessage](../../com.what3words.autosuggest.error/-w3-w-auto-suggest-error-message/index.md), default view will show below [W3WAutoSuggestEditText](index.md)

`callback` - will return [W3WSuggestion](../../com.what3words.autosuggest.voice/-w3-w-suggestion/index.md) selected by the user.

**Return**
same [W3WAutoSuggestEditText](index.md) instance

