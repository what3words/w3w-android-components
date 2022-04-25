package com.what3words.testing.what3wordscomponentuitest.clipToCircle

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
import com.what3words.testing.waitUntilViewShown
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.what3words.testing.hasItemCountGreaterThanZero
import com.what3words.testing.waitUntil


@RunWith(AndroidJUnit4::class)
class W3WAutoSuggestUIClipToCircleTests {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    private val circle = "55.136930, -4.288321, 680.0"

    @Test
    fun testTextSearch_clipToCircleContainAddressInsideCircle() {
        val threeWordAddress = "falters.curtains.point"
        waitUntilViewShown(
            withId(R.id.main)
        )
        Espresso.onView(withId(R.id.textClipToCircle))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())
            .perform(typeTextIntoFocusedView(circle))

        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())
            .perform(typeTextIntoFocusedView(threeWordAddress))


        Espresso.onView(
            withId(
                com.what3words.components.R.id.w3wAutoSuggestDefaultPicker
            )
        )
            .perform(waitUntil(hasItemCountGreaterThanZero()))
            .perform(
                RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                    0,
                    click()
                )
            )

        Espresso.onView(withId(R.id.selectedInfo))
            .check(matches(withText(containsString(threeWordAddress))))


        Thread.sleep(2000)
    }

    @Test
    fun testTextSearch_clipToCircleDoesNotContainAddressOutsideCircle() {
        val threeWordAddress = "jazz.silver.bagels"
        waitUntilViewShown(
            withId(R.id.main)
        )

        Espresso.onView(withId(R.id.textClipToCircle))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())
            .perform(typeTextIntoFocusedView(circle))

        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click())
            .perform(typeTextIntoFocusedView(threeWordAddress))

        Espresso.onView(
            withId(
                com.what3words.components.R.id.w3wAutoSuggestDefaultPicker
            )
        )
            .perform(waitUntil(hasItemCountGreaterThanZero()))
            .perform(
            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(
                0,
                click()
            )
        )

        Espresso.onView(withId(R.id.selectedInfo))
            .check(matches(withText(not(containsString(threeWordAddress)))))
    }
}