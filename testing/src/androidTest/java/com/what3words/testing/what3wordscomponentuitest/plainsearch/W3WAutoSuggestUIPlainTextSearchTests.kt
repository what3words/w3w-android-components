package com.what3words.testing.what3wordscomponentuitest.plainsearch

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.what3words.testing.MainActivity
import com.what3words.testing.R
import com.what3words.testing.what3wordscomponentuitest.utils.hasItemCountGreaterThanZero
import com.what3words.testing.what3wordscomponentuitest.utils.waitUntilVisible
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class W3WAutoSuggestUIPlainTextSearchTests {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testTextSearch_withoutFiltersOrCoordinates() {
        val threeWordAddress = "filled.count.soap"
        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(click(), replaceText(threeWordAddress), closeSoftKeyboard())


        Espresso.onView(
            withId(
                com.what3words.components.R.id.w3wAutoSuggestDefaultPicker
            )
        )
            .perform(waitUntilVisible(hasItemCountGreaterThanZero()))
            .perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click()
            )
        )

        Espresso.onView(withId(R.id.selectedInfo))
            .check(matches(withText(CoreMatchers.containsString(threeWordAddress))))

        Espresso.onView(withId(R.id.selectedInfo))
            .check(matches(withText(CoreMatchers.containsStringIgnoringCase("latitude: null"))))

        Espresso.onView(withId(R.id.selectedInfo))
            .check(matches(withText(CoreMatchers.containsStringIgnoringCase("longitude: null"))))

    }
}