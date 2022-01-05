//[lib](../../index.md)/[com.what3words.components.models](index.md)/[getOrElse](get-or-else.md)

# getOrElse

[androidJvm]\
fun <[L](get-or-else.md), [R](get-or-else.md)> [Either](-either/index.md)<[L](get-or-else.md), [R](get-or-else.md)>.[getOrElse](get-or-else.md)(value: [R](get-or-else.md)): [R](get-or-else.md)

Returns the value from this Right or the given argument if this is a Left. Right(12).getOrElse(17) RETURNS 12 and Left(12).getOrElse(17) RETURNS 17
