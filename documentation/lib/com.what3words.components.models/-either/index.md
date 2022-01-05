//[lib](../../../index.md)/[com.what3words.components.models](../index.md)/[Either](index.md)

# Either

[androidJvm]\
sealed class [Either](index.md)<out [L](index.md), out [R](index.md)>

## Types

| Name | Summary |
|---|---|
| [Left](-left/index.md) | [androidJvm]<br>data class [Left](-left/index.md)<out [L](-left/index.md)>(**a**: [L](-left/index.md)) : [Either](index.md)<[L](-left/index.md), [Nothing](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-nothing/index.html)> <br><ul><li>Represents the left side of [Either](index.md) class which by convention is a "Failure".</li></ul> |
| [Right](-right/index.md) | [androidJvm]<br>data class [Right](-right/index.md)<out [R](-right/index.md)>(**b**: [R](-right/index.md)) : [Either](index.md)<[Nothing](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-nothing/index.html), [R](-right/index.md)> <br><ul><li>Represents the right side of [Either](index.md) class which by convention is a "Success".</li></ul> |

## Functions

| Name | Summary |
|---|---|
| [fold](fold.md) | [androidJvm]<br>fun [fold](fold.md)(fnL: ([L](index.md)) -> [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), fnR: ([R](index.md)) -> [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)): [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)<br>Applies fnL if this is a Left or fnR if this is a Right. |
| [left](left.md) | [androidJvm]<br>fun <[L](left.md)> [left](left.md)(a: [L](left.md)): [Either.Left](-left/index.md)<[L](left.md)><br>Creates a Left type. |
| [right](right.md) | [androidJvm]<br>fun <[R](right.md)> [right](right.md)(b: [R](right.md)): [Either.Right](-right/index.md)<[R](right.md)><br>Creates a Left type. |

## Properties

| Name | Summary |
|---|---|
| [isLeft](is-left.md) | [androidJvm]<br>val [isLeft](is-left.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Returns true if this is a Left, false otherwise. |
| [isRight](is-right.md) | [androidJvm]<br>val [isRight](is-right.md): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Returns true if this is a Right, false otherwise. |

## Inheritors

| Name |
|---|
| [Either](-left/index.md) |
| [Either](-right/index.md) |

## Extensions

| Name | Summary |
|---|---|
| [flatMap](../flat-map.md) | [androidJvm]<br>fun <[T](../flat-map.md), [L](../flat-map.md), [R](../flat-map.md)> [Either](index.md)<[L](../flat-map.md), [R](../flat-map.md)>.[flatMap](../flat-map.md)(fn: ([R](../flat-map.md)) -> [Either](index.md)<[L](../flat-map.md), [T](../flat-map.md)>): [Either](index.md)<[L](../flat-map.md), [T](../flat-map.md)><br>Right-biased flatMap() FP convention which means that Right is assumed to be the default case to operate on. |
| [getOrElse](../get-or-else.md) | [androidJvm]<br>fun <[L](../get-or-else.md), [R](../get-or-else.md)> [Either](index.md)<[L](../get-or-else.md), [R](../get-or-else.md)>.[getOrElse](../get-or-else.md)(value: [R](../get-or-else.md)): [R](../get-or-else.md)<br>Returns the value from this Right or the given argument if this is a Left. |
| [map](../map.md) | [androidJvm]<br>fun <[T](../map.md), [L](../map.md), [R](../map.md)> [Either](index.md)<[L](../map.md), [R](../map.md)>.[map](../map.md)(fn: ([R](../map.md)) -> [T](../map.md)): [Either](index.md)<[L](../map.md), [T](../map.md)><br>Right-biased map() FP convention which means that Right is assumed to be the default case to operate on. |
| [onFailure](../on-failure.md) | [androidJvm]<br>fun <[L](../on-failure.md), [R](../on-failure.md)> [Either](index.md)<[L](../on-failure.md), [R](../on-failure.md)>.[onFailure](../on-failure.md)(fn: ([L](../on-failure.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)): [Either](index.md)<[L](../on-failure.md), [R](../on-failure.md)><br>Left-biased onFailure() FP convention dictates that when this class is Left, it'll perform the onFailure functionality passed as a parameter, but, overall will still return an either object so you chain calls. |
| [onSuccess](../on-success.md) | [androidJvm]<br>fun <[L](../on-success.md), [R](../on-success.md)> [Either](index.md)<[L](../on-success.md), [R](../on-success.md)>.[onSuccess](../on-success.md)(fn: ([R](../on-success.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)): [Either](index.md)<[L](../on-success.md), [R](../on-success.md)><br>Right-biased onSuccess() FP convention dictates that when this class is Right, it'll perform the onSuccess functionality passed as a parameter, but, overall will still return an either object so you chain calls. |
