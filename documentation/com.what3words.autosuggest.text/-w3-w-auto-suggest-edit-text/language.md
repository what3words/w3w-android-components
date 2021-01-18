[lib](../../index.md) / [com.what3words.autosuggest.text](../index.md) / [W3WAutoSuggestEditText](index.md) / [language](./language.md)

# language

`fun language(language: `[`String`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)`): `[`W3WAutoSuggestEditText`](index.md)

For normal text input, specifies a fallback language, which will help guide AutoSuggest if the input is particularly messy. If specified,
this parameter must be a supported 3 word address language as an ISO 639-1 2 letter code. For voice input (see voice section),
language must always be specified.

### Parameters

`language` - the fallback language

**Return**
same [W3WAutoSuggestEditText](index.md) instance

