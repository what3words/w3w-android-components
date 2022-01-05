//[lib](../../../index.md)/[com.what3words.components.models](../index.md)/[AutosuggestLogicManager](index.md)

# AutosuggestLogicManager

[androidJvm]\
interface [AutosuggestLogicManager](index.md)

## Functions

| Name | Summary |
|---|---|
| [autosuggest](autosuggest.md) | [androidJvm]<br>abstract suspend fun [autosuggest](autosuggest.md)(microphone: Microphone, options: AutosuggestOptions, voiceLanguage: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [Either](../-either/index.md)<APIResponse.What3WordsError, [VoiceAutosuggestManager](../-voice-autosuggest-manager/index.md)><br>abstract suspend fun [autosuggest](autosuggest.md)(query: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), options: AutosuggestOptions?, allowFlexibleDelimiters: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) = false): [Either](../-either/index.md)<APIResponse.What3WordsError, [Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)<[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<Suggestion>?, Suggestion?>> |
| [isVoiceEnabled](is-voice-enabled.md) | [androidJvm]<br>abstract fun [isVoiceEnabled](is-voice-enabled.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [multipleWithCoordinates](multiple-with-coordinates.md) | [androidJvm]<br>abstract suspend fun [multipleWithCoordinates](multiple-with-coordinates.md)(rawQuery: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), suggestions: [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<Suggestion>): [Either](../-either/index.md)<APIResponse.What3WordsError, [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<SuggestionWithCoordinates>> |
| [selected](selected.md) | [androidJvm]<br>abstract suspend fun [selected](selected.md)(rawQuery: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), suggestion: Suggestion): [Either](../-either/index.md)<APIResponse.What3WordsError, SuggestionWithCoordinates> |
| [selectedWithCoordinates](selected-with-coordinates.md) | [androidJvm]<br>abstract suspend fun [selectedWithCoordinates](selected-with-coordinates.md)(rawQuery: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), suggestion: Suggestion): [Either](../-either/index.md)<APIResponse.What3WordsError, SuggestionWithCoordinates> |

## Inheritors

| Name |
|---|
| [AutosuggestApiManager](../-autosuggest-api-manager/index.md) |
