package cosmin.licenta.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import cosmin.licenta.R;


public class TimerFragment extends Fragment {

    private boolean started;
    private Timer timer;
    private int sec, min, milli;
    private TextView secView, minView, milliView;
    private String second, minute, millisecond;
    private ArrayAdapter adapter;
    private ArrayList<String> timeList;
    private ListView lapList;
    private Button startButton;

    public TimerFragment() {
    }

    public static TimerFragment newInstance() {
        return new TimerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_timer, container, false);

        started = false;

        secView = rootView.findViewById(R.id.textSec);
        minView = rootView.findViewById(R.id.textMin);
        milliView = rootView.findViewById(R.id.textMilli);
        sec = 0;
        min = 0;
        milli = 0;

        timeList = new ArrayList<>();

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, timeList);

        lapList = rootView.findViewById(R.id.LapList);
        lapList.setAdapter(adapter);

        startButton = rootView.findViewById(R.id.StartBtn);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!started) {
                    startButton.setText(getContext().getString(R.string.stop_timer));
                    startTimer();
                } else {
                    startButton.setText(getContext().getString(R.string.start_timer));
                    stopTimer();
                }
            }
        });

        rootView.findViewById(R.id.LapBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printLap();
            }
        });

        rootView.findViewById(R.id.ResetBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        stopTimer();
        super.onDetach();
    }

    public void startTimer() {
        started = true;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                milli++;
                if (milli == 99) {
                    sec++;
                    milli = 0;
                }
                if (sec == 59) {
                    min++;
                    sec = 0;
                }
                if (min == 59) {
                    min = 0;
                    stopTimer();
                }
                if (milli < 10) {
                    millisecond = "0" + String.valueOf(milli);
                } else {
                    millisecond = String.valueOf(milli);
                }
                if (sec < 10) {
                    second = "0" + String.valueOf(sec) + ":";
                } else {
                    second = String.valueOf(sec) + ":";
                }
                if (min < 10) {
                    minute = "0" + String.valueOf(min) + ":";
                } else {
                    minute = String.valueOf(min) + ":";
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        secView.setText(second);
                        minView.setText(minute);
                        milliView.setText(millisecond);
                    }
                });
            }
        }, 0, 10);
    }

    public void stopTimer() {
        if (timer != null) {
            started = false;
            timer.cancel();
            timer.purge();
        }
    }

    public void resetTimer() {
        stopTimer();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sec = 0;
                min = 0;
                milli = 0;
                secView.setText("00:");
                minView.setText("00:");
                milliView.setText("00");
            }
        }, 200);

    }

    public void printLap() {
        String lap = minView.getText().toString()+secView.getText().toString()+milliView.getText().toString();
        timeList.add(lap);
        adapter.notifyDataSetChanged();
    }
}
