[lib](../../index.md) / [com.what3words.autosuggest.text](../index.md) / [W3WAutoSuggestEditText](index.md) / [onError](./on-error.md)

# onError

`fun onError(errorView: AppCompatTextView? = null, errorCallback: Consumer<What3WordsError>): `[`W3WAutoSuggestEditText`](index.md)

onError will provide any errors [APIResponse.What3WordsError](#) that might happen during the API call

### Parameters

`errorView` - set custom error view can be any [AppCompatTextView](#) or [W3WAutoSuggestErrorMessage](../../com.what3words.autosuggest.error/-w3-w-auto-suggest-error-message/index.md), default view will show below [W3WAutoSuggestEditText](index.md) (this will only show end-user error friendly message or message provided on [errorMessage](error-message.md))

`errorCallback` - will return [APIResponse.What3WordsError](#) with information about the error occurred.

**Return**
same [W3WAutoSuggestEditText](index.md) instance

