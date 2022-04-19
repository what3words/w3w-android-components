package com.what3words.testing

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.what3words.testing.utils.waitUntilViewShown
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class W3WAutoSuggestUIPlainTextSearch {

    @Rule
    @JvmField
    var mActivityTestRule: ActivityTestRule<MainActivity> =
        ActivityTestRule(MainActivity::class.java)

    @Test
    fun testTextSearch_withoutFiltersOrCoordinates() {
        Espresso.onView(withId(R.id.w3wAutoSuggestEditText))
            .perform(ViewActions.click())
            .perform(ViewActions.typeTextIntoFocusedView("filled.count.soap"))

        waitUntilViewShown(
            withId(
                com.what3words.components.R.id.w3wAutoSuggestDefaultPicker
            )
        )

        Espresso.onView(
            withId(
                com.what3words.components.R.id.w3wAutoSuggestDefaultPicker
            )
        ).perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                ViewActions.click()
            )
        )

        Espresso.onView(withId(R.id.w3wSuggestionInfo))
            .check(matches(withText("words: filled.count.soap\ncountry: GB\nnear: Bayswater, London\ndistance: N/A\nlatitude: null\nlongitude: null")))

        Thread.sleep(3000)
    }

}