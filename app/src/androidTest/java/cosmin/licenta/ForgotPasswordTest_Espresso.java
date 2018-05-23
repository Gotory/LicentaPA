//package cosmin.licenta;
//
//import android.support.design.widget.TextInputLayout;
//import android.support.test.espresso.Espresso;
//import android.support.test.rule.ActivityTestRule;
//import android.util.Log;
//import android.view.View;
//import android.widget.EditText;
//
//import com.example.stefan.proiect_learningbyplayinggame.R;
//
//import org.hamcrest.Description;
//import org.hamcrest.Matcher;
//import org.hamcrest.TypeSafeMatcher;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//
//import ro.LearnByPLaying.Activitati.ForgotPasswordActivity;
//import ro.LearnByPLaying.Activitati.LoginActivity;
//
//import static android.support.test.espresso.Espresso.onView;
//import static android.support.test.espresso.action.ViewActions.click;
//import static android.support.test.espresso.action.ViewActions.typeText;
//import static android.support.test.espresso.assertion.ViewAssertions.matches;
//import static android.support.test.espresso.core.internal.deps.guava.base.Preconditions.checkNotNull;
//import static android.support.test.espresso.matcher.ViewMatchers.withId;
//
///**
// * Created by Stefan on 5/6/2018.
// */
//
//public class ForgotPasswordTest_Espresso {
//    @Rule
//    public ActivityTestRule<ForgotPasswordActivity> mActivityTestRule = new ActivityTestRule<ForgotPasswordActivity>(ForgotPasswordActivity.class);
//
//    private String realEmailForTest = "test@testEmail.com"; //pass: testtesttest
//    private String emptyEmailAddress = "";
//    private String fakeEmailAddress = "fakeish@fake.com";
//    private String formatWrongEmailAddress = "testareFormatEMail";
//    @Before
//    public void setUp() throws Exception{
//
//    }
//    @Test
//    public void testScenario_01(){
//        Log.d("Espresso"," - Scenariul 01 - ");
//        Log.d("Espresso","inputs email real: "+realEmailForTest);
//        onView(withId(R.id.forgotPass_editTextEmail)).perform(typeText(realEmailForTest));
//        //close soft keyboard
//        Espresso.closeSoftKeyboard();
//        //perform button click
//        onView(withId(R.id.forgotPass_button)).perform(click());
//    }
//    @Test
//    public void testScenario_02(){
//        Log.d("Espresso"," - Scenariul 02 - ");
//        Log.d("Espresso","no inputs");
//        //perform button click
//        onView(withId(R.id.forgotPass_button)).perform(click());
//        onView(withId(R.id.forgotPass_textInputLayoutEmail)).check
//                (matches(hasTextInputLayoutErrorText(mActivityTestRule.getActivity().getString(R.string
//                        .error_empty,"E-mail"))));
//    }
//    @Test
//    public void testScenario_03(){
//        Log.d("Espresso"," - Scenariul 03 - ");
//        Log.d("Espresso","fake email: "+fakeEmailAddress);
//        onView(withId(R.id.forgotPass_editTextEmail)).perform(typeText(fakeEmailAddress));
//        //close soft keyboard
//        Espresso.closeSoftKeyboard();
//        //perform button click
//        onView(withId(R.id.forgotPass_button)).perform(click());
//        //TODO verificarea erori ce se afiseaaza pe ecran
//    }
//    @Test
//    public void testScenario_04(){
//        Log.d("Espresso"," - Scenariul 04 - ");
//        Log.d("Espresso","wrong email format: "+formatWrongEmailAddress);
//        onView(withId(R.id.forgotPass_editTextEmail)).perform(typeText(fakeEmailAddress));
//        //close soft keyboard
//        Espresso.closeSoftKeyboard();
//        //perform button click
//        onView(withId(R.id.forgotPass_button)).perform(click());
//        //TODO verificarea erori ce se afiseaaza pe ecran
//    }
//
//}
