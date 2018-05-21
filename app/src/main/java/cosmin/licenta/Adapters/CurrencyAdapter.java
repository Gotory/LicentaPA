package cosmin.licenta.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cosmin.licenta.Common.Exchange;
import cosmin.licenta.R;

public class CurrencyAdapter extends ArrayAdapter<Exchange> {

    private ArrayList<Exchange> mDestinationsList;
    private Context mContext;
    private Exchange exchangeRate;

    public CurrencyAdapter(Context context, int textViewResourceId, ArrayList<Exchange> objects) {
        super(context, textViewResourceId, objects);
        mContext = context;
        mDestinationsList = objects;
    }

    private class ViewHolder {
        TextView mName;
        TextView mRate;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final CurrencyAdapter.ViewHolder viewHolder;

        if (position < mDestinationsList.size()) {
            exchangeRate = mDestinationsList.get(position);
        }

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_currency, parent, false);

            viewHolder = new CurrencyAdapter.ViewHolder();
            viewHolder.mName = convertView.findViewById(R.id.type);
            viewHolder.mRate = convertView.findViewById(R.id.rate);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (CurrencyAdapter.ViewHolder) convertView.getTag();
        }

        viewHolder.mName.setText(mContext.getString(R.string.currency_type,exchangeRate.getType()));
        viewHolder.mRate.setText(exchangeRate.getRate());

        return convertView;
    }

    @Override
    public int getPosition(Exchange item) {
        return super.getPosition(item);
    }

    @Override
    public Exchange getItem(int position) {
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
