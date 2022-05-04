package com.what3words.testing.what3wordscomponentuitest.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.test.espresso.IdlingPolicies
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingResourceTimeoutException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import org.hamcrest.Matcher
import org.hamcrest.StringDescription
import java.util.concurrent.TimeUnit


inline fun <reified T : View> waitUntilVisibleInParent(
    matcher: Matcher<View> = isVisibleInParent<T>()
): ViewAction {
    return object : ViewAction {
        override fun getDescription(): String {
            val description = StringDescription()
            matcher.describeTo(description)
            return String.format("wait until is visible in parent: %s", description)
        }

        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(View::class.java)
        }

        override fun perform(uiController: UiController?, view: View?) {
            IdlingPolicies.setIdlingResourceTimeout(1, TimeUnit.MINUTES)
            IdlingPolicies.setMasterPolicyTimeout(1, TimeUnit.MINUTES)
            var hasMatchedMatcher = false
            var hasMatchedId = false

            for (child in (view as ViewGroup).children) {
                if (child.id == view.id) hasMatchedId = true
                if (matcher.matches(child)) hasMatchedMatcher = true
                if (hasMatchedId && hasMatchedMatcher) {
                    return
                }
            }

            var idlingResourceCallback: IdlingResource.ResourceCallback? = null
            val idlingResource = object : IdlingResource {
                override fun getName(): String {
                    return "Parent layout change listener"
                }

                override fun isIdleNow(): Boolean {
                    return hasMatchedId && hasMatchedMatcher
                }

                override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
                    idlingResourceCallback = callback
                }
            }
            try {
                IdlingRegistry.getInstance().register(idlingResource)
                (view?.parent as ViewGroup).addOnLayoutChangeListener { parent, _, _, _, _, _, _, _, _ ->
                    for (child in (parent as ViewGroup).children) {
                        if (child.id == view.id) hasMatchedId = true
                        if (matcher.matches(child)) hasMatchedMatcher = true
                        if (hasMatchedId && hasMatchedMatcher) {
                            idlingResourceCallback?.onTransitionToIdle()
                            break
                        }
                    }
                }
                uiController?.loopMainThreadUntilIdle()
            } finally {
                IdlingRegistry.getInstance().unregister(idlingResource)
            }
        }
    }
}

fun waitUntilGone(delay: Long): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(View::class.java)
        }

        override fun getDescription(): String {
            return "wait for  $delay + milliseconds"
        }

        override fun perform(uiController: UiController, view: View) {
            assertThat(view, withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE))
            uiController.loopMainThreadForAtLeast(delay)
            assertThat(view, withEffectiveVisibility(ViewMatchers.Visibility.GONE))
        }
    }
}


fun waitUntilVisible(
    matcher: Matcher<View> = withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)
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
            IdlingPolicies.setIdlingResourceTimeout(1, TimeUnit.MINUTES)
            IdlingPolicies.setMasterPolicyTimeout(1, TimeUnit.MINUTES)
            if (!matcher.matches(view)) {
                var matched = false
                var idlingResourceCallback: IdlingResource.ResourceCallback? = null
                val idlingResource = object : IdlingResource {
                    override fun getName(): String {
                        return "Layout out change listener"
                    }

                    override fun isIdleNow(): Boolean {
                        return matched
                    }

                    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
                        idlingResourceCallback = callback
                    }
                }
                try {
                    IdlingRegistry.getInstance().register(idlingResource)
                    view.addOnLayoutChangeListener { mView, _, _, _, _, _, _, _, _ ->
                        if (matcher.matches(mView)) {
                            matched = true
                            idlingResourceCallback?.onTransitionToIdle()
                        }
                    }
                    uiController.loopMainThreadUntilIdle()
                } finally {
                    IdlingRegistry.getInstance().unregister(idlingResource)
                }
            }
        }
    }
}


