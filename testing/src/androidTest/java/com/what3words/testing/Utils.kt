package com.what3words.testing


import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers
import com.google.android.material.snackbar.Snackbar
import com.what3words.components.picker.W3WAutoSuggestCorrectionPicker
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.StringDescription
import kotlin.coroutines.CoroutineContext


fun snackBarIsVisible(): Matcher<View> {
    return object : BoundedMatcher<View, View>(View::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText("snackbar is visible")
        }

        override fun matchesSafely(item: View): Boolean {
            return item is Snackbar.SnackbarLayout && item.isVisible
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

