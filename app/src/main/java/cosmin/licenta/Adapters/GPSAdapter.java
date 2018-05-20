package cosmin.licenta.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cosmin.licenta.R;

public class GPSAdapter extends ArrayAdapter<String> {

    private ArrayList<String> mDestinationsList;
    private Context mContext;
    private String destination;

    public GPSAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
        super(context, textViewResourceId, objects);
        mContext = context;
        mDestinationsList = objects;
    }

    private class ViewHolder {
        TextView mDestination;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final GPSAdapter.ViewHolder viewHolder;

        if (position < mDestinationsList.size()) {
            destination = mDestinationsList.get(position);
        }

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_gps, parent, false);

            viewHolder = new GPSAdapter.ViewHolder();
            viewHolder.mDestination = convertView.findViewById(R.id.destination);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (GPSAdapter.ViewHolder) convertView.getTag();
        }

        viewHolder.mDestination.setText(destination);

        return convertView;
    }

    @Override
    public int getPosition(String item) {
        return super.getPosition(item);
    }

    @Override
    public String getItem(int position) {
        return mDestinationsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return mDestinationsList.size();
    }

}
