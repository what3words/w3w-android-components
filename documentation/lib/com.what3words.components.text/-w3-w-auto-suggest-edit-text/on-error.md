//[lib](../../../index.md)/[com.what3words.components.text](../index.md)/[W3WAutoSuggestEditText](index.md)/[onError](on-error.md)

# onError

[androidJvm]\
fun [onError](on-error.md)(errorView: [AppCompatTextView](https://developer.android.com/reference/kotlin/androidx/appcompat/widget/AppCompatTextView.html)? = null, errorCallback: [Consumer](https://developer.android.com/reference/kotlin/androidx/core/util/Consumer.html)<APIResponse.What3WordsError>): [W3WAutoSuggestEditText](index.md)

Will provide any errors APIResponse.What3WordsError that might happen during the API call

#### Return

same [W3WAutoSuggestEditText](index.md) instance

## Parameters

androidJvm

| | |
|---|---|
| errorView | set custom error view can be any [AppCompatTextView](https://developer.android.com/reference/kotlin/androidx/appcompat/widget/AppCompatTextView.html) or [W3WAutoSuggestErrorMessage](../../com.what3words.components.error/-w3-w-auto-suggest-error-message/index.md), default view will show below [W3WAutoSuggestEditText](index.md) (this will only show end-user error friendly message or message provided on [errorMessage](error-message.md)) |
| errorCallback | will return APIResponse.What3WordsError with information about the error occurred. |
