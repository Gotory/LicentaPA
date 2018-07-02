package cosmin.licenta;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
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
public class MethodTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void getHelperInstance() {
        Helper helper = Helper.getInstance();
        if(helper != null) {
            assert true;
        } else {
            Assert.fail();
        }
    }


    @Test
    public void testAddLocation() {
        Context appContext = InstrumentationRegistry.getTargetContext();

        try {
            DBHelper dbHelper = new DBHelper(appContext);

            String destination = "Ca Jou";
            dbHelper.addDest(destination);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testEval() {
        assertEquals(String.valueOf(Helper.getInstance().eval("((4 - 2^3 + 1) * -sqrt(3*3+4*4)) / 2")),"7.5");
    }

    @Test
    public void testIsPhoneNumber() {
        try{
            Helper.getInstance().isPhoneNumber("0729080553");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void testSendMessage() {
        try{
            Helper.getInstance().sendSMS("0729080553","mesaj de test");
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
