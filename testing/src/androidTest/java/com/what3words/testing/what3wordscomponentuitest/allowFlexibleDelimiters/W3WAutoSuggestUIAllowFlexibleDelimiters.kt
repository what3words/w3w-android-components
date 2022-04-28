package com.what3words.testing.what3wordscomponentuitest.allowFlexibleDelimiters


import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.what3words.testing.MainActivity
import com.what3words.testing.R
import com.what3words.testing.hasItemCountGreaterThanZero
import com.what3words.testing.what3wordscomponentuitest.utils.isVisible
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.what3words.testing.what3wordscomponentuitest.utils.waitUntilVisible
import org.hamcrest.CoreMatchers.containsStringIgnoringCase
import org.hamcrest.CoreMatchers.not


@RunWith(AndroidJUnit4::class)
class W3WAutoSuggestUIAllowFlexibleDelimiters {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)


    @Test
    fun testTextSearch_allowFlexibleDelimiters() {
        val spaceSeparatedThreeWordAddress = "index home raft"
        val correctThreeWordsAddress = "index.home.raft"
        Espresso.onView(withId(R.id.main))
            .perform(waitUntilVisible())


        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click(), typeTextIntoFocusedView(spaceSeparatedThreeWordAddress))

        Espresso.onView(withId(R.id.correctionPicker))
            .perform(waitUntilVisible(checkForChildren = true))


        Espresso.onView(withId(R.id.btnClear))
            .perform(click())

        Espresso.onView(withId(R.id.checkboxAllowFlexibleDelimiters))
            .perform(scrollTo(), click())

        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click(), typeTextIntoFocusedView(spaceSeparatedThreeWordAddress))

        Espresso.onView(withId(R.id.correctionPicker))
            .check(matches(not(isVisible())))

        Espresso.onView(withId(R.id.w3wAutoSuggestDefaultPicker))
            .perform(waitUntilVisible(hasItemCountGreaterThanZero()))
            .check(matches(isDisplayed()))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0, click()
                )
            )

        Espresso.onView(withId(R.id.selectedInfo))
            .check(matches(withText(containsStringIgnoringCase(correctThreeWordsAddress))))

    }

}