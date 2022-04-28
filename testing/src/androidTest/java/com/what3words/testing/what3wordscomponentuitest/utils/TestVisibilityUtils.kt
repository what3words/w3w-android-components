package com.what3words.testing.what3wordscomponentuitest.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.StringDescription

fun isVisible(): Matcher<View> {
    return object : BoundedMatcher<View, View>(View::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText("${View::class.simpleName} is visible")
        }

        override fun matchesSafely(item: View): Boolean {
            return item.isVisible
        }
    }
}

fun waitUntilVisible(
    matcher: Matcher<View> = isVisible(),
    checkForChildren: Boolean = false
): ViewAction {
    return object : ViewAction {
        override fun getDescription(): String {
            val description = StringDescription()
            matcher.describeTo(description)
            return String.format("wait until: %s", description)
        }

        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(View::class.java)
        }

        override fun perform(uiController: UiController, view: View) {
            if (!matcher.matches(view)) {
                val idlingResource = LayoutChangeCallback(matcher, checkForChildren)
                try {
                    IdlingRegistry.getInstance().register(idlingResource)
                    if (checkForChildren) (view.parent as ViewGroup).addOnLayoutChangeListener(
                        idlingResource
                    )
                    else view.addOnLayoutChangeListener(idlingResource)
                    uiController.loopMainThreadUntilIdle()
                } finally {
                    IdlingRegistry.getInstance().unregister(idlingResource)
                }
            }
        }
    }
}

class LayoutChangeCallback(
    private val matcher: Matcher<View>,
    private val checkForChildren: Boolean
) :
    IdlingResource,
    View.OnLayoutChangeListener {
    private var callback: IdlingResource.ResourceCallback? = null
    private var matched = false

    override fun getName(): String {
        return "Layout change callback"
    }

    override fun isIdleNow(): Boolean {
        return matched
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
    }

    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        if (checkForChildren) {
            (v as ViewGroup).children.forEach { child ->
                if (matcher.matches(child)) {
                    matched = true
                    callback?.onTransitionToIdle()
                }
            }
        } else {
            matched = true
            callback?.onTransitionToIdle()
        }
    }

}