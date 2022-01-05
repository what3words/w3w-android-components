//[lib](../../index.md)/[com.what3words.components.models](index.md)

# Package com.what3words.components.models

## Types

| Name | Summary |
|---|---|
| [AutosuggestApiManager](-autosuggest-api-manager/index.md) | [androidJvm]<br>class [AutosuggestApiManager](-autosuggest-api-manager/index.md)(**wrapper**: What3WordsV3) : [AutosuggestLogicManager](-autosuggest-logic-manager/index.md) |
| [AutosuggestLogicManager](-autosuggest-logic-manager/index.md) | [androidJvm]<br>interface [AutosuggestLogicManager](-autosuggest-logic-manager/index.md) |
| [DisplayUnits](-display-units/index.md) | [androidJvm]<br>enum [DisplayUnits](-display-units/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)<[DisplayUnits](-display-units/index.md)> |
| [Either](-either/index.md) | [androidJvm]<br>sealed class [Either](-either/index.md)<out [L](-either/index.md), out [R](-either/index.md)> |
| [VoiceApiAutosuggestManager](-voice-api-autosuggest-manager/index.md) | [androidJvm]<br>class [VoiceApiAutosuggestManager](-voice-api-autosuggest-manager/index.md)(**voiceBuilder**: VoiceBuilder) : [VoiceAutosuggestManager](-voice-autosuggest-manager/index.md) |
| [VoiceAutosuggestManager](-voice-autosuggest-manager/index.md) | [androidJvm]<br>interface [VoiceAutosuggestManager](-voice-autosuggest-manager/index.md) |
| [W3WListeningState](-w3-w-listening-state/index.md) | [androidJvm]<br>enum [W3WListeningState](-w3-w-listening-state/index.md) : [Enum](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-enum/index.html)<[W3WListeningState](-w3-w-listening-state/index.md)> |

## Functions

| Name | Summary |
|---|---|
| [c](c.md) | [androidJvm]<br>fun <[A](c.md), [B](c.md), [C](c.md)> ([A](c.md)) -> [B](c.md).[c](c.md)(f: ([B](c.md)) -> [C](c.md)): ([A](c.md)) -> [C](c.md)<br>Composes 2 functions See <a href="https://proandroiddev.com/kotlins-nothing-type-946de7d464fb">Credits to Alex Hart. |
| [flatMap](flat-map.md) | [androidJvm]<br>fun <[T](flat-map.md), [L](flat-map.md), [R](flat-map.md)> [Either](-either/index.md)<[L](flat-map.md), [R](flat-map.md)>.[flatMap](flat-map.md)(fn: ([R](flat-map.md)) -> [Either](-either/index.md)<[L](flat-map.md), [T](flat-map.md)>): [Either](-either/index.md)<[L](flat-map.md), [T](flat-map.md)><br>Right-biased flatMap() FP convention which means that Right is assumed to be the default case to operate on. |
| [getOrElse](get-or-else.md) | [androidJvm]<br>fun <[L](get-or-else.md), [R](get-or-else.md)> [Either](-either/index.md)<[L](get-or-else.md), [R](get-or-else.md)>.[getOrElse](get-or-else.md)(value: [R](get-or-else.md)): [R](get-or-else.md)<br>Returns the value from this Right or the given argument if this is a Left. |
| [map](map.md) | [androidJvm]<br>fun <[T](map.md), [L](map.md), [R](map.md)> [Either](-either/index.md)<[L](map.md), [R](map.md)>.[map](map.md)(fn: ([R](map.md)) -> [T](map.md)): [Either](-either/index.md)<[L](map.md), [T](map.md)><br>Right-biased map() FP convention which means that Right is assumed to be the default case to operate on. |
| [onFailure](on-failure.md) | [androidJvm]<br>fun <[L](on-failure.md), [R](on-failure.md)> [Either](-either/index.md)<[L](on-failure.md), [R](on-failure.md)>.[onFailure](on-failure.md)(fn: ([L](on-failure.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)): [Either](-either/index.md)<[L](on-failure.md), [R](on-failure.md)><br>Left-biased onFailure() FP convention dictates that when this class is Left, it'll perform the onFailure functionality passed as a parameter, but, overall will still return an either object so you chain calls. |
| [onSuccess](on-success.md) | [androidJvm]<br>fun <[L](on-success.md), [R](on-success.md)> [Either](-either/index.md)<[L](on-success.md), [R](on-success.md)>.[onSuccess](on-success.md)(fn: ([R](on-success.md)) -> [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html)): [Either](-either/index.md)<[L](on-success.md), [R](on-success.md)><br>Right-biased onSuccess() FP convention dictates that when this class is Right, it'll perform the onSuccess functionality passed as a parameter, but, overall will still return an either object so you chain calls. |
