package cosmin.licenta;

import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.espresso.contrib.PickerActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.Gravity;
import android.widget.CalendarView;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerMatchers.isClosed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;

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

            Thread.sleep(1000);

            onView(withId(R.id.drawer_layout))
                    .check(matches(isClosed(Gravity.LEFT)))
                    .perform(DrawerActions.open());

            onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.nav_home));

            Thread.sleep(1000);

            onView(withId(R.id.drawer_layout))
                    .check(matches(isClosed(Gravity.LEFT)))
                    .perform(DrawerActions.open());

            onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.nav_gps));

            Thread.sleep(1000);

            onView(withId(R.id.drawer_layout))
                    .check(matches(isClosed(Gravity.LEFT)))
                    .perform(DrawerActions.open());

            onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.nav_reminders));

            Thread.sleep(1000);

            onView(withId(R.id.drawer_layout))
                    .check(matches(isClosed(Gravity.LEFT)))
                    .perform(DrawerActions.open());

            onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.nav_timer));

            Thread.sleep(1000);

            onView(withId(R.id.drawer_layout))
                    .check(matches(isClosed(Gravity.LEFT)))
                    .perform(DrawerActions.open());

            onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.nav_currency));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCase2() {
        try {
            onView(withId(R.id.drawer_layout))
                    .check(matches(isClosed(Gravity.LEFT)))
                    .perform(DrawerActions.open());

            onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.nav_contacts));

            onData(anything()).inAdapterView(withId(R.id.contacts_list)).atPosition(0).perform(click());

            Thread.sleep(1000);

            onData(anything()).inAdapterView(withId(R.id.contacts_list)).atPosition(0).onChildView(withId(R.id.call_button)).perform(click());

            Thread.sleep(2000);

            onView(withId(R.id.decline_Btn)).perform(click());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCase3() {
        try {
            onView(withId(R.id.drawer_layout))
                    .check(matches(isClosed(Gravity.LEFT)))
                    .perform(DrawerActions.open());

            onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.nav_contacts));

            onData(anything()).inAdapterView(withId(R.id.contacts_list)).atPosition(0).perform(click());

            onData(anything()).inAdapterView(withId(R.id.contacts_list)).atPosition(0).onChildView(withId(R.id.sms_button)).perform(click());

            onView(withId(android.R.id.button2)).perform(click());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCase4() {
        try {
            onView(withId(R.id.action_add_note)).perform(click());

            onView(withText(R.string.note_dialog_text)).check(matches(isDisplayed()));

            onView(withId(android.R.id.button2)).perform(click());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCase5() {
        try {
            onView(withId(R.id.action_add_note)).perform(click());

            onView(withId(R.id.note_title)).perform(typeText("test title"));

            onView(withId(R.id.note_text)).perform(typeText("test"));

            onView(withId(android.R.id.button1)).perform(click());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testCase6() {
        try {
            onView(withId(R.id.drawer_layout))
                    .check(matches(isClosed(Gravity.LEFT)))
                    .perform(DrawerActions.open());

            onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.nav_gps));

            onView(withId(R.id.nav_destination)).perform(typeText("ca jou"));
            onView(withId(R.id.nav_btn)).perform(click());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCase7() {
        try {
            onView(withId(R.id.drawer_layout))
                    .check(matches(isClosed(Gravity.LEFT)))
                    .perform(DrawerActions.open());

            onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.nav_gps));

            onData(anything()).inAdapterView(withId(R.id.contacts_list)).atPosition(0).perform(click());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCase8() {
        try {
            onView(withId(R.id.drawer_layout))
                    .check(matches(isClosed(Gravity.LEFT)))
                    .perform(DrawerActions.open());

            onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.nav_timer));

            onView(withId(R.id.StartBtn)).perform(click());

            Thread.sleep(5000);

            onView(withId(R.id.StartBtn)).perform(click());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCase9() {
        try {
            onView(withId(R.id.drawer_layout))
                    .check(matches(isClosed(Gravity.LEFT)))
                    .perform(DrawerActions.open());

            onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.nav_reminders));

            onView(withId(R.id.event_name)).perform(typeText("test"));
            onView(withId(R.id.event_desc)).perform(typeText("test description"));
            onView(withId(R.id.start_time_hour)).perform(replaceText("13"));
            onView(withId(R.id.start_time_minute)).perform(replaceText("25"));
            onView(withId(R.id.end_time_hour)).perform(replaceText("14"));
            onView(withId(R.id.end_time_minute)).perform(replaceText("55"));
            onView(withId(R.id.reminder_time_hour)).perform(replaceText("00"));
            onView(withId(R.id.reminder_time_minute)).perform(replaceText("05"));
            onView(withClassName(Matchers.equalTo(CalendarView.class.getName()))).perform(PickerActions.setDate(2018, 6, 5));
            onView(withId(R.id.add_event)).perform(click());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCase10() {
        try {
            onView(withId(R.id.drawer_layout))
                    .check(matches(isClosed(Gravity.LEFT)))
                    .perform(DrawerActions.open());

            onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.nav_currency));

            onData(anything()).inAdapterView(withId(R.id.currency_list)).atPosition(0).perform(longClick());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCase11() {
        try {
            onView(withId(R.id.drawer_layout))
                    .check(matches(isClosed(Gravity.LEFT)))
                    .perform(DrawerActions.open());

            onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.nav_gps));

            onData(anything()).inAdapterView(withId(R.id.last_location_list)).atPosition(0).perform(longClick());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCase12() {
        try {
            onData(anything()).inAdapterView(withId(R.id.note_list)).atPosition(0).perform(longClick());

            onView(withId(R.id.note_text)).perform(typeText("new test"));

            onView(withId(android.R.id.button1)).perform(click());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCase13() {
        try {
            onView(withId(R.id.action_add_note)).perform(click());

            onView(withId(R.id.note_text)).perform(typeText("test"));

            onView(withId(android.R.id.button1)).perform(click());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCase14() {
        try {
            onData(anything()).inAdapterView(withId(R.id.note_list)).atPosition(0).perform(click());

            onData(anything()).inAdapterView(withId(R.id.note_list)).atPosition(0).onChildView(withId(R.id.decline_Btn)).perform(click());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
