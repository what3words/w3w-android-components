//[lib](../../../../index.md)/[com.what3words.components.models](../../index.md)/[Either](../index.md)/[Left](index.md)

# Left

[androidJvm]\
data class [Left](index.md)<out [L](index.md)>(**a**: [L](index.md)) : [Either](../index.md)<[L](index.md), [Nothing](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-nothing/index.html)> 

<ul><li>Represents the left side of [Either](../index.md) class which by convention is a "Failure".</li></ul>

## Constructors

| | |
|---|---|
| [Left](-left.md) | [androidJvm]<br>fun <out [L](index.md)> [Left](-left.md)(a: [L](index.md)) |

## Functions

| Name | Summary |
|---|---|
| [fold](index.md#-24557023%2FFunctions%2F-1973928616) | [androidJvm]<br>fun [fold](index.md#-24557023%2FFunctions%2F-1973928616)(fnL: ([L](index.md)) -> [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html), fnR: ([Nothing](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-nothing/index.html)) -> [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)): [Any](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-any/index.html)<br>Applies fnL if this is a Left or fnR if this is a Right. |
| [left](../left.md) | [androidJvm]<br>fun <[L](../left.md)> [left](../left.md)(a: [L](../left.md)): [Either.Left](index.md)<[L](../left.md)><br>Creates a Left type. |
| [right](../right.md) | [androidJvm]<br>fun <[R](../right.md)> [right](../right.md)(b: [R](../right.md)): [Either.Right](../-right/index.md)<[R](../right.md)><br>Creates a Left type. |

## Properties

| Name | Summary |
|---|---|
| [a](a.md) | [androidJvm]<br>val [a](a.md): [L](index.md) |
| [isLeft](index.md#-1775459143%2FProperties%2F-1973928616) | [androidJvm]<br>val [isLeft](index.md#-1775459143%2FProperties%2F-1973928616): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Returns true if this is a Left, false otherwise. |
| [isRight](index.md#2028749406%2FProperties%2F-1973928616) | [androidJvm]<br>val [isRight](index.md#2028749406%2FProperties%2F-1973928616): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)<br>Returns true if this is a Right, false otherwise. |
