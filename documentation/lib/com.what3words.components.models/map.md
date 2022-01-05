//[lib](../../index.md)/[com.what3words.components.models](index.md)/[map](map.md)

# map

[androidJvm]\
fun <[T](map.md), [L](map.md), [R](map.md)> [Either](-either/index.md)<[L](map.md), [R](map.md)>.[map](map.md)(fn: ([R](map.md)) -> [T](map.md)): [Either](-either/index.md)<[L](map.md), [T](map.md)>

Right-biased map() FP convention which means that Right is assumed to be the default case to operate on. If it is Left, operations like map, flatMap, ... return the Left value unchanged.
