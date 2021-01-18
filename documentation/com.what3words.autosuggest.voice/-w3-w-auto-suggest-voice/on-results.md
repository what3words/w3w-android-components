[lib](../../index.md) / [com.what3words.autosuggest.voice](../index.md) / [W3WAutoSuggestVoice](index.md) / [onResults](./on-results.md)

# onResults

`fun onResults(callback: Consumer<`[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<`[`W3WSuggestion`](../-w3-w-suggestion/index.md)`>>): `[`W3WAutoSuggestVoice`](index.md)

onResults without [W3WAutoSuggestPicker](../../com.what3words.autosuggest.picker/-w3-w-auto-suggest-picker/index.md) will provide a list of 3 word addresses found using our voice API.

### Parameters

`callback` - will return a list of [W3WSuggestion](../-w3-w-suggestion/index.md).

**Return**
same [W3WAutoSuggestVoice](index.md) instance

`fun onResults(picker: `[`W3WAutoSuggestPicker`](../../com.what3words.autosuggest.picker/-w3-w-auto-suggest-picker/index.md)`, callback: Consumer<`[`W3WSuggestion`](../-w3-w-suggestion/index.md)`?>): `[`W3WAutoSuggestVoice`](index.md)

onResults with will provide the 3 word address selected by the end-user using the [W3WAutoSuggestPicker](../../com.what3words.autosuggest.picker/-w3-w-auto-suggest-picker/index.md) provided.

### Parameters

`picker` - [W3WAutoSuggestPicker](../../com.what3words.autosuggest.picker/-w3-w-auto-suggest-picker/index.md) to show on screen the list of 3 word addresses found using our voice API.

`callback` - will return the [W3WSuggestion](../-w3-w-suggestion/index.md) picked by the end-user.

**Return**
same [W3WAutoSuggestVoice](index.md) instance

