//[lib](../../index.md)/[com.what3words.components.models](index.md)/[flatMap](flat-map.md)

# flatMap

[androidJvm]\
fun <[T](flat-map.md), [L](flat-map.md), [R](flat-map.md)> [Either](-either/index.md)<[L](flat-map.md), [R](flat-map.md)>.[flatMap](flat-map.md)(fn: ([R](flat-map.md)) -> [Either](-either/index.md)<[L](flat-map.md), [T](flat-map.md)>): [Either](-either/index.md)<[L](flat-map.md), [T](flat-map.md)>

Right-biased flatMap() FP convention which means that Right is assumed to be the default case to operate on. If it is Left, operations like map, flatMap, ... return the Left value unchanged.
