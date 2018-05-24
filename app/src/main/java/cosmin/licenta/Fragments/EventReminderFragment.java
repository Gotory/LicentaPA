package cosmin.licenta.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import cosmin.licenta.Common.Helper;
import cosmin.licenta.Common.MyConstants;
import cosmin.licenta.R;


public class EventReminderFragment extends Fragment {

    private Context mContext;

    private CalendarView calendarView;
    private Button addEvent;
    private EditText startTimeHour, startTimeMin, endTimeHour, endTimeMin, titleET, descriptionET, reminderTimeHour, reminderTimeMin;

    private HashMap<String, String> params;
    private HashMap<String, Integer> eventDate;

    private Date currentTime;

    public EventReminderFragment() {
    }


    public static EventReminderFragment newInstance() {
        return new EventReminderFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_event_reminder, container, false);
        mContext = getContext();
        calendarView = rootView.findViewById(R.id.calendarView);

        startTimeHour = rootView.findViewById(R.id.start_time_hour);
        startTimeMin = rootView.findViewById(R.id.start_time_minute);
        endTimeHour = rootView.findViewById(R.id.end_time_hour);
        endTimeMin = rootView.findViewById(R.id.end_time_minute);
        titleET = rootView.findViewById(R.id.event_name);
        descriptionET = rootView.findViewById(R.id.event_desc);
        reminderTimeHour = rootView.findViewById(R.id.reminder_time_hour);
        reminderTimeMin = rootView.findViewById(R.id.reminder_time_minute);

        params = new HashMap<>();
        eventDate = new HashMap<>();


        currentTime = Calendar.getInstance().getTime();
        startTimeHour.setText(DateFormat.format("kk ", currentTime));
        startTimeMin.setText(DateFormat.format("mm", currentTime));
        endTimeHour.setText(DateFormat.format("kk ", currentTime));
        endTimeMin.setText(DateFormat.format("mm", currentTime));
        reminderTimeHour.setText("00");
        reminderTimeMin.setText("00");
        eventDate.put(MyConstants.eventDay, Integer.valueOf((String) DateFormat.format("dd", currentTime)));
        eventDate.put(MyConstants.eventMonth, Integer.valueOf((String) DateFormat.format("MM", currentTime)));
        eventDate.put(MyConstants.eventYear, Integer.valueOf((String) DateFormat.format("yyyy", currentTime)));

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                eventDate.put(MyConstants.eventDay, dayOfMonth);
                eventDate.put(MyConstants.eventMonth, month);
                eventDate.put(MyConstants.eventYear, year);
            }
        });

        addEvent = rootView.findViewById(R.id.add_event);
        addEvent.getBackground().setAlpha(50);
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                params.put(MyConstants.eventName, titleET.getText().toString());
                params.put(MyConstants.eventDesc, descriptionET.getText().toString());
                params.put(MyConstants.eventStartHour, startTimeHour.getText().toString());
                params.put(MyConstants.eventEndHour, endTimeHour.getText().toString());
                params.put(MyConstants.eventReminderHour, reminderTimeHour.getText().toString());
                params.put(MyConstants.eventStartMin, startTimeMin.getText().toString());
                params.put(MyConstants.eventEndMin, endTimeMin.getText().toString());
                params.put(MyConstants.eventReminderMin, reminderTimeMin.getText().toString());
                if (checkFields()) {
                    titleET.setText("");
                    descriptionET.setText("");
                    startTimeHour.setText("");
                    endTimeHour.setText("");
                    reminderTimeHour.setText("");
                    startTimeMin.setText("");
                    endTimeMin.setText("");
                    reminderTimeMin.setText("");
                    Toast.makeText(mContext, getString(R.string.event_added), Toast.LENGTH_SHORT).show();
                    Helper.getInstance().checkSaveEvent(mContext, params, eventDate);
                }
            }
        });
        //todo find a way to show events

        return rootView;
    }

    private Boolean checkFields() {
        if (descriptionET.getText().toString().isEmpty()) {
            params.put(MyConstants.eventDesc, "");
        }
        if (endTimeHour.getText().toString().isEmpty() && !endTimeMin.getText().toString().isEmpty()) {
            params.put(MyConstants.eventEndHour, startTimeHour.getText().toString());
        }
        if (endTimeHour.getText().toString().isEmpty() && endTimeMin.getText().toString().isEmpty()) {
            params.put(MyConstants.eventEndHour, startTimeHour.getText().toString());
            params.put(MyConstants.eventEndMin, startTimeMin.getText().toString());
        }
        if(){

        } else {
            if (Integer.valueOf(startTimeHour.getText().toString()) < Integer.valueOf((String) DateFormat.format("kk", currentTime))) {
                startTimeHour.setText(DateFormat.format("kk", currentTime));
                params.put(MyConstants.eventStartHour, (String) DateFormat.format("kk", currentTime));
            }
            if (startTimeHour.getText().equals(DateFormat.format("kk", currentTime)) && Integer.valueOf(startTimeMin.getText().toString()) < Integer.valueOf((String) DateFormat.format("mm", currentTime))) {
                startTimeMin.setText(DateFormat.format("mm", currentTime));
                params.put(MyConstants.eventStartMin, (String) DateFormat.format("mm", currentTime));
            }
            if (Integer.valueOf(endTimeHour.getText().toString()) < Integer.valueOf(startTimeHour.getText().toString())) {
                endTimeHour.setText(startTimeHour.getText().toString());
                params.put(MyConstants.eventEndHour, startTimeHour.getText().toString());
            }
            if (endTimeHour.getText().equals(startTimeHour.getText()) && Integer.valueOf(endTimeMin.getText().toString()) < Integer.valueOf(startTimeMin.getText().toString())) {
                endTimeMin.setText(startTimeMin.getText().toString());
                params.put(MyConstants.eventEndMin, startTimeMin.getText().toString());
            }
        }
        if (reminderTimeHour.getText().toString().isEmpty()) {
            params.put(MyConstants.eventReminderHour, "00");
        }
        if (reminderTimeMin.getText().toString().isEmpty()) {
            params.put(MyConstants.eventReminderMin, "00");
        }
        if (titleET.getText().toString().isEmpty() || startTimeHour.getText().toString().isEmpty() || startTimeMin.getText().toString().isEmpty()) {
            Toast.makeText(mContext, getString(R.string.toast_complete_fields), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
