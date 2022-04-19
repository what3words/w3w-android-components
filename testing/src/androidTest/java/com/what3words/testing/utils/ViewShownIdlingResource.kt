package com.what3words.testing.utils

import android.view.View
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingResource.ResourceCallback
import androidx.test.espresso.ViewFinder
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import org.hamcrest.Matcher


class ViewShownIdlingResource(private val viewMatcher: Matcher<View>) :
    IdlingResource {
    private var resourceCallback: ResourceCallback? = null
    override fun isIdleNow(): Boolean {
        val view = getView(viewMatcher)
        val idle = view == null || view.isShown
        if (idle && resourceCallback != null) {
            resourceCallback!!.onTransitionToIdle()
        }
        return idle
    }

    override fun registerIdleTransitionCallback(resourceCallback: ResourceCallback) {
        this.resourceCallback = resourceCallback
    }

    override fun getName(): String {
        return this.toString() + viewMatcher.toString()
    }

    companion object {
        private val TAG = ViewShownIdlingResource::class.java.simpleName
        private fun getView(viewMatcher: Matcher<View>): View? {
            return try {
                val viewInteraction = onView(viewMatcher)
                val finderField = viewInteraction.javaClass.getDeclaredField("viewFinder")
                finderField.isAccessible = true
                val finder = finderField[viewInteraction] as ViewFinder
                finder.view
            } catch (e: Exception) {
                null
            }
        }
    }
}

fun waitUntilViewShown(matcher: Matcher<View>) {
    val idlingResource: IdlingResource = ViewShownIdlingResource(matcher)
    try {
        IdlingRegistry.getInstance().register(idlingResource)
        onView(matcher).check(matches(isDisplayed()))
    } finally {
        IdlingRegistry.getInstance().unregister(idlingResource)
    }
}