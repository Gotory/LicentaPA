package cosmin.licenta.Fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import cosmin.licenta.Adapters.GPSAdapter;
import cosmin.licenta.Common.DBHelper;
import cosmin.licenta.R;

import static android.net.Uri.parse;


public class GpsFragment extends Fragment {

    public Context mContext;

    private EditText mDestinationET;

    public GPSAdapter mAdapter = null;
    public ArrayList<String> mDestinationList;

    public GpsFragment() {
    }

    public static GpsFragment newInstance() {
        return new GpsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_gps, container, false);

        mContext = getContext();
        ListView mListView = rootView.findViewById(R.id.last_location_list);

        mDestinationList = new DBHelper(mContext).getDestinations();
        mAdapter = new GPSAdapter(mContext, R.layout.list_item_gps, mDestinationList);

        mListView.setAdapter(mAdapter);

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                new DBHelper(mContext).deleteDestination(mAdapter.getItem(position));
                mDestinationList.remove(position);
                mAdapter.notifyDataSetChanged();
                return false;
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String destination = mAdapter.getItem(position);
                destination = destination.replaceAll(" ", "+");
                Uri gmmIntentUri = parse("google.navigation:q=" + destination);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        mDestinationET = rootView.findViewById(R.id.nav_destination);

        rootView.findViewById(R.id.nav_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String destination = mDestinationET.getText().toString();
                mDestinationET.setText("");
                if(!destination.isEmpty()) {
                    if(!mDestinationList.contains(destination)) {
                        new DBHelper(mContext).addDest(destination);
                    }
                    destination = destination.replaceAll(" ", "+");
                    Uri gmmIntentUri = parse("google.navigation:q=" + destination);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
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
        super.onDetach();
    }
}
