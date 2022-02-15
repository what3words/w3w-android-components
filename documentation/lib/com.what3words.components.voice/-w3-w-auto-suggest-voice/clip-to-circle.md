//[lib](../../../index.md)/[com.what3words.components.voice](../index.md)/[W3WAutoSuggestVoice](index.md)/[clipToCircle](clip-to-circle.md)

# clipToCircle

[androidJvm]\
fun [clipToCircle](clip-to-circle.md)(centre: Coordinates?, radius: [Double](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-double/index.html)?): [W3WAutoSuggestVoice](index.md)

Restrict autosuggest results to a circle, specified by Coordinates representing the centre of the circle, plus the [radius](clip-to-circle.md) in kilometres. For convenience, longitude is allowed to wrap around 180 degrees. For example 181 is equivalent to -179.

#### Return

same [W3WAutoSuggestVoice](index.md) instance

## Parameters

androidJvm

| | |
|---|---|
| centre | the centre of the circle |
| radius | the radius of the circle in kilometres |
