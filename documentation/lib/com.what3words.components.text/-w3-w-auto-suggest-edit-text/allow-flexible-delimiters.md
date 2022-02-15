//[lib](../../../index.md)/[com.what3words.components.text](../index.md)/[W3WAutoSuggestEditText](index.md)/[allowFlexibleDelimiters](allow-flexible-delimiters.md)

# allowFlexibleDelimiters

[androidJvm]\
fun [allowFlexibleDelimiters](allow-flexible-delimiters.md)(isAllowed: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)): [W3WAutoSuggestEditText](index.md)

Allow EditText to accept different delimiters than the what3words standard full stop "index.home.raft". By default [allowFlexibleDelimiters](allow-flexible-delimiters.md) is false, when you type an existing three word address with a different delimiter (i.e "index home raft") will trigger our Did You Mean feature, but if you set [allowFlexibleDelimiters](allow-flexible-delimiters.md) (true) "index home raft" will be parsed to "index.home.raft" and will return the [nResults](n-results.md) suggestions for that query.

#### Return

same [W3WAutoSuggestEditText](index.md) instance

## Parameters

androidJvm

| | |
|---|---|
| isAllowed | if true [W3WAutoSuggestEditText](index.md) will accept flexible delimiters and show suggestions, if false will not accept flexible delimiters but if is that three word address exist will show the did you mean feature. |
