//[lib](../../../index.md)/[com.what3words.components.models](../index.md)/[VoiceAutosuggestManager](index.md)

# VoiceAutosuggestManager

[androidJvm]\
interface [VoiceAutosuggestManager](index.md)

## Functions

| Name | Summary |
|---|---|
| [isListening](is-listening.md) | [androidJvm]<br>abstract fun [isListening](is-listening.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [startListening](start-listening.md) | [androidJvm]<br>abstract suspend fun [startListening](start-listening.md)(): [Either](../-either/index.md)<APIResponse.What3WordsError, [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<Suggestion>> |
| [stopListening](stop-listening.md) | [androidJvm]<br>abstract fun [stopListening](stop-listening.md)() |
| [updateOptions](update-options.md) | [androidJvm]<br>abstract fun [updateOptions](update-options.md)(options: AutosuggestOptions) |

## Inheritors

| Name |
|---|
| [VoiceApiAutosuggestManager](../-voice-api-autosuggest-manager/index.md) |
