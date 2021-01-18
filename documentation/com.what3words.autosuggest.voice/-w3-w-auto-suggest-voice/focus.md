[lib](../../index.md) / [com.what3words.autosuggest.voice](../index.md) / [W3WAutoSuggestVoice](index.md) / [focus](./focus.md)

# focus

`fun focus(coordinates: Coordinates?): `[`W3WAutoSuggestVoice`](index.md)

This is a location [Coordinates](#), specified as a latitude (often where the user making the query is). If specified, the results will be weighted to
give preference to those near the focus. For convenience, longitude is allowed to wrap around the 180 line, so 361 is equivalent to 1.

### Parameters

`coordinates` - the focus to use

**Return**
same [W3WAutoSuggestVoice](index.md) instance

