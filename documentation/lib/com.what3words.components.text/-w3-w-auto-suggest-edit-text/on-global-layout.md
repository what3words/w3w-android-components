//[lib](../../../index.md)/[com.what3words.components.text](../index.md)/[W3WAutoSuggestEditText](index.md)/[onGlobalLayout](on-global-layout.md)

# onGlobalLayout

[androidJvm]\
open override fun [onGlobalLayout](on-global-layout.md)()

Since [W3WAutoSuggestEditText](index.md) have other views which depends on like [W3WAutoSuggestPicker](../../com.what3words.components.picker/-w3-w-auto-suggest-picker/index.md), [W3WAutoSuggestErrorMessage](../../com.what3words.components.error/-w3-w-auto-suggest-error-message/index.md), [W3WAutoSuggestCorrectionPicker](../../com.what3words.components.picker/-w3-w-auto-suggest-correction-picker/index.md) and multiple voiceScreenType's all of these have to be rendered after [W3WAutoSuggestEditText](index.md) is added to the getViewTreeObserver so we can use W3WAutoSuggestEditText.getX, W3WAutoSuggestEditText.getY, W3WAutoSuggestEditText.getWidth and W3WAutoSuggestEditText.getHeight to position the dependent views correctly and we want this to run only once hence why we use isRendered to check if all views have already been rendered (getViewTreeObserver.addOnGlobalLayoutListener can be called multiple times).

Another issue found is that if first time that [onGlobalLayout](on-global-layout.md) is called and W3WAutoSuggestEditText.getVisibility = [View.GONE](https://developer.android.com/reference/kotlin/android/view/View.html#gone) all W3WAutoSuggestEditText.getX, W3WAutoSuggestEditText.getY, W3WAutoSuggestEditText.getWidth and W3WAutoSuggestEditText.getHeight will be 0 which will be a problem when rendering/positioning the other views. The solution is to check if [W3WAutoSuggestEditText](index.md) is [View.VISIBLE](https://developer.android.com/reference/kotlin/android/view/View.html#visible) before setting isRendered = true and render all the dependent views correctly.
