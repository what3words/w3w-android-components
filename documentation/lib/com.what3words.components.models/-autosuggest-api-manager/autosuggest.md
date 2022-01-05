//[lib](../../../index.md)/[com.what3words.components.models](../index.md)/[AutosuggestApiManager](index.md)/[autosuggest](autosuggest.md)

# autosuggest

[androidJvm]\
open suspend override fun [autosuggest](autosuggest.md)(query: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), options: AutosuggestOptions?, allowFlexibleDelimiters: [Boolean](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-boolean/index.html)): [Either](../-either/index.md)<APIResponse.What3WordsError, [Pair](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/index.html)<[List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)<Suggestion>?, Suggestion?>>

open suspend override fun [autosuggest](autosuggest.md)(microphone: Microphone, options: AutosuggestOptions, voiceLanguage: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [Either](../-either/index.md)<APIResponse.What3WordsError, [VoiceAutosuggestManager](../-voice-autosuggest-manager/index.md)>
