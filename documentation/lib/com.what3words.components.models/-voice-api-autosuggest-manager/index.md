//[lib](../../../index.md)/[com.what3words.components.models](../index.md)/[VoiceApiAutosuggestManager](index.md)

# VoiceApiAutosuggestManager

[androidJvm]\
class [VoiceApiAutosuggestManager](index.md)(**voiceBuilder**: VoiceBuilder) : [VoiceAutosuggestManager](../-voice-autosuggest-manager/index.md)

## Functions

| Name | Summary |
|---|---|
| [isListening](is-listening.md) | [androidJvm]<br>open override fun [isListening](is-listening.md)(): [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html) |
| [startListening](start-listening.md) | [androidJvm]<br>open suspend override fun [startListening](start-listening.md)(): [Either](../-either/index.md)<APIResponse.What3WordsError, [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<Suggestion>> |
| [stopListening](stop-listening.md) | [androidJvm]<br>open override fun [stopListening](stop-listening.md)() |
| [updateOptions](update-options.md) | [androidJvm]<br>open override fun [updateOptions](update-options.md)(options: AutosuggestOptions) |
