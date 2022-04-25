package com.what3words.testing


import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.material.snackbar.Snackbar
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.StringDescription


fun snackBarIsVisible(): Matcher<View> {
    return object : BoundedMatcher<View, ViewGroup>(ViewGroup::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText("snackbar is visible")
        }

        override fun matchesSafely(item: ViewGroup?): Boolean {
            if (item == null) return false
            var matched = false
            val children = item.children
            for (child in children) {
                matched = child is Snackbar.SnackbarLayout
                if (matched) break
            }
            return matched
        }
    }
}


fun hasItemCountGreaterThanZero(): Matcher<View> {
    return object : BoundedMatcher<View, RecyclerView>(RecyclerView::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText("has item count greater than 0")
        }

        override fun matchesSafely(item: RecyclerView?): Boolean {
            return if (item?.adapter?.itemCount == null) false
            else item.adapter!!.itemCount > 0
        }
    }
}

fun waitUntil(matcher: Matcher<View>): ViewAction {
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
                val callback = LayoutChangeCallback(matcher)
                try {
                    IdlingRegistry.getInstance().register(callback)
                    view.addOnLayoutChangeListener(callback)
                    uiController.loopMainThreadUntilIdle()
                } finally {
                    view.removeOnLayoutChangeListener(callback)
                    IdlingRegistry.getInstance().unregister(callback)
                }
            }
        }
    }
}

private class LayoutChangeCallback(private val matcher: Matcher<View>) : IdlingResource,
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
        matched = matcher.matches(v)
        callback?.onTransitionToIdle()
    }

}