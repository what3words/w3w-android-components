package com.what3words.testing.what3wordscomponentuitest.utils

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher


inline fun <reified T : View> isVisibleInParent(): Matcher<View> {
    return object : BoundedMatcher<View, T>(T::class.java) {
        override fun describeTo(description: Description?) {
            description?.appendText("${T::class.java.simpleName} is visible")
        }

        override fun matchesSafely(item: T): Boolean {
            return item.isVisible
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




