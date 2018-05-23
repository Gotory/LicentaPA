package cosmin.licenta;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.Gravity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerMatchers.isClosed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class UI_tests {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testCase1() {
        try {
            onView(withId(R.id.drawer_layout))
                    .check(matches(isClosed(Gravity.LEFT)))
                    .perform(DrawerActions.open());

            onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.nav_contacts));

            onView(withId(R.id.contacts_list)).check(matches(isDisplayed()));

        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCase2() {

        try {

        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testCase3() {

        try {

        } catch (Exception e) {
            fail();
        }
    }


    @Test
    public void testCase4() {

        try {

        } catch (Exception e) {
            fail();
        }
    }


    @Test
    public void testCase5() {
        try {

        } catch (Exception e) {
            fail();
        }
    }

}
