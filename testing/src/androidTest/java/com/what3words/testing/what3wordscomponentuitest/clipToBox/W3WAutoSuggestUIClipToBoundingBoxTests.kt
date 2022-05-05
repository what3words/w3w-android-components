package com.what3words.testing.what3wordscomponentuitest.clipToBox

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeTextIntoFocusedView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasChildCount
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.what3words.testing.MainActivity
import com.what3words.testing.R
import com.what3words.testing.what3wordscomponentuitest.utils.hasItemCountGreaterThanZero
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.CoreMatchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.what3words.testing.what3wordscomponentuitest.utils.waitUntilVisible


@RunWith(AndroidJUnit4::class)
class W3WAutoSuggestUIClipToBoundingBoxTests {

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule(MainActivity::class.java)

    private val boundingBox = "49.180803, -8.001330, 58.470001, 2.158991"

    @Test
    fun testTextSearch_clipToBoundingBoxContainAddressInsideBox() {

        Espresso.onView(withId(R.id.textClipToBoundingBox))
            .perform(scrollTo(), waitUntilVisible(), click(), typeTextIntoFocusedView(boundingBox))

        val threeWordAddress = "cliche.whom.passage"
        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(
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
            .check(matches(withText(containsString( threeWordAddress))))

    }

    @Test
    fun testTextSearch_clipToBoundingBoxDoesNotContainAddressOutsideBox() {

        Espresso.onView(withId(R.id.textClipToBoundingBox))
            .perform(scrollTo(), click(), waitUntilVisible(), typeTextIntoFocusedView(boundingBox))

        val threeWordAddress = "falters.curtains.point"
        Espresso.onView(withId(R.id.suggestionEditText))
            .perform(
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
            .perform(waitUntilVisible(hasChildCount(3)))
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

