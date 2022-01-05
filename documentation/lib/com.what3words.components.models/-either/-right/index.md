//[lib](../../../../index.md)/[com.what3words.components.models](../../index.md)/[Either](../index.md)/[Right](index.md)

# Right

[androidJvm]\
data class [Right](index.md)<out [R](index.md)>(**b**: [R](index.md)) : [Either](../index.md)<[Nothing](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-nothing/index.html), [R](index.md)> 

<ul><li>Represents the right side of [Either](../index.md) class which by convention is a "Success".</li></ul>

## Constructors

| | |
|---|---|
| [Right](-right.md) | [androidJvm]<br>fun <out [R](index.md)> [Right](-right.md)(b: [R](index.md)) |

## Functions

| Name | Summary |
|---|---|
| [fold](index.md#-208810787%2FFunctions%2F-1973928616) | [androidJvm]<br>fun [fold](index.md#-208810787%2FFunctions%2F-1973928616)(fnL: ([Nothing](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-nothing/index.html)) -> [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), fnR: ([R](index.md)) -> [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)): [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)<br>Applies fnL if this is a Left or fnR if this is a Right. |
| [left](../left.md) | [androidJvm]<br>fun <[L](../left.md)> [left](../left.md)(a: [L](../left.md)): [Either.Left](../-left/index.md)<[L](../left.md)><br>Creates a Left type. |
| [right](../right.md) | [androidJvm]<br>fun <[R](../right.md)> [right](../right.md)(b: [R](../right.md)): [Either.Right](index.md)<[R](../right.md)><br>Creates a Left type. |

## Properties

| Name | Summary |
|---|---|
| [b](b.md) | [androidJvm]<br>val [b](b.md): [R](index.md) |
| [isLeft](index.md#-1673259772%2FProperties%2F-1973928616) | [androidJvm]<br>val [isLeft](index.md#-1673259772%2FProperties%2F-1973928616): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Returns true if this is a Left, false otherwise. |
| [isRight](index.md#901962611%2FProperties%2F-1973928616) | [androidJvm]<br>val [isRight](index.md#901962611%2FProperties%2F-1973928616): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Returns true if this is a Right, false otherwise. |
