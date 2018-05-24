package cosmin.licenta;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import cosmin.licenta.Common.DBHelper;
import cosmin.licenta.Common.Helper;
import cosmin.licenta.Common.Note;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class InstrumentedTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testCase1() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("cosmin.licenta", appContext.getPackageName());
    }


    @Test
    public void testCase2() {
        Context appContext = InstrumentationRegistry.getTargetContext();

        try {
            DBHelper dbHelper = new DBHelper(appContext);

            Note note = new Note();
            note.setNote("test db");
            note.setTitle("test");
            dbHelper.addNote(note);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCase3() {
        Context appContext = InstrumentationRegistry.getTargetContext();

        try {
            DBHelper dbHelper = new DBHelper(appContext);

            ArrayList<Note> list = dbHelper.getNotes();
            Note note = list.get(0);

            dbHelper.editNote(note.getTitle(), "new db test");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testCase4() {
        Context appContext = InstrumentationRegistry.getTargetContext();

        try {
            DBHelper dbHelper = new DBHelper(appContext);

            String destination = "test DB";
            dbHelper.addDest(destination);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testCase5() {
        if (Helper.getInstance().eval("((4 - 2^3 + 1) * -sqrt(3*3+4*4)) / 2") != 7.5)
            fail();
    }

    @Test
    public void testCase6() {
        try{
            Helper.getInstance().isPhoneNumber("0729080553");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
