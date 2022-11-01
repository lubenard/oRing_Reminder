package com.lubenard.oring_reminder;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void firstBootApp() {
        DbManager dbManager = new DbManager(getAppContext());

        getAppContext().deleteDatabase(dbManager.getDatabaseName());

        assertEquals(0, dbManager.getAllDatasForAllEntrys().size());

        // launch desired activity
        ActivityScenario<MainActivity> firstActivity = ActivityScenario.launch(MainActivity.class);

        //firstActivity.onActivity()
        //onView(withId(R.id.layout_session_active)).check(matches(withText("layout_no_session_active")));
        //onView(withText("Hello Steve!")).check(matches(isDisplayed()));*/
    }

    public Context getAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.lubenard.oring_reminder", appContext.getPackageName());
        return appContext;
    }
}