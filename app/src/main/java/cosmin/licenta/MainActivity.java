package cosmin.licenta;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
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
import java.util.HashMap;
import java.util.Locale;

import cosmin.licenta.Common.Contact;
import cosmin.licenta.Common.Helper;
import cosmin.licenta.Common.MyConstants;
import cosmin.licenta.Fragments.ContactsFragment;
import cosmin.licenta.Fragments.CurrencyFragment;
import cosmin.licenta.Fragments.EventReminderFragment;
import cosmin.licenta.Fragments.GpsFragment;
import cosmin.licenta.Fragments.HomeFragment;
import cosmin.licenta.Fragments.TimerFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static AudioManager audioManager;
    public static boolean isAppActive = false;
    public static boolean wasInCall = false;
    public boolean firstPrompt;

    public Activity activity;

    public ArrayList<String> commandList;

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
                        if (result.toLowerCase().contains(getString(R.string.call))) {
                            commandList.add("call");
                            drawerButtonActions(R.id.nav_contacts);
                        } else if (result.toLowerCase().contains(getString(R.string.sms))) {
                            commandList.add("sms");
                            drawerButtonActions(R.id.nav_contacts);
                        } else if (result.toLowerCase().contains(getString(R.string.last_command))) {
                            String lastCommand = prefs.getString(MyConstants.prefsLastCommand, "");
                            if (!lastCommand.isEmpty()) {
                                commandList.add(lastCommand);
                            }
                        }
                    }
                    makeNewCommand();
                }
                break;
            }
            case MyConstants.REQ_CODE_ARGS: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    Log.d("+++", results.toString());
                    String command = commandList.get(0);
                    switch (command) {
                        case "call": {
                            String result = results.get(0);
                            ContactsFragment fragment = (ContactsFragment) getSupportFragmentManager().findFragmentById(R.id.frag_content_frame);
                            for (Contact contact : fragment.mContactsList) {
                                if (contact.getName().toLowerCase().equals(result.toLowerCase())) {
                                    Helper.getInstance().callNumber(contact.getPhoneNumber(), this);
                                    commandList.clear();
                                }
                            }
                            break;
                        }
                        case "sms": {
                            if (step == 0) {
                                String result = results.get(0);
                                ContactsFragment fragment = (ContactsFragment) getSupportFragmentManager().findFragmentById(R.id.frag_content_frame);
                                for (Contact contact : fragment.mContactsList) {
                                    if (contact.getName().toLowerCase().equals(result.toLowerCase())) {
                                        phone = contact.getPhoneNumber();
                                        step++;
                                        tts.speak(getString(R.string.message), TextToSpeech.QUEUE_FLUSH, null);
                                        listenAfterDelay(2000, true, this);
                                        break;
                                    }
                                }
                                break;
                            } else if (step == 1) {
                                String result = results.get(0);
                                Helper.getInstance().sendSMS(phone, result);
                                step = 0;
                                commandList.remove(0);
                                makeNewCommand();
                            }
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
                prefs.edit().putString(MyConstants.prefsLastCommand, "call").apply();
                tts.speak(getString(R.string.to_who, getString(R.string.call)), TextToSpeech.QUEUE_FLUSH, null);
                listenAfterDelay(2000, true, this);
                break;
            }
            case "sms": {
                prefs.edit().putString(MyConstants.prefsLastCommand, "sms").apply();
                tts.speak(getString(R.string.to_who, getString(R.string.send_sms)), TextToSpeech.QUEUE_FLUSH, null);
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
