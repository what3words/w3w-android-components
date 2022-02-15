//[lib](../../../index.md)/[com.what3words.components.text](../index.md)/[W3WAutoSuggestEditText](index.md)/[clipToPolygon](clip-to-polygon.md)

# clipToPolygon

[androidJvm]\
fun [clipToPolygon](clip-to-polygon.md)(polygon: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<Coordinates>): [W3WAutoSuggestEditText](index.md)

Restrict autosuggest results to a polygon, specified by a collection of Coordinates. The polygon should be closed, i.e. the first element should be repeated as the last element; also the list should contain at least 4 entries. The API is currently limited to accepting up to 25 pairs.

#### Return

same [W3WAutoSuggestEditText](index.md) instance

## Parameters

androidJvm

| | |
|---|---|
| polygon | the polygon to clip results too |
