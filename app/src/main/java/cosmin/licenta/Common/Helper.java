package cosmin.licenta.Common;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.RemoteException;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import cosmin.licenta.AnswerCallsActivity;
import cosmin.licenta.Fragments.ContactsFragment;
import cosmin.licenta.Fragments.CurrencyFragment;
import cosmin.licenta.Fragments.EventReminderFragment;
import cosmin.licenta.Fragments.GpsFragment;
import cosmin.licenta.Fragments.HomeFragment;
import cosmin.licenta.Fragments.TimerFragment;
import cosmin.licenta.MainActivity;
import cosmin.licenta.R;

public class Helper {

    private static Helper INSTANCE;

    public static Helper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Helper();
        }
        return INSTANCE;
    }

    public boolean isPhoneNumber(String str) {
        return android.util.Patterns.PHONE.matcher(str).matches();
    }

    public boolean isNotificationServiceAccessible(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        String enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners");
        String packageName = context.getPackageName();

        return enabledNotificationListeners == null || !enabledNotificationListeners.contains(packageName);
    }

    //todo add functions for the voice commands

    public void promptSpeechInput(MainActivity activity) {
        activity.commandList.clear();

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        try {
            activity.startActivityForResult(intent, MyConstants.REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(activity, activity.getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    public void requestCommandArgs(MainActivity activity) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        try {
            activity.startActivityForResult(intent, MyConstants.REQ_CODE_ARGS);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(activity, activity.getString(R.string.speech_not_supported), Toast.LENGTH_SHORT).show();
        }
    }

    public void changeSelectedNavItem(MainActivity activity) {
        if (activity.navView != null) {
            Fragment currentFragment = activity.getSupportFragmentManager().findFragmentById(R.id.frag_content_frame);
            if (currentFragment instanceof ContactsFragment) {
                activity.navView.getMenu().getItem(1).setChecked(true);
            } else if (currentFragment instanceof EventReminderFragment) {
                activity.navView.getMenu().getItem(3).setChecked(true);
            } else if ((currentFragment instanceof GpsFragment)) {
                activity.navView.getMenu().getItem(2).setChecked(true);
            } else if (currentFragment instanceof HomeFragment) {
                activity.navView.getMenu().getItem(0).setChecked(true);
            } else if (currentFragment instanceof TimerFragment) {
                activity.navView.getMenu().getItem(4).setChecked(true);
            } else if (currentFragment instanceof CurrencyFragment){
                activity.navView.getMenu().getItem(5).setChecked(true);
            }
        }
    }

    public void callNumber(final String number, final Context context) {
        // delay the call so that the user can cancel the call
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Intent callNumberIntent = new Intent(android.content.Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(number)));
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                context.startActivity(callNumberIntent);
            }
        });
        if (!Helper.getInstance().isNotificationServiceAccessible(context) || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            int delay = 2000;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intentPhoneCall = new Intent(context, AnswerCallsActivity.class);
                    intentPhoneCall.putExtra(MyConstants.intent_incoming_number, number);
                    intentPhoneCall.putExtra(MyConstants.show_answer_button, false);
                    intentPhoneCall.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intentPhoneCall);
                }
            }, delay);
        }
    }

    public void sendSMS(String address, String content) {
        try {
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> smsString = sms.divideMessage(content);
            sms.sendMultipartTextMessage(address, null, smsString, null, null);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void deleteNote(MainActivity activity, int position) {
        HomeFragment homeFragment = null;
        Fragment currentFrag = activity.getSupportFragmentManager().findFragmentById(R.id.frag_content_frame);
        if (currentFrag instanceof HomeFragment) {
            homeFragment = (HomeFragment) currentFrag;
        }
        if (homeFragment != null) {
            new DBHelper(homeFragment.mContext).deleteNote(homeFragment.mAdapter.getItem(position).getTitle());
            homeFragment.mNoteList.remove(position);
            homeFragment.mAdapter.notifyDataSetChanged();
            if (homeFragment.mNoteList.isEmpty())
                homeFragment.mNoNotesTV.setVisibility(View.VISIBLE);
            homeFragment.deleteButton.animate().translationX(homeFragment.displayWidth / MyConstants.DIVIDER_DELETE).setDuration(MyConstants.TRANSLATE_X_DELETE);
            homeFragment.layout.animate().translationX(MyConstants.START_POINT_TRANSLATE_X).setDuration(MyConstants.TRANSLATE_X_DELETE);
            homeFragment.deleteButton.setVisibility(View.GONE);
            homeFragment.deleteFlag = false;
        }
    }

    public void showDialog(final Context context, final HashMap<String, String> params) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        switch (params.get(MyConstants.paramsType)) {
            case MyConstants.paramsSMS: {
                final EditText input = new EditText(context);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                builder.setView(input);
                builder.setTitle(R.string.sms_dialog_title);
                builder.setMessage(R.string.sms_dialog_text);
                builder.setPositiveButton(R.string.general_dialog_send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!input.getText().toString().isEmpty())
                            sendSMS(params.get(MyConstants.paramsAddress), input.getText().toString());
                    }
                });
                break;
            }
            case MyConstants.paramsNote: {
                final EditText input = new EditText(context);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                builder.setView(input);
                builder.setTitle(R.string.note_dialog_title);
                builder.setMessage(R.string.note_dialog_text);
                builder.setPositiveButton(R.string.general_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (params.get(MyConstants.paramsEdit).equals(MyConstants.FALSE)) {
                            Note note = new Note();
                            if (params.get(MyConstants.paramsTitle).isEmpty()) {
                                note.setTitle(Calendar.getInstance().getTime().toString());
                            } else {
                                note.setTitle(params.get(MyConstants.paramsTitle));
                            }
                            if (!input.getText().toString().isEmpty()) {
                                note.setNote(input.getText().toString());
                                new DBHelper(context).addNote(note);
                            }
                        } else {
                            if (!input.getText().toString().isEmpty()) {
                                new DBHelper(context).editNote(params.get(MyConstants.paramsTitle), input.getText().toString());
                            }
                        }
                        Intent intent = new Intent(MyConstants.actionNewNote);
                        context.sendBroadcast(intent);
                    }
                });
                break;
            }
            case MyConstants.paramsContact: {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View view = inflater.inflate(R.layout.dialog_view, null);
                builder.setView(view);
                builder.setTitle(R.string.contact_dialog_title);
                builder.setMessage(R.string.contact_dialog_text);
                builder.setPositiveButton(R.string.general_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText nameView = view.findViewById(R.id.contact_name);
                        EditText phoneView = view.findViewById(R.id.contact_phone);
                        if (!(nameView.getText().toString().isEmpty() && phoneView.getText().toString().isEmpty()))
                            addNewContact(context, nameView.getText().toString(), phoneView.getText().toString());
                    }
                });
                break;
            }
            default:
                break;
        }
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        builder.show();
    }

    private void addNewContact(Context context, String name, String number) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        operations.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        if (name != null) {
            operations.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            name).build());
        }

        if (number != null) {
            operations.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }

        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operations);
        } catch (RemoteException | OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public void checkSaveEvent(Context context, HashMap<String, String> params, HashMap<String, Integer> date) {
        Calendar calendar = Calendar.getInstance();
        String eventTitle = params.get(MyConstants.eventName);
        String eventNotes = params.get(MyConstants.eventDesc);

        int year = date.get(MyConstants.eventYear);
        int month = date.get(MyConstants.eventMonth);
        int day = date.get(MyConstants.eventDay);

        String startHour = params.get(MyConstants.eventStartHour);
        String startMinute = params.get(MyConstants.eventStartMin);

        String endHour = params.get(MyConstants.eventEndHour);
        String endMinute = params.get(MyConstants.eventEndMin);

        String reminderHour = params.get(MyConstants.eventReminderHour);
        String reminderMinute = params.get(MyConstants.eventReminderMin);

        calendar.set(year, month, day, Integer.parseInt(startHour), Integer.parseInt(startMinute));
        long startMillis = calendar.getTimeInMillis();

        calendar.set(year, month, day, Integer.parseInt(endHour), Integer.parseInt(endMinute));
        long endMillis = calendar.getTimeInMillis();

        checkAddEvent(context, startMillis, endMillis, eventTitle, eventNotes, reminderHour, reminderMinute);
    }

    private void checkAddEvent(Context context, long millisStart, long millisEnd, String title, String eventNotes, String reminderHours, String reminderMinutes) {
        final String NO_REMINDER = "00";
        ContentValues contentValues = new ContentValues();

        contentValues.put(CalendarContract.Events.DTSTART, millisStart);
        contentValues.put(CalendarContract.Events.DTEND, millisEnd);
        contentValues.put(CalendarContract.Events.TITLE, title);
        contentValues.put(CalendarContract.Events.DESCRIPTION, eventNotes);
        contentValues.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CONFIRMED);
        contentValues.put(CalendarContract.Events.CALENDAR_ID, 1);
        contentValues.put(CalendarContract.Events.EVENT_TIMEZONE, Calendar.getInstance().getTimeZone().getID());

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Uri uri = context.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, contentValues);
        if (uri == null) {
            return;
        }

        if (!NO_REMINDER.equals(reminderHours) || !NO_REMINDER.equals(reminderMinutes)) {
            long eventID = Long.parseLong(uri.getLastPathSegment());
            setReminder(context, eventID, reminderHours, reminderMinutes);
        }
    }

    private void setReminder(Context context, long eventID, String reminderHours, String reminderMinutes) {
        ContentValues contentValues = new ContentValues();

        int duration = Integer.parseInt(reminderHours) * 60 + Integer.parseInt(reminderMinutes);

        contentValues.put(CalendarContract.Reminders.MINUTES, duration);
        contentValues.put(CalendarContract.Reminders.EVENT_ID, eventID);
        contentValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        context.getContentResolver().insert(CalendarContract.Reminders.CONTENT_URI, contentValues);
    }
}
