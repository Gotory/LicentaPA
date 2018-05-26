package cosmin.licenta.Fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import cosmin.licenta.Adapters.NotesAdapter;
import cosmin.licenta.Common.DBHelper;
import cosmin.licenta.Common.Helper;
import cosmin.licenta.Common.MyConstants;
import cosmin.licenta.Common.Note;
import cosmin.licenta.MainActivity;
import cosmin.licenta.R;

public class HomeFragment extends Fragment {

    public Context mContext;
    private MainActivity activity;

    public TextView mNoNotesTV;
    public ImageButton deleteButton;

    public boolean deleteFlag;
    public int displayWidth;

    public NotesAdapter mAdapter = null;

    private BroadcastReceiver newNoteReceiver;
    private IntentFilter filter;

    public ArrayList<Note> mNoteList;

//    public View layout;

    public HomeFragment() {
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mContext = getContext();
        ListView mListView = rootView.findViewById(R.id.note_list);

        mNoNotesTV = rootView.findViewById(R.id.no_notes);

        filter = new IntentFilter();
        filter.addAction(MyConstants.actionNewNote);
        newNoteReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mNoNotesTV.setVisibility(View.GONE);
                new PopulateNoteListAsync().execute();
            }
        };

        mContext.registerReceiver(newNoteReceiver, filter);

        if (activity == null) {
            if (getActivity() instanceof MainActivity)
                activity = (MainActivity) getActivity();
        }
        deleteFlag = false;
        mNoteList = new ArrayList<>();
        mAdapter = new NotesAdapter(mContext, R.layout.list_item_note, mNoteList);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (!deleteFlag) {
                    displayWidth = view.getWidth();
//                    layout = view.findViewById(R.id.noteLayout);
//                    layout.animate().translationX(-displayWidth / MyConstants.DIVIDER_DELETE).setDuration(MyConstants.TRANSLATE_X_DELETE);
                    deleteButton = view.findViewById(R.id.delete_btn);
                    deleteButton.setVisibility(View.VISIBLE);
                    deleteButton.animate().translationX(MyConstants.START_POINT_TRANSLATE_X).setDuration(MyConstants.TRANSLATE_X_DELETE);
                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Helper.getInstance().deleteNote(activity, position);
                        }
                    });
                    deleteFlag = true;
                } else {
                    deleteButton.animate().translationX(displayWidth / MyConstants.DIVIDER_DELETE).setDuration(MyConstants.TRANSLATE_X_DELETE);
//                    layout.animate().translationX(MyConstants.START_POINT_TRANSLATE_X).setDuration(MyConstants.TRANSLATE_X_DELETE);
                    deleteButton.setVisibility(View.GONE);
                    deleteFlag = false;
                }
            }
        });

        //todo update damn widget.

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> params = new HashMap<>();
                params.put(MyConstants.paramsType, MyConstants.paramsNote);
                params.put(MyConstants.paramsEdit, MyConstants.TRUE);
                params.put(MyConstants.paramsTitle, mAdapter.getItem(position).getTitle());
                params.put(MyConstants.paramsNote, mAdapter.getItem(position).getNote());
                Helper.getInstance().showDialog(getContext(), params);
                return false;
            }
        });

//        Button mAddNoteBtn = rootView.findViewById(R.id.add_note);
//        mAddNoteBtn.getBackground().setAlpha(50);
//        mAddNoteBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                HashMap<String, String> params = new HashMap<>();
//                params.put(MyConstants.paramsType, MyConstants.paramsNote);
//                params.put(MyConstants.paramsEdit, MyConstants.FALSE);
//                params.put(MyConstants.paramsTitle, mTitleEditText.getText().toString());
//                Helper.getInstance().showDialog(getContext(), params);
//                mTitleEditText.setText("");
//            }
//        });
        ImageButton mStartListeningBtn = rootView.findViewById(R.id.start_listening_btn);
        mStartListeningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = activity.getSharedPreferences(MyConstants.PREFS_NAME, Context.MODE_PRIVATE);

                if (!prefs.getString(MyConstants.prefsUser, "").equals("")) {
                    activity.tts.speak(activity.getString(R.string.hello_user, prefs.getString(MyConstants.prefsUser, "")), TextToSpeech.QUEUE_FLUSH, null);
                } else {
                    activity.tts.speak(activity.getString(R.string.hello_user_default), TextToSpeech.QUEUE_FLUSH, null);
                }
                activity.listenAfterDelay(2000, false, activity);
            }
        });

        new PopulateNoteListAsync().execute();

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_note:
                HashMap<String, String> params = new HashMap<>();
                params.put(MyConstants.paramsType, MyConstants.paramsNote);
                params.put(MyConstants.paramsEdit, MyConstants.FALSE);
                Helper.getInstance().showDialog(getContext(), params);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //the options menu contains a simple search mechanism
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home, menu);

//        MenuItem addNote = menu.findItem(R.id.action_add_note);
//        addNote.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                return false;
//            }
//        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (newNoteReceiver != null)
            mContext.registerReceiver(newNoteReceiver, filter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity.unregisterReceiver(newNoteReceiver);

    }

    private class PopulateNoteListAsync extends AsyncTask<String, String, String> {
        private ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(mContext);
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... args) {
            mNoteList.clear();
            try {
                mNoteList.addAll(new DBHelper(getContext()).getNotes());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            return "ok";
        }

        @Override
        protected void onPostExecute(String file_url) {
            if (mNoteList.isEmpty())
                mNoNotesTV.setVisibility(View.VISIBLE);
            else
                mNoNotesTV.setVisibility(View.GONE);
            mAdapter.notifyDataSetChanged();
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        }
    }
}
