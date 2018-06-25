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
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private ArrayList<String> results = new ArrayList<>();

    private boolean first = true;

    private String newContactName;
    private HashMap<String, String> newEventData = new HashMap<>();
    private HashMap<String, Integer> newEventDate = new HashMap<>();

    private String currency = "";
    private Note note;

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

        results.clear();

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
                        listenAfterDelay(4000, false, this);
                        break;
                    }

                    for (String result : results) {
                        for (String key : getResources().getStringArray(R.array.call_key)) {
                            if (result.toLowerCase().contains(key)) {
                                commandList.add("call");
                                break;
                            }
                        }
                        for (String key : getResources().getStringArray(R.array.sms_key)) {
                            if (result.toLowerCase().contains(key)) {
                                commandList.add("sms");
                            }
                        }
                        for (String key : getResources().getStringArray(R.array.add_contact_key)) {
                            if (result.toLowerCase().contains(key)) {
                                commandList.add("add_contact");
                            }
                        }
                        for (String key : getResources().getStringArray(R.array.log_out_key)) {
                            if (result.toLowerCase().contains(key)) {
                                commandList.add("exit");
                                drawerButtonActions(R.id.nav_log_out);
                            }
                        }
                        for (String key : getResources().getStringArray(R.array.nothing_key)) {
                            if (result.toLowerCase().contains(key)) {
                                if (commandList.isEmpty()) {
                                    commandList.add("nothing");
                                }
                            }
                        }
                        for (String key : getResources().getStringArray(R.array.gps_key)) {
                            if (result.toLowerCase().contains(key)) {
                                commandList.add("navigation");
                            }
                        }
                        for (String key : getResources().getStringArray(R.array.event_key)) {
                            if (result.toLowerCase().contains(key)) {
                                commandList.add("event");
                            }
                        }
                        for (String key : getResources().getStringArray(R.array.currency_key)) {
                            if (result.toLowerCase().contains(key)) {
                                commandList.add("currency");
                            }
                        }
                        for (String key : getResources().getStringArray(R.array.timer_key)) {
                            if (result.toLowerCase().contains(key)) {
                                drawerButtonActions(R.id.nav_timer);
                                commandList.add("timer");
                            }
                        }
                        for (String key : getResources().getStringArray(R.array.reset_key)) {
                            if (result.toLowerCase().contains(key)) {
                                drawerButtonActions(R.id.nav_timer);
                                commandList.add("reset");
                            }
                        }
                        for (String key : getResources().getStringArray(R.array.clear_notes_key)) {
                            if (result.toLowerCase().contains(key)) {
                                commandList.add("delete_notes");
                            }
                        }
                        for (String key : getResources().getStringArray(R.array.clear_gps_key)) {
                            if (result.toLowerCase().contains(key)) {
                                drawerButtonActions(R.id.nav_gps);
                                commandList.add("delete_gps");
                            }
                        }
                        for (String key : getResources().getStringArray(R.array.change_name_key)) {
                            if (result.toLowerCase().contains(key)) {
                                commandList.add("change_user");
                            }
                        }
                        for (String key : getResources().getStringArray(R.array.alarm_key)) {
                            if (result.toLowerCase().contains(key)) {
                                commandList.add("alarm");
                            }
                        }
                        for (String key : getResources().getStringArray(R.array.note_key)) {
                            if (result.toLowerCase().contains(key)) {
                                commandList.add("home");
                            }
                        }
                        for (String key : getResources().getStringArray(R.array.calculate_key)) {
                            if (result.toLowerCase().contains(key)) {
                                commandList.add("calculate");
                            }
                        }
                        if (result.toLowerCase().contains(getString(R.string.last_command))) {
                            String lastCommand = prefs.getString(MyConstants.prefsLastCommand, "");
                            if (!lastCommand.isEmpty()) {
                                commandList.add(lastCommand);
                            }
                        }
                        if (!commandList.isEmpty()) {
                            break;
                        }
                    }
                    if (commandList.isEmpty()) {
                        tts.speak(getString(R.string.no_command), TextToSpeech.QUEUE_FLUSH, null);
                    } else if (commandList.get(0).equals("nothing")) {
                        tts.speak(getString(R.string.nothing), TextToSpeech.QUEUE_FLUSH, null);
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
                            if (result.equals(R.string.no)) {
                                this.results.remove(0);
                            }
                            if (!this.results.isEmpty() && !result.equals(getString(R.string.yes))) {
                                String name = this.results.get(0);
                                tts.speak(getString(R.string.did_you_mean, name), TextToSpeech.QUEUE_FLUSH, null);
                                listenAfterDelay(2000, true, this);
                            } else {
                                if (this.results.isEmpty()) {
                                    tts.speak(getString(R.string.no_match), TextToSpeech.QUEUE_FLUSH, null);
                                } else {
                                    String name = this.results.get(0);
                                    for (Contact contact : fragment.mContactsList) {
                                        if (contact.getName().toLowerCase().equals(name.toLowerCase())) {
                                            Helper.getInstance().callNumber(contact.getPhoneNumber(), this);
                                            commandList.clear();
                                            first = true;
                                            break;
                                        }
                                    }
                                    if (!first) {
                                        first = true;
                                        tts.speak(getString(R.string.no_contact), TextToSpeech.QUEUE_FLUSH, null);
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
                                if (result.equals(getString(R.string.no))) {
                                    this.results.remove(0);
                                }
                                if (!this.results.isEmpty() && !result.equals(getString(R.string.yes))) {
                                    String name = this.results.get(0);
                                    tts.speak(getString(R.string.did_you_mean, name), TextToSpeech.QUEUE_FLUSH, null);
                                    listenAfterDelay(2000, true, this);
                                } else {
                                    if (this.results.isEmpty()) {
                                        tts.speak(getString(R.string.no_match), TextToSpeech.QUEUE_FLUSH, null);
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
                                        if (step!=1) {
                                            first = true;
                                            tts.speak(getString(R.string.no_contact), TextToSpeech.QUEUE_FLUSH, null);
                                        }
                                    }
                                }
                                break;
                            } else if (step == 1) {
                                String result = results.get(0);
                                Helper.getInstance().sendSMS(phone, result);
                                tts.speak(getString(R.string.message_sent), TextToSpeech.QUEUE_FLUSH, null);
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
                                if (result.equals(getString(R.string.no))) {
                                    this.results.remove(0);
                                }
                                if (!this.results.isEmpty() && !result.equals(getString(R.string.yes))) {
                                    String name = this.results.get(0);
                                    tts.speak(getString(R.string.did_you_mean, name), TextToSpeech.QUEUE_FLUSH, null);
                                    listenAfterDelay(2000, true, this);
                                } else {
                                    if (this.results.isEmpty()) {
                                        tts.speak(getString(R.string.no_match), TextToSpeech.QUEUE_FLUSH, null);
                                    } else {
                                        newContactName = this.results.get(0).toLowerCase();
                                        step++;
                                        tts.speak(getString(R.string.give_number), TextToSpeech.QUEUE_FLUSH, null);
                                        listenAfterDelay(2000, true, this);
                                    }
                                }
                                break;
                            } else if (step == 1) {
                                String result = results.get(0);
                                if (Helper.getInstance().isPhoneNumber(result)) {
                                    Helper.getInstance().addNewContact(this, newContactName, result);
                                    tts.speak(getString(R.string.contact_added), TextToSpeech.QUEUE_FLUSH, null);
                                } else {
                                    tts.speak(getString(R.string.not_a_phone), TextToSpeech.QUEUE_FLUSH, null);
                                }
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
                                if (result.equals(getString(R.string.no))) {
                                    tts.speak("Specify start time", TextToSpeech.QUEUE_FLUSH, null);
                                    step += 2;
                                    newEventData.put(MyConstants.eventDesc, "");
                                    listenAfterDelay(2000, true, this);
                                } else if (result.equals(getString(R.string.yes))) {
                                    newEventData.put(MyConstants.eventDesc, results.get(0));
                                    step++;
                                    tts.speak("OK. What is it", TextToSpeech.QUEUE_FLUSH, null);
                                    listenAfterDelay(2000, true, this);
                                }
                            } else if (step == 2) {
                                String result = results.get(0);
                                if (newEventData.get(MyConstants.eventDesc).equals(getString(R.string.yes))) {
                                    newEventData.put(MyConstants.eventDesc, result);
                                    tts.speak("Specify start time", TextToSpeech.QUEUE_FLUSH, null);
                                    step++;
                                    listenAfterDelay(2000, true, this);
                                }
                            } else if (step == 3) {
                                Log.d("+++", results.toString());
                                String hour = "";
                                String minute = "";
                                for (String result : results) {
                                    if (result.contains(":")) {
                                        hour = result.substring(0, 2);
                                        minute = result.substring(3, 5);
                                        Log.d("+++", hour + " " + minute);
                                        break;
                                    } else if (result.contains(" ")) {
                                        hour = result.substring(0, 2);
                                        minute = result.substring(3, 5);
                                        Log.d("+++", hour + " " + minute);
                                        break;
                                    }
                                }
                                newEventDate.put(MyConstants.eventStartHour, Integer.valueOf(hour));
                                newEventDate.put(MyConstants.eventStartMin, Integer.valueOf(minute));
                                step++;
                                tts.speak("Specify end time", TextToSpeech.QUEUE_FLUSH, null);
                                listenAfterDelay(2000, true, this);
                            } else if (step == 4) {
                                Log.d("+++", results.toString());
                                String hour = "";
                                String minute = "";
                                for (String result : results) {
                                    if (result.contains(":")) {
                                        hour = result.substring(0, 2);
                                        minute = result.substring(3, 5);
                                        Log.d("+++", hour + " " + minute);
                                        break;
                                    } else if (result.contains(" ")) {
                                        hour = result.substring(0, 2);
                                        minute = result.substring(3, 5);
                                        Log.d("+++", hour + " " + minute);
                                        break;
                                    }
                                }
                                newEventDate.put(MyConstants.eventEndHour, Integer.valueOf(hour));
                                newEventDate.put(MyConstants.eventEndMin, Integer.valueOf(minute));
                                step++;
                                tts.speak("Do you want a reminder?", TextToSpeech.QUEUE_FLUSH, null);
                                listenAfterDelay(2000, true, this);
                            } else if (step == 5) {
                                String result = results.get(0);
                                if (result.equals(getString(R.string.no))) {
                                    tts.speak("In what day", TextToSpeech.QUEUE_FLUSH, null);
                                    step += 2;
                                    newEventData.put(MyConstants.eventDesc, "");
                                    listenAfterDelay(2000, true, this);
                                } else if (result.equals(getString(R.string.yes))) {
                                    newEventData.put(MyConstants.eventDesc, results.get(0));
                                    step++;
                                    tts.speak("OK. What is it", TextToSpeech.QUEUE_FLUSH, null);
                                    listenAfterDelay(2000, true, this);
                                }
                            } else if (step == 6) {
                                Log.d("+++", results.toString());
                                String hour = "";
                                String minute = "";
                                for (String result : results) {
                                    if (result.contains(":")) {
                                        hour = result.substring(0, 2);
                                        minute = result.substring(3, 5);
                                        Log.d("+++", hour + " " + minute);
                                        break;
                                    } else if (result.contains(" ")) {
                                        hour = result.substring(0, 2);
                                        minute = result.substring(3, 5);
                                        Log.d("+++", hour + " " + minute);
                                        break;
                                    }
                                }
                                newEventDate.put(MyConstants.eventReminderHour, Integer.valueOf(hour));
                                newEventDate.put(MyConstants.eventReminderMin, Integer.valueOf(minute));
                                step++;
                                tts.speak("In what day", TextToSpeech.QUEUE_FLUSH, null);
                                listenAfterDelay(2000, true, this);
                            } else if (step == 7) {
                                String result = results.get(0);
                                Log.d("+++", results.toString());
                                if (result.equals("today")) {
                                    Date currentTime = Calendar.getInstance().getTime();
                                    newEventDate.put(MyConstants.eventDay, Integer.valueOf((String) DateFormat.format("dd", currentTime)));
                                    newEventDate.put(MyConstants.eventMonth, Integer.valueOf((String) DateFormat.format("MM", currentTime)));
                                    newEventDate.put(MyConstants.eventYear, Integer.valueOf((String) DateFormat.format("yyyy", currentTime)));
                                    Helper.getInstance().checkSaveEvent(this, newEventData, newEventDate);
                                    step = 0;
                                    tts.speak("Event added succesfully", TextToSpeech.QUEUE_FLUSH, null);
                                    commandList.remove(0);
                                    makeNewCommand();
                                } else {
                                    newEventDate.put(MyConstants.eventDay, Integer.valueOf(result));
                                    step++;
                                    tts.speak("In what month", TextToSpeech.QUEUE_FLUSH, null);
                                    listenAfterDelay(2000, true, this);
                                }
                            } else if (step == 8) {
                                try {
                                    String result = results.get(0);
                                    Log.d("+++", results.toString());
                                    SimpleDateFormat inputFormat = new SimpleDateFormat("MMMM");
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(inputFormat.parse(result));
                                    SimpleDateFormat outputFormat = new SimpleDateFormat("MM");
                                    newEventDate.put(MyConstants.eventMonth, Integer.valueOf(outputFormat.format(cal.getTime())));
                                    Log.d("+++", outputFormat.format(cal.getTime()));
                                    step++;
                                    tts.speak("In what year", TextToSpeech.QUEUE_FLUSH, null);
                                    listenAfterDelay(2000, true, this);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else if (step == 9) {
                                String result = results.get(0);
                                Log.d("+++", results.toString());
                                newEventDate.put(MyConstants.eventYear, Integer.valueOf(result));
                                Helper.getInstance().checkSaveEvent(this, newEventData, newEventDate);
                                step = 0;
                                tts.speak("Event added succesfully", TextToSpeech.QUEUE_FLUSH, null);
                                commandList.remove(0);
                                makeNewCommand();
                            }
                            break;
                        }
                        case "currency": {
                            if (step == 0) {
                                String result = results.get(0);
                                currency = result;
                                Log.d("+++", results.toString());
                                tts.speak("Specify sum", TextToSpeech.QUEUE_FLUSH, null);
                                listenAfterDelay(2000, true, this);
                            } else if (step == 1) {
                                String result = results.get(0);
                                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frag_content_frame);
                                if (fragment instanceof CurrencyFragment) {
                                    CurrencyFragment currencyFragment = (CurrencyFragment) fragment;
                                    tts.speak("The result is " + currencyFragment.getSpecificCurrency(currency, Double.valueOf(result)), TextToSpeech.QUEUE_FLUSH, null);
                                    step = 0;
                                    commandList.remove(0);
                                    makeNewCommand();
                                }
                            }
                            break;
                        }
                        case "home": {
                            if (step == 0) {
                                String result = results.get(0);
                                note = new Note();
                                note.setNote(result);
                                step++;
                                tts.speak("do you want a title for the note", TextToSpeech.QUEUE_FLUSH, null);
                                listenAfterDelay(2000, true, this);
                            } else if (step == 1) {
                                String result = results.get(0);
                                if (result.equals(getString(R.string.no))) {
                                    note.setTitle(android.text.format.DateFormat.format("yyyy-MM-dd hh:mm", Calendar.getInstance().getTime()).toString());
                                    new DBHelper(this).addNote(note);
                                    step = 0;
                                    tts.speak("Note added", TextToSpeech.QUEUE_FLUSH, null);
                                    commandList.remove(0);
                                    makeNewCommand();
                                } else {
                                    step++;
                                    tts.speak("Ok, what is it", TextToSpeech.QUEUE_FLUSH, null);
                                    listenAfterDelay(2000, true, this);
                                }
                            } else if (step == 2) {
                                String result = results.get(0);
                                note.setTitle(result);
                                new DBHelper(this).addNote(note);
                                step = 0;
                                tts.speak("Note added", TextToSpeech.QUEUE_FLUSH, null);
                                commandList.remove(0);
                                makeNewCommand();
                            }
                        }
                        case "change_user": {
                            String result = results.get(0);
                            prefs.edit().putString(MyConstants.prefsUser, result).apply();
                            tts.speak("name has been changed", TextToSpeech.QUEUE_FLUSH, null);
                            commandList.remove(0);
                            makeNewCommand();
                            break;
                        }
                        case "alarm": {
                            Log.d("+++", results.toString());
                            String hour = "";
                            String minute = "";
                            for (String result : results) {
                                if (result.contains(":")) {
                                    hour = result.substring(0, 2);
                                    minute = result.substring(3, 5);
                                    Log.d("+++", hour + " " + minute);
                                    break;
                                } else if (result.contains(" ")) {
                                    hour = result.substring(0, 2);
                                    minute = result.substring(3, 5);
                                    Log.d("+++", hour + " " + minute);
                                    break;
                                }
                            }
                            Calendar alarmHour = Calendar.getInstance();
                            alarmHour.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
                            alarmHour.set(Calendar.MINUTE, Integer.valueOf(minute));
                            alarmHour.set(Calendar.SECOND, 0);
                            alarmHour.set(Calendar.MILLISECOND, 0);
                            Intent intent = new Intent(getBaseContext(), AlarmReceiver.class);
                            PendingIntent pendingIntent = PendingIntent.getBroadcast(getBaseContext(), 1, intent, 0);
                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmHour.getTimeInMillis(), pendingIntent);
                            tts.speak("Alarm set", TextToSpeech.QUEUE_FLUSH, null);
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
                //todo add search?
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
            case "currency": {
                drawerButtonActions(R.id.nav_currency);
                prefs.edit().putString(MyConstants.prefsLastCommand, "currency").apply();
                tts.speak("Please specify currency", TextToSpeech.QUEUE_FLUSH, null);
                listenAfterDelay(2000, true, this);
                break;
            }
            case "timer": {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frag_content_frame);
                        if (fragment instanceof TimerFragment) {
                            TimerFragment timerFrag = (TimerFragment) fragment;
                            timerFrag.startTimer();
                            prefs.edit().putString(MyConstants.prefsLastCommand, "timer").apply();
                            tts.speak("Timer started", TextToSpeech.QUEUE_FLUSH, null);

                        }
                    }
                }, 1000);
                break;
            }
            case "reset": {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frag_content_frame);
                        if (fragment instanceof TimerFragment) {
                            TimerFragment timerFrag = (TimerFragment) fragment;
                            timerFrag.stopTimer();
                            tts.speak("Timer reset", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                }, 1000);
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
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frag_content_frame);
                        if (fragment instanceof GpsFragment) {
                            GpsFragment gpsFrag = (GpsFragment) fragment;
                            new DBHelper(MainActivity.this).deleteAllDestinations();
                            gpsFrag.mDestinationList.clear();
                            gpsFrag.mAdapter.notifyDataSetChanged();
                            prefs.edit().putString(MyConstants.prefsLastCommand, "delete_gps").apply();
                            tts.speak("Destinations cleared", TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                }, 1000);
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
                prefs.edit().putString(MyConstants.prefsLastCommand, "calculate").apply();
                tts.speak("Please specify expression", TextToSpeech.QUEUE_FLUSH, null);
                listenAfterDelay(2000, true, this);
                break;
            }
            case "nothing": {
                commandList.remove(0);
                makeNewCommand();
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
                    listenAfterDelay(2000, false, this);
                } else {
                    tts.speak(activity.getString(R.string.hello_user_default), TextToSpeech.QUEUE_FLUSH, null);
                    listenAfterDelay(5000, false, this);
                }
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
