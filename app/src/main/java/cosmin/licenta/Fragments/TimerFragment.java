package cosmin.licenta.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import cosmin.licenta.R;


public class TimerFragment extends Fragment {

    private boolean started;
    private Timer timer;
    private int sec, min;
    private TextView secView, minView;
    private String second, minute;

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
        sec = 0;
        min = 0;

        rootView.findViewById(R.id.timer_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
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
        started = false;
        sec = 0;
        min = 0;
        timer.cancel();
        timer.purge();
        super.onDetach();
    }

    public void startTimer(){
        if (!started) {
            started = true;
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    sec++;
                    if (sec == 59) {
                        min++;
                        sec = 0;
                    }
                    if (min == 59) {
                        min = 0;
                    }
                    if (sec < 10) {
                        second = "0" + String.valueOf(sec);
                    } else {
                        second = String.valueOf(sec);
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
                        }
                    });
                }
            }, 0, 1000);
        } else {
            started = false;
            sec = 0;
            min = 0;
            timer.cancel();
            timer.purge();
        }
    }
}
