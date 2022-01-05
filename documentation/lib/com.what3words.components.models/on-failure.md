//[lib](../../index.md)/[com.what3words.components.models](index.md)/[onFailure](on-failure.md)

# onFailure

[androidJvm]\
fun <[L](on-failure.md), [R](on-failure.md)> [Either](-either/index.md)<[L](on-failure.md), [R](on-failure.md)>.[onFailure](on-failure.md)(fn: ([L](on-failure.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)): [Either](-either/index.md)<[L](on-failure.md), [R](on-failure.md)>

Left-biased onFailure() FP convention dictates that when this class is Left, it'll perform the onFailure functionality passed as a parameter, but, overall will still return an either object so you chain calls.
