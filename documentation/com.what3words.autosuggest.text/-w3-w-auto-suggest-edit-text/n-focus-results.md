[lib](../../index.md) / [com.what3words.autosuggest.text](../index.md) / [W3WAutoSuggestEditText](index.md) / [nFocusResults](./n-focus-results.md)

# nFocusResults

`fun nFocusResults(n: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`?): `[`W3WAutoSuggestEditText`](index.md)

Specifies the number of results (must be &amp;lt;= nResults) within the results set which will have a focus. Defaults to nResults.
This allows you to run autosuggest with a mix of focussed and unfocussed results, to give you a "blend" of the two. This is exactly what the old V2
standardblend did, and standardblend behaviour can easily be replicated by passing nFocusResults(1)
which will return just one focussed result and the rest unfocussed.

### Parameters

`n` - number of results within the results set which will have a focus

**Return**
same [W3WAutoSuggestEditText](index.md) instance

