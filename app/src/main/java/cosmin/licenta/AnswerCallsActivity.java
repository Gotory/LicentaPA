package cosmin.licenta;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import cosmin.licenta.Common.MyConstants;
import cosmin.licenta.Common.NotificationReceiverService;


public class AnswerCallsActivity extends Activity {
    private KeyguardManager keyguardManager;
    private BroadcastReceiver finishActivityReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_calls);

        final Context context = getApplicationContext();
        final Activity activity = this;

        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        updateWindowFlags();

        TextView contactView = findViewById(R.id.contact_name_view);
        TextView numberView = findViewById(R.id.number_view);
        String caller = "";

        MainActivity.isAppActive = true;

        IntentFilter filter = new IntentFilter();
        filter.addAction(MyConstants.actionFinish);
        finishActivityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                activity.finish();
            }
        };

        registerReceiver(finishActivityReceiver, filter);

        String number = getIntent().getExtras().getString(MyConstants.intent_incoming_number);
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        ContentResolver resolver = context.getContentResolver();
        Cursor contactLookup = resolver.query(uri, null, null, null, null);
        try {
            if (contactLookup != null && contactLookup.getCount() > 0) {
                contactLookup.moveToNext();
                caller = contactLookup.getString(contactLookup.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            } else {
                caller = getResources().getString(R.string.contact_unknown);
            }
        } finally {
            if (contactLookup != null) {
                contactLookup.close();
            }
        }

        numberView.setText(number);
        contactView.setText(caller);

        final Button answerButton = findViewById(R.id.accept_Btn);
        if (!getIntent().getExtras().getBoolean(MyConstants.show_answer_button)) {
            answerButton.setVisibility(View.GONE);
        } else {
            answerButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View v) {
                    answerButton.setVisibility(View.GONE);
                    if (Build.VERSION.SDK_INT < 21) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Runtime.getRuntime().exec("input keyevent " + Integer.toString(79));
                                } catch (IOException e) {
                                    String str = "android.permission.CALL_PRIVILEGED";
                                    Intent putExtra = new Intent("android.intent.action.MEDIA_BUTTON").putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(0, 79));
                                    Intent putExtra2 = new Intent("android.intent.action.MEDIA_BUTTON").putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(1, 79));
                                    getApplicationContext().sendOrderedBroadcast(putExtra, str);
                                    getApplicationContext().sendOrderedBroadcast(putExtra2, str);
                                }
                            }
                        }).start();
                    } else {
                        sendHeadsetHookLollipop(getApplicationContext());
                    }
                    int delay = 1000;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent startMain = new Intent(context, AnswerCallsActivity.class);
                            startMain.setAction(MyConstants.actionMoveToFront);
                            startMain.setClass(context, AnswerCallsActivity.class);
                            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startMain.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            context.startActivity(startMain);
                        }
                    }, delay);
                }
            });
        }

        Button declineButton = findViewById(R.id.decline_Btn);
        declineButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                killCall(context);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public void killCall(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            @SuppressWarnings("rawtypes")
            Class clazz;
            if (tm != null) {
                clazz = Class.forName(tm.getClass().getName());
                Method method = clazz.getDeclaredMethod("getITelephony");
                method.setAccessible(true);
                Object telephonyService = method.invoke(tm);
                clazz = Class.forName(telephonyService.getClass().getName());
                method = clazz.getDeclaredMethod("endCall");
                method.setAccessible(true);
                method.invoke(telephonyService);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void sendHeadsetHookLollipop(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaSessionManager mediaSessionManager = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
            try {
                List<MediaController> mediaControllerList;
                mediaControllerList = mediaSessionManager.getActiveSessions(new ComponentName(context, NotificationReceiverService.class));

                for (MediaController m : mediaControllerList) {
                    if ("com.android.server.telecom".equals(m.getPackageName())) {
                        m.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK));
                        break;
                    }
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateWindowFlags() {
        if (keyguardManager.inKeyguardRestrictedInputMode()) {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        } else {
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(finishActivityReceiver);
    }
}
