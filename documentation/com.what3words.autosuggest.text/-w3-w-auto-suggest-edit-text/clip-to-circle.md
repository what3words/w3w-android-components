[lib](../../index.md) / [com.what3words.autosuggest.text](../index.md) / [W3WAutoSuggestEditText](index.md) / [clipToCircle](./clip-to-circle.md)

# clipToCircle

`fun clipToCircle(centre: Coordinates?, radius: `[`Double`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)`?): `[`W3WAutoSuggestEditText`](index.md)

Restrict autosuggest results to a circle, specified by [Coordinates](#) representing the centre of the circle, plus the
radius in kilometres. For convenience, longitude is allowed to wrap around 180 degrees. For example 181 is equivalent to -179.

### Parameters

`centre` - the centre of the circle

`radius` - the radius of the circle in kilometres

**Return**
same [W3WAutoSuggestEditText](index.md) instance

