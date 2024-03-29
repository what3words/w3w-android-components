package com.what3words.testing.what3wordscomponentuitest.clipToCircle

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.what3words.testing.MainActivity
import com.what3words.testing.R
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.what3words.testing.what3wordscomponentuitest.utils.hasItemCountGreaterThanZero
import com.what3words.testing.what3wordscomponentuitest.utils.waitUntilVisible


@RunWith(AndroidJUnit4::class)
class W3WAutoSuggestUIClipToCircleTests {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    private val circle = "55.136930, -4.288321, 680.0"

    @Test
    fun testTextSearch_clipToCircleContainAddressInsideCircle() {
        val threeWordAddress = "falters.curtains.point"

        Espresso.onView(withId(R.id.main))
            .perform(waitUntilVisible(), closeSoftKeyboard())

        Espresso.onView(withId(R.id.holderClipToCircle))
            .perform(waitUntilVisible(), scrollTo())

        Espresso.onView(withId(R.id.textClipToCircle))
            .perform(
                click(), typeTextIntoFocusedView(circle),
                closeSoftKeyboard()
            )

        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(
                waitUntilVisible(),
                scrollTo(),
                click(),
                replaceText(threeWordAddress),
                closeSoftKeyboard()
            )

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
            .check(matches(withText(containsString(threeWordAddress))))

    }

    @Test
    fun testTextSearch_clipToCircleDoesNotContainAddressOutsideCircle() {
        val threeWordAddress = "jazz.silver.bagels"
        Espresso.onView(withId(R.id.main))
            .perform(waitUntilVisible(), closeSoftKeyboard())

        Espresso.onView(withId(R.id.holderClipToCircle))
            .perform(waitUntilVisible(), scrollTo())

        Espresso.onView(withId(R.id.textClipToCircle))
            .perform(waitUntilVisible(), click(), typeTextIntoFocusedView(circle))

        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(
                waitUntilVisible(),
                scrollTo(),
                click(),
                replaceText(threeWordAddress),
                closeSoftKeyboard()
            )

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
            .check(matches(withText(not(containsString(threeWordAddress)))))
    }

}