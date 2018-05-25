package cosmin.licenta;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import cosmin.licenta.Common.Contact;
import cosmin.licenta.Common.DBHelper;
import cosmin.licenta.Common.Helper;
import cosmin.licenta.Common.MyConstants;
import cosmin.licenta.Common.Note;
import cosmin.licenta.Fragments.ContactsFragment;
import cosmin.licenta.Fragments.CurrencyFragment;
import cosmin.licenta.Fragments.EventReminderFragment;
import cosmin.licenta.Fragments.GpsFragment;
import cosmin.licenta.Fragments.HomeFragment;
import cosmin.licenta.Fragments.TimerFragment;
import cosmin.licenta.Receivers.AlarmReceiver;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static AudioManager audioManager;
    public static boolean isAppActive = false;
    public static boolean wasInCall = false;
    public boolean firstPrompt;

    public Activity activity;

    public ArrayList<String> commandList;
    private ArrayList<String> results;

    private boolean first = true;

    private String newContactName;
    private HashMap<String, String> newEventData;
    private HashMap<String, Integer> newEventDate;

    private FrameLayout appBase;

    private int step;
    private String phone;
    public TextToSpeech tts;
    private SharedPreferences prefs;

    public NavigationView navView;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;
        firstPrompt = true;

        navView = findViewById(R.id.navigation_view);
        prefs = activity.getSharedPreferences(MyConstants.PREFS_NAME, Context.MODE_PRIVATE);
        appBase = findViewById(R.id.frag_content_frame);
        appBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                View focus = activity.getCurrentFocus();
                if (focus != null) {
                    inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
                }
            }
        });

        results = new ArrayList<>();

        if (navView != null) {
            navView.getMenu().getItem(0).setChecked(true);
            navView.setNavigationItemSelectedListener(this);
        }

        step = 0;
        commandList = new ArrayList<>();

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.drawer_home));
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                View focus = activity.getCurrentFocus();
                if (focus != null) {
                    inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        MyPhoneStateListener phoneStateListener = new MyPhoneStateListener(getApplicationContext());

        TelephonyManager telephonyManager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
        if (telephonyManager != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.SEND_SMS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_CONTACTS
        }, 33);

        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        showHomeFragment();

        if (getIntent().getAction().equals(MyConstants.actionNewNoteWidget)) {
            HashMap<String, String> params = new HashMap<>();
            params.put(MyConstants.paramsType, MyConstants.paramsNote);
            params.put(MyConstants.paramsTitle, "");
            params.put(MyConstants.paramsEdit, MyConstants.FALSE);
            Helper.getInstance().showDialog(this, params);
        }

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Helper.getInstance().isNotificationServiceAccessible(getApplicationContext())) {
                showNotificationAccessRequestDialog(this);
            }
        }
        isAppActive = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAppActive = false;
        tts.stop();
        tts.shutdown();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case MyConstants.REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.d("+++", results.toString());
                    if (prefs.getString(MyConstants.prefsUser, "").equals("")) {
                        String name = results.get(0);
                        prefs.edit().putString(MyConstants.prefsUser, name).apply();
                        tts.speak(getString(R.string.user_set, name), TextToSpeech.QUEUE_FLUSH, null);
                        listenAfterDelay(2000, false, this);
                        break;
                    }

                    for (String result : results) {
                        if (result.toLowerCase().contains(getString(R.string.call)) || result.toLowerCase().contains("dial")) {
                            commandList.add("call");
                        }
                        if (result.toLowerCase().contains(getString(R.string.sms)) || result.toLowerCase().contains("message")) {
                            commandList.add("sms");
                        }
                        if (result.toLowerCase().contains("add contact")) {
                            commandList.add("add_contact");
                        }
                        if (result.toLowerCase().contains("exit") || result.toLowerCase().contains("log out") || result.toLowerCase().contains("close") || result.toLowerCase().contains("stop app") || result.toLowerCase().contains("log off")) {
                            drawerButtonActions(R.id.nav_log_out);
                        }
                        if (result.toLowerCase().contains("nothing")) {
                            if (commandList.isEmpty()) {
                                commandList.add("nothing");
                            }
                        }
                        if (result.toLowerCase().contains("directions") || result.toLowerCase().contains("navigation") || result.toLowerCase().contains("rout") || result.toLowerCase().contains("gps")) {
                            commandList.add("navigation");
                        }
                        if (result.toLowerCase().contains("event") || result.toLowerCase().contains("reminder") || result.toLowerCase().contains("calendar")) {
                            commandList.add("event");
                        }
//                        if (result.toLowerCase().contains("currency") || result.toLowerCase().contains("exchange")) {
//                            commandList.add("currency");
//                        }
                        if (result.toLowerCase().contains("timer") || result.toLowerCase().contains("chronometer")) {
                            commandList.add("timer");
                        }
                        if (result.toLowerCase().contains("reset") || result.equals("stop timer")) {
                            commandList.add("reset");
                        }
                        if (result.toLowerCase().contains("clear notes") || result.toLowerCase().contains("delete notes")) {
                            commandList.add("delete_notes");
                        }
                        if (result.toLowerCase().contains("clear routes") || result.toLowerCase().contains("delete gps information")) {
                            commandList.add("delete_gps");
                        }
                        if (result.toLowerCase().contains("change name") || result.toLowerCase().contains("modify username") || result.toLowerCase().contains("change username") || result.toLowerCase().contains("modify name")) {
                            commandList.add("change_user");
                        }
                        if (result.toLowerCase().contains("alarm")) {
                            commandList.add("alarm");
                        }
                        if (result.toLowerCase().contains("note") || result.toLowerCase().contains("home")) {
                            commandList.add("home");
                        }
                        if (result.toLowerCase().contains("calculate")) {
                            commandList.add("calculate");
                        }
                        if (result.toLowerCase().contains(""))
                            if (result.toLowerCase().contains(getString(R.string.last_command))) {
                                String lastCommand = prefs.getString(MyConstants.prefsLastCommand, "");
                                if (!lastCommand.isEmpty()) {
                                    commandList.add(lastCommand);
                                }
                            }
                    }
                    if (commandList.isEmpty()) {
                        tts.speak("You did not imput a command please try again", TextToSpeech.QUEUE_FLUSH, null);
                    } else if (commandList.get(0).equals("nothing")) {
                        tts.speak("Why did you start me then", TextToSpeech.QUEUE_FLUSH, null);
                        commandList.clear();
                    } else {
                        makeNewCommand();
                    }
                }
                break;
            }
            case MyConstants.REQ_CODE_ARGS: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String command = commandList.get(0);
                    switch (command) {
                        case "call": {
                            ContactsFragment fragment = (ContactsFragment) getSupportFragmentManager().findFragmentById(R.id.frag_content_frame);
                            if (first) {
                                this.results = results;
                                first = false;
                            }
                            String result = results.get(0);
                            if (result.equals("no")) {
                                this.results.remove(0);
                            }
                            if ((!this.results.isEmpty() && !result.equals("yes")) || results.size() > 1) {
                                String name = this.results.get(0);
                                tts.speak("Did you mean" + name, TextToSpeech.QUEUE_FLUSH, null);
                                listenAfterDelay(2000, true, this);
                            } else {
                                if (this.results.isEmpty()) {
                                    tts.speak("Sorry didn't find a match", TextToSpeech.QUEUE_FLUSH, null);
                                } else {
                                    for (Contact contact : fragment.mContactsList) {
                                        if (contact.getName().toLowerCase().equals(result.toLowerCase())) {
                                            Helper.getInstance().callNumber(contact.getPhoneNumber(), this);
                                            commandList.clear();
                                            first = true;
                                            break;
                                        }
                                    }
                                    if (!first) {
                                        first = true;
                                        tts.speak("Sorry did not find the contact in your contact list", TextToSpeech.QUEUE_FLUSH, null);
                                    }
                                }
                            }
                            break;
                        }
                        case "sms": {
                            if (step == 0) {
                                ContactsFragment fragment = (ContactsFragment) getSupportFragmentManager().findFragmentById(R.id.frag_content_frame);
                                if (first) {
                                    this.results = results;
                                    first = false;
                                }
                                String result = results.get(0);
                                if (result.equals("no")) {
                                    this.results.remove(0);
                                }
                                if ((!this.results.isEmpty() && !result.equals("yes")) || results.size() > 1) {
                                    String name = this.results.get(0);
                                    tts.speak("Did you mean" + name, TextToSpeech.QUEUE_FLUSH, null);
                                    listenAfterDelay(2000, true, this);
                                } else {
                                    if (this.results.isEmpty()) {
                                        tts.speak("Sorry didn't find a match", TextToSpeech.QUEUE_FLUSH, null);
                                    } else {
                                        for (Contact contact : fragment.mContactsList) {
                                            if (contact.getName().toLowerCase().equals(this.results.get(0).toLowerCase())) {
                                                phone = contact.getPhoneNumber();
                                                step++;
                                                tts.speak(getString(R.string.message), TextToSpeech.QUEUE_FLUSH, null);
                                                listenAfterDelay(2000, true, this);
                                                break;
                                            }
                                        }
                                    }
                                }
                                break;
                            } else if (step == 1) {
                                String result = results.get(0);
                                Helper.getInstance().sendSMS(phone, result);
                                step = 0;
                                commandList.remove(0);
                                first = true;
                                makeNewCommand();
                            }
                            break;
                        }
                        case "add_contact": {
                            if (step == 0) {
                                if (first) {
                                    this.results = results;
                                    first = false;
                                }
                                String result = results.get(0);
                                if (result.equals("no")) {
                                    this.results.remove(0);
                                }
                                if ((!this.results.isEmpty() && !result.equals("yes")) || results.size() > 1) {
                                    String name = this.results.get(0);
                                    tts.speak("Did you mean" + name, TextToSpeech.QUEUE_FLUSH, null);
                                    listenAfterDelay(2000, true, this);
                                } else {
                                    if (this.results.isEmpty()) {
                                        tts.speak("Sorry didn't find a match", TextToSpeech.QUEUE_FLUSH, null);
                                    } else {
                                        newContactName = this.results.get(0).toLowerCase();
                                        step++;
                                        tts.speak("Now give me the phone number", TextToSpeech.QUEUE_FLUSH, null);
                                        listenAfterDelay(2000, true, this);
                                    }
                                }
                                break;
                            } else if (step == 1) {
                                String result = results.get(0);
                                Helper.getInstance().addNewContact(this, newContactName, result);
                                step = 0;
                                commandList.remove(0);
                                first = true;
                                makeNewCommand();
                            }
                            break;
                        }
                        case "navigation": {
                            String result = results.get(0);
                            Helper.getInstance().startMaps(this, result);
                            commandList.clear();
                            makeNewCommand();
                            break;
                        }
                        case "event": {
                            if (step == 0) {
                                newEventData.put(MyConstants.eventName, results.get(0));
                                step++;
                                tts.speak("Do you want a description", TextToSpeech.QUEUE_FLUSH, null);
                                listenAfterDelay(2000, true, this);
                                break;
                            } else if (step == 1) {
                                String result = results.get(0);
                                if (result.equals("no")) {
                                    tts.speak("Specify start time", TextToSpeech.QUEUE_FLUSH, null);
                                    step += 2;
                                    newEventData.put(MyConstants.eventDesc, "");
                                    listenAfterDelay(2000, true, this);
                                } else if (result.equals("yes")) {
                                    newEventData.put(MyConstants.eventDesc, results.get(0));
                                    step++;
                                    tts.speak("OK. What is it", TextToSpeech.QUEUE_FLUSH, null);
                                    listenAfterDelay(2000, true, this);
                                }
                            } else if (step == 2) {
                                String result = results.get(0);
                                if (newEventData.get(MyConstants.eventDesc).equals("yes")) {
                                    newEventData.put(MyConstants.eventDesc, result);
                                    tts.speak("Specify start time", TextToSpeech.QUEUE_FLUSH, null);
                                    step++;
                                    listenAfterDelay(2000, true, this);
                                }
                            } else if (step == 3) {
                                String result = results.get(0);
                                Log.d("+++", results.toString());
                                //todo split hour and minute
//                                newEvent.put(MyConstants.eventStartHour, result);
                                step++;
                                tts.speak("Specify end time", TextToSpeech.QUEUE_FLUSH, null);
                                listenAfterDelay(2000, true, this);
                            } else if (step == 4) {
                                String result = results.get(0);
                                Log.d("+++", results.toString());
                                //todo split hour and minute
//                                newEvent.put(MyConstants.eventEndHour, result);
                                step++;
                                tts.speak("Do you want a reminder?", TextToSpeech.QUEUE_FLUSH, null);
                                listenAfterDelay(2000, true, this);
                            } else if (step == 5) {
                                String result = results.get(0);
                                if (result.equals("no")) {
                                    tts.speak("In what day", TextToSpeech.QUEUE_FLUSH, null);
                                    step += 2;
                                    newEventData.put(MyConstants.eventDesc, "");
                                    listenAfterDelay(2000, true, this);
                                } else if (result.equals("yes")) {
                                    newEventData.put(MyConstants.eventDesc, results.get(0));
                                    step++;
                                    tts.speak("OK. What is it", TextToSpeech.QUEUE_FLUSH, null);
                                    listenAfterDelay(2000, true, this);
                                }
                            } else if (step == 6) {
                                String result = results.get(0);
                                Log.d("+++", results.toString());
                                //todo split hour and minute
//                                newEvent.put(MyConstants.eventReminderHour, result);
                                step++;
                                tts.speak("In what day", TextToSpeech.QUEUE_FLUSH, null);
                                listenAfterDelay(2000, true, this);
                            } else if (step == 7) {
                                //todo make sure it's a year
                                String result = results.get(0);
                                Log.d("+++", results.toString());
//                                newEventDate.put(MyConstants.eventDay, Integer.valueOf(result));
                                step++;
                                tts.speak("In what month", TextToSpeech.QUEUE_FLUSH, null);
                                listenAfterDelay(2000, true, this);
                            } else if (step == 8) {
                                String result = results.get(0);
                                Log.d("+++", results.toString());
//                                newEventDate.put(MyConstants.eventMonth, Integer.valueOf(result));
                                step++;
                                tts.speak("In what year", TextToSpeech.QUEUE_FLUSH, null);
                                listenAfterDelay(2000, true, this);
                            } else if (step == 9) {
                                String result = results.get(0);
                                Log.d("+++", results.toString());
//                                newEventDate.put(MyConstants.eventYear, Integer.valueOf(result));
//                                Helper.getInstance().checkSaveEvent(this, newEventData, newEventDate);
                                step = 0;
                                commandList.remove(0);
                                makeNewCommand();
                            }
                            break;
                        }
                        case "home": {
                            String result = results.get(0);
                            Note note = new Note();
                            note.setTitle(android.text.format.DateFormat.format("yyyy-MM-dd hh:mm a", Calendar.getInstance().getTime()).toString());
                            note.setNote(result);
                            new DBHelper(this).addNote(note);
                            commandList.remove(0);
                            makeNewCommand();
                            break;
                        }
                        case "change_user": {
                            String result = results.get(0);
                            prefs.edit().putString(MyConstants.prefsUser, result).apply();
                            commandList.remove(0);
                            makeNewCommand();
                            break;
                        }
                        case "alarm": {
                            //todo
                            String result = results.get(0);
                            Log.d("+++", results.toString());

//                            Calendar alarmHour = Calendar.getInstance();

//                            alarmHour.set(Calendar.HOUR_OF_DAY, result);
//                            alarmHour.set(Calendar.MINUTE, result);
//                            alarmHour.set(Calendar.SECOND, 0);
//                            alarmHour.set(Calendar.MILLISECOND, 0);
//                            Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
//                            PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), 1, intent, 0);
//                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//                            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmHour.getTimeInMillis(), pendingIntent);
                            commandList.remove(0);
                            makeNewCommand();
                            break;
                        }
                        case "calculate": {
                            //todo - parse command better
                            Log.d("+++", results.toString());
                            String result = results.get(0);
                            tts.speak(String.valueOf(Helper.getInstance().eval(result)), TextToSpeech.QUEUE_FLUSH, null);
                            commandList.remove(0);
                            makeNewCommand();
                            break;
                        }
                    }
                }
                break;
            }
        }
    }

    private void makeNewCommand() {
        String command = "";
        if (!commandList.isEmpty()) {
            command = commandList.get(0);
        }
        switch (command) {
            case "call": {
                drawerButtonActions(R.id.nav_contacts);
                prefs.edit().putString(MyConstants.prefsLastCommand, "call").apply();
                tts.speak(getString(R.string.to_who, getString(R.string.call)), TextToSpeech.QUEUE_FLUSH, null);
                listenAfterDelay(2000, true, this);
                break;
            }
            case "sms": {
                drawerButtonActions(R.id.nav_contacts);
                prefs.edit().putString(MyConstants.prefsLastCommand, "sms").apply();
                tts.speak(getString(R.string.to_who, getString(R.string.send_sms)), TextToSpeech.QUEUE_FLUSH, null);
                listenAfterDelay(2000, true, this);
                break;
            }
            case "add_contact": {
                prefs.edit().putString(MyConstants.prefsLastCommand, "add_contact").apply();
                tts.speak("please specify contact name", TextToSpeech.QUEUE_FLUSH, null);
                listenAfterDelay(2000, true, this);
                break;
            }
            case "navigation": {
                drawerButtonActions(R.id.nav_destination);
                prefs.edit().putString(MyConstants.prefsLastCommand, "navigation").apply();
                tts.speak("Please specify destination", TextToSpeech.QUEUE_FLUSH, null);
                listenAfterDelay(2000, true, this);
                break;
            }
            case "event": {
                drawerButtonActions(R.id.nav_reminders);
                prefs.edit().putString(MyConstants.prefsLastCommand, "event").apply();
                tts.speak("Please specify event name", TextToSpeech.QUEUE_FLUSH, null);
                listenAfterDelay(2000, true, this);
                break;
            }
//            case "currency": {
//                drawerButtonActions(R.id.nav_currency);
//                prefs.edit().putString(MyConstants.prefsLastCommand, "currency").apply();
//                tts.speak("Please specify currency", TextToSpeech.QUEUE_FLUSH, null);
//                listenAfterDelay(2000, true, this);
//                break;
//            }
            case "timer": {
                drawerButtonActions(R.id.nav_timer);
                TimerFragment fragment = (TimerFragment) getSupportFragmentManager().findFragmentById(R.id.frag_content_frame);
                fragment.startTimer();
                prefs.edit().putString(MyConstants.prefsLastCommand, "timer").apply();
                tts.speak("Timer started", TextToSpeech.QUEUE_FLUSH, null);
                break;
            }
            case "reset": {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frag_content_frame);
                if (fragment instanceof TimerFragment) {
                    TimerFragment timerFrag = (TimerFragment) fragment;
                    timerFrag.stopTimer();
                    tts.speak("Timer reset", TextToSpeech.QUEUE_FLUSH, null);
                }
                break;
            }
            case "delete_notes": {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frag_content_frame);
                if (fragment instanceof HomeFragment) {
                    HomeFragment homeFrag = (HomeFragment) fragment;
                    new DBHelper(this).deleteAllNotes();
                    homeFrag.mNoteList.clear();
                    homeFrag.mAdapter.notifyDataSetChanged();
                    prefs.edit().putString(MyConstants.prefsLastCommand, "delete_notes").apply();
                    tts.speak("Notes deleted", TextToSpeech.QUEUE_FLUSH, null);
                }
                break;
            }
            case "delete_gps": {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frag_content_frame);
                if (fragment instanceof GpsFragment) {
                    GpsFragment gpsFrag = (GpsFragment) fragment;
                    new DBHelper(this).deleteAllDestinations();
                    gpsFrag.mDestinationList.clear();
                    gpsFrag.mAdapter.notifyDataSetChanged();
                    prefs.edit().putString(MyConstants.prefsLastCommand, "delete_gps").apply();
                    tts.speak("Destinations cleared", TextToSpeech.QUEUE_FLUSH, null);
                }
                break;
            }
            case "change_user": {
                prefs.edit().putString(MyConstants.prefsLastCommand, "change_user").apply();
                tts.speak("OK. How do you want me to call you", TextToSpeech.QUEUE_FLUSH, null);
                listenAfterDelay(2000, true, this);
                break;
            }
            case "alarm": {
                prefs.edit().putString(MyConstants.prefsLastCommand, "alarm").apply();
                tts.speak("Please specify alarm time", TextToSpeech.QUEUE_FLUSH, null);
                listenAfterDelay(2000, true, this);
                break;
            }
            case "home": {
                drawerButtonActions(R.id.nav_home);
                prefs.edit().putString(MyConstants.prefsLastCommand, "home").apply();
                tts.speak("Please specify note text", TextToSpeech.QUEUE_FLUSH, null);
                listenAfterDelay(2000, true, this);
                break;
            }
            case "calculate": {
                drawerButtonActions(R.id.nav_home);
                prefs.edit().putString(MyConstants.prefsLastCommand, "calculate").apply();
                tts.speak("Please specify expression", TextToSpeech.QUEUE_FLUSH, null);
                listenAfterDelay(2000, true, this);
                break;
            }
        }

    }

    public void listenAfterDelay(int delay, final boolean forArgs, final MainActivity activity) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!forArgs) {
                    Helper.getInstance().promptSpeechInput(activity);
                } else {
                    Helper.getInstance().requestCommandArgs(activity);
                }
            }
        }, delay);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (getSupportFragmentManager().findFragmentByTag(MyConstants.gps_tag) != null ||
                    getSupportFragmentManager().findFragmentByTag(MyConstants.calendar_tag) != null ||
                    getSupportFragmentManager().findFragmentByTag(MyConstants.timer_tag) != null ||
                    getSupportFragmentManager().findFragmentByTag(MyConstants.currency_tag) != null ||
                    getSupportFragmentManager().findFragmentByTag(MyConstants.contacts_tag) != null) {
                showHomeFragment();
            } else {
                super.onBackPressed();
            }
        }
    }

    private void showHomeFragment() {
        drawerButtonActions(R.id.nav_home);
        if (navView != null) {
            navView.getMenu().getItem(0).setChecked(true);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        // Handle navigation view item clicks here.
        final Handler uiHandler = new Handler();
        Thread thread = new Thread() {
            @Override
            public void run() {
                final int id = item.getItemId();
                uiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        drawerButtonActions(id);
                    }
                });
            }
        };
        thread.start();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    public void drawerButtonActions(int id) {
        switch (id) {
            case R.id.nav_home: {
                replaceFragment(HomeFragment.newInstance(), MyConstants.home_tag);
                toolbar.setTitle(getString(R.string.drawer_home));
                break;
            }
            case R.id.nav_contacts: {
                replaceFragment(ContactsFragment.newInstance(), MyConstants.contacts_tag);
                toolbar.setTitle(getString(R.string.drawer_contacts));
                break;
            }
            case R.id.nav_gps: {
                replaceFragment(GpsFragment.newInstance(), MyConstants.gps_tag);
                toolbar.setTitle(getString(R.string.drawer_gps));
                break;
            }
            case R.id.nav_reminders: {
                replaceFragment(EventReminderFragment.newInstance(), MyConstants.calendar_tag);
                toolbar.setTitle(getString(R.string.drawer_reminder));
                break;
            }
            case R.id.nav_timer: {
                replaceFragment(TimerFragment.newInstance(), MyConstants.timer_tag);
                toolbar.setTitle(getString(R.string.drawer_timer));
                break;
            }
            case R.id.nav_currency: {
                replaceFragment(CurrencyFragment.newInstance(), MyConstants.currency_tag);
                toolbar.setTitle(getString(R.string.drawer_currency));
                break;
            }
            case R.id.nav_listen: {
                if (!prefs.getString(MyConstants.prefsUser, "").equals("")) {
                    tts.speak(activity.getString(R.string.hello_user, prefs.getString(MyConstants.prefsUser, "")), TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    tts.speak(activity.getString(R.string.hello_user_default), TextToSpeech.QUEUE_FLUSH, null);
                }
                listenAfterDelay(2000, false, this);
                Helper.getInstance().changeSelectedNavItem(this);
                break;
            }
            case R.id.nav_add_contact: {
                HashMap<String, String> params = new HashMap<>();
                params.put(MyConstants.paramsType, MyConstants.paramsContact);
                Helper.getInstance().showDialog(this, params);
                Helper.getInstance().changeSelectedNavItem(this);
                break;
            }
            case R.id.nav_log_out: {
                MainActivity.this.finish();
                break;
            }
            default:
                break;
        }
    }

    private void replaceFragment(Fragment fragment, String tag) {
        if (getSupportFragmentManager().findFragmentByTag(tag) == null
                || !getSupportFragmentManager().findFragmentByTag(tag).isVisible()) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frag_content_frame, fragment, tag).commit();
        }
    }

    public void showNotificationAccessRequestDialog(Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.access_notifications_dialog_title);
        builder.setMessage(R.string.access_notifications_dialog_message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        Context context;

        private MyPhoneStateListener(Context context) {
            this.context = context;
        }

        @Override
        public void onCallStateChanged(int state, final String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_OFFHOOK: {
                    wasInCall = true;
                    setSpeakerPhoneOn();
                    break;
                }

                case TelephonyManager.CALL_STATE_IDLE: {
                    if (wasInCall) {
                        wasInCall = false;
                        if (!Helper.getInstance().isNotificationServiceAccessible(context) || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            Intent intentPhoneCall = new Intent(MyConstants.actionFinish);
                            context.sendBroadcast(intentPhoneCall);
                        }
                    }
                    break;
                }

                case TelephonyManager.CALL_STATE_RINGING: {
                    wasInCall = true;
                    if (isAppActive) {

                        if (!Helper.getInstance().isNotificationServiceAccessible(context) || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                            int delay = 500;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intentPhoneCall = new Intent(context, AnswerCallsActivity.class);
                                    intentPhoneCall.putExtra(MyConstants.intent_incoming_number, incomingNumber);
                                    intentPhoneCall.putExtra(MyConstants.show_answer_button, true);
                                    intentPhoneCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intentPhoneCall);
                                }
                            }, delay);
                        }
                    }
                    break;
                }
            }
        }

        private void setSpeakerPhoneOn() {
            MainActivity.audioManager.setMode(AudioManager.MODE_IN_CALL);
            MainActivity.audioManager.setSpeakerphoneOn(true);
        }
    }
}
