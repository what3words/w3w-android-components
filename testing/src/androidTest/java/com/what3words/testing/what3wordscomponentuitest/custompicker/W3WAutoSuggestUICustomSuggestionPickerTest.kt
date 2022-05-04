package com.what3words.testing.what3wordscomponentuitest.custompicker


import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.what3words.testing.MainActivity
import com.what3words.testing.R
import com.what3words.testing.what3wordscomponentuitest.utils.hasItemCountGreaterThanZero
import org.hamcrest.CoreMatchers.containsString
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.what3words.testing.what3wordscomponentuitest.utils.waitUntilVisible
import org.hamcrest.CoreMatchers.not


@RunWith(AndroidJUnit4::class)
class W3WAutoSuggestUICustomSuggestionPickerTest {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testCustomSuggestionPickerIsVisible() {

        val threeWordAddress = "index.home.raft"

        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click(), typeTextIntoFocusedView(threeWordAddress))

        Espresso.onView(withId(R.id.w3wAutoSuggestDefaultPicker))
            .perform(waitUntilVisible(hasItemCountGreaterThanZero()))
            .check(matches(isDisplayed()))

        Espresso.onView(withId(R.id.btnClear))
            .perform(click())

        Espresso.onView(withId(R.id.checkboxCustomPicker))
            .perform(scrollTo(), click())

        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click(), typeTextIntoFocusedView(threeWordAddress))

        Espresso.onView(withId(R.id.w3wAutoSuggestDefaultPicker))
            .check(matches(not(isDisplayed())))

        Espresso.onView(withId(R.id.suggestionPicker))
            .perform(waitUntilVisible(hasItemCountGreaterThanZero()))
            .check(matches(isDisplayed()))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )

        Espresso.onView(withId(R.id.selectedInfo))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString(threeWordAddress))))

    }


}