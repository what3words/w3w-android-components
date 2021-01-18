[lib](../../index.md) / [com.what3words.autosuggest.picker](../index.md) / [SuggestionsAdapter](./index.md)

# SuggestionsAdapter

`class SuggestionsAdapter : Adapter<`[`W3WLocationViewHolder`](-w3-w-location-view-holder/index.md)`>`

### Types

| Name | Summary |
|---|---|
| [W3WLocationViewHolder](-w3-w-location-view-holder/index.md) | `class W3WLocationViewHolder : ViewHolder` |

### Constructors

| Name | Summary |
|---|---|
| [&lt;init&gt;](-init-.md) | `SuggestionsAdapter(typeface: `[`Typeface`](https://developer.android.com/reference/android/graphics/Typeface.html)`, textColor: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`, callback: (Suggestion) -> `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)`)` |

### Functions

| Name | Summary |
|---|---|
| [getItemCount](get-item-count.md) | `fun getItemCount(): `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html) |
| [onBindViewHolder](on-bind-view-holder.md) | `fun onBindViewHolder(holder: `[`W3WLocationViewHolder`](-w3-w-location-view-holder/index.md)`, position: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
| [onCreateViewHolder](on-create-view-holder.md) | `fun onCreateViewHolder(parent: `[`ViewGroup`](https://developer.android.com/reference/android/view/ViewGroup.html)`, viewType: `[`Int`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-int/index.html)`): `[`W3WLocationViewHolder`](-w3-w-location-view-holder/index.md) |
| [refreshSuggestions](refresh-suggestions.md) | `fun refreshSuggestions(suggestions: `[`List`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)`<Suggestion>, query: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`?): `[`Unit`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html) |
