package com.what3words.testing.what3wordscomponentuitest.clearButtonVisibility

import android.content.Intent
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.ActivityTestRule
import com.what3words.testing.MainActivity
import com.what3words.testing.R
import com.what3words.testing.what3wordscomponentuitest.utils.waitUntilVisible
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class W3WAutoSuggestUICheckClearButtonVisibility {

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    var activityScenarioRule = ActivityScenarioRule<MainActivity>(
        Intent(context, MainActivity::class.java).apply {
            this.putExtra(MainActivity.FOCUS_AUTO_SUGGEST_TEXT_ON_CREATE, true)
        }
    )

    @Test
    fun testTextSearch_isDisplayedCorrectlyWhenEditTextComponentHasFocus() {
        Espresso.onView(withId(com.what3words.components.R.id.btnClear)).check(matches(isDisplayed()))
    }
}