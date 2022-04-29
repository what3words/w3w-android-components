package com.what3words.testing.what3wordscomponentuitest.utils

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import com.google.android.material.snackbar.Snackbar
import com.what3words.components.picker.W3WAutoSuggestCorrectionPicker
import org.hamcrest.Description
import org.hamcrest.Matcher



inline fun <reified T : View> isVisibleInParentMatcher(): Matcher<View> {
    return object : BoundedMatcher<View, T>(T::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText("${T::class.java.simpleName} is visible")
        }

        override fun matchesSafely(item: T): Boolean {
            return item.isVisible
        }
    }
}

fun isVisibleMatcher(): Matcher<View> {
    return object : BoundedMatcher<View, View>(View::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText("${View::class.java.simpleName} is visible")
        }

        override fun matchesSafely(item: View?): Boolean {
            return item?.isVisible ?: false
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




