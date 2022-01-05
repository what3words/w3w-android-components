//[lib](../../index.md)/[com.what3words.components.models](index.md)/[onSuccess](on-success.md)

# onSuccess

[androidJvm]\
fun <[L](on-success.md), [R](on-success.md)> [Either](-either/index.md)<[L](on-success.md), [R](on-success.md)>.[onSuccess](on-success.md)(fn: ([R](on-success.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)): [Either](-either/index.md)<[L](on-success.md), [R](on-success.md)>

Right-biased onSuccess() FP convention dictates that when this class is Right, it'll perform the onSuccess functionality passed as a parameter, but, overall will still return an either object so you chain calls.
