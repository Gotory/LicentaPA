package cosmin.licenta;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import cosmin.licenta.Common.DBHelper;
import cosmin.licenta.Common.Note;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.junit.Assert.*;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class InstrumentedTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);


    @Test
    public void testCase1() {
        try {
            onView(withId(R.id.add_note)).perform(click());
            onView(withText(R.string.note_dialog_text)).check(matches(isDisplayed()));
            onView(withId(android.R.id.button2)).perform(click());

        } catch (Exception e){
            fail();
        }
    }

    @Test
    public void testCase2() {
        Context appContext = InstrumentationRegistry.getTargetContext();

        try {
            onView(withId(R.id.add_note)).perform(click());
            onView(withId(android.R.id.button1)).perform(click());

            DBHelper dbHelper =  new DBHelper(appContext);

            ArrayList<Note> list = dbHelper.getNotes();
            for(Note dbNote : list){
                if(dbNote.getNote().equals("")){
                    assert true;
                    break;
                }
            }
        } catch (Exception e){
            fail();
        }
    }

    @Test
    public void testCase3() {
        Context appContext = InstrumentationRegistry.getTargetContext();

        try {
            DBHelper dbHelper =  new DBHelper(appContext);

            ArrayList<Note> list = dbHelper.getNotes();
            Note note = list.get(0);

            dbHelper.editNote(note.getTitle(),"new db test");

            for(Note dbNote : list){
                if(dbNote.getNote().equals("new db test")){
                    assert true;
                    break;
                }
            }
        } catch (Exception e){
            fail();
        }
    }


    @Test
    public void testCase4() {
        Context appContext = InstrumentationRegistry.getTargetContext();

        try {
            DBHelper dbHelper =  new DBHelper(appContext);

            String destination = "test DB";
            dbHelper.addDest(destination);

            ArrayList<String> list = dbHelper.getDestinations();
            for(String dbDest : list){
                if(dbDest.equals("test db")){
                    assert true;
                    break;
                }
            }
        } catch (Exception e){
            fail();
        }
    }

    @Test
    public void testCase5() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("cosmin.licenta", appContext.getPackageName());
    }


    @Test
    public void testCase6() {
        Context appContext = InstrumentationRegistry.getTargetContext();

        try {
            DBHelper dbHelper =  new DBHelper(appContext);

            Note note = new Note();
            note.setNote("test db");
            note.setTitle("test");
            dbHelper.addNote(note);

            ArrayList<Note> list = dbHelper.getNotes();
            for(Note dbNote : list){
                if(dbNote.getNote().equals("test db")){
                    assert true;
                    break;
                }
            }
        } catch (Exception e){
            fail();
        }
    }

}
