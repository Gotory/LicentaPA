package cosmin.licenta.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import cosmin.licenta.Common.Contact;
import cosmin.licenta.R;

public class ContactsAdapter extends ArrayAdapter<Contact> implements Filterable {

    private ArrayList<Contact> mContactsList;
    private Context mContext;
    private Contact contact;

    public ContactsAdapter(Context context, int textViewResourceId, ArrayList<Contact> objects) {
        super(context, textViewResourceId, objects);
        mContext = context;
        mContactsList = objects;
        contact = new Contact();
    }

    private class ViewHolder {
        TextView mName;
        TextView mTelephone;
        ImageButton mCallBtn;
        ImageButton mSmsBtn;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;

        if (position < mContactsList.size()) {
            contact = mContactsList.get(position);
        }

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_contact, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.mName = convertView.findViewById(R.id.name);
            viewHolder.mTelephone = convertView.findViewById(R.id.phone);
            viewHolder.mCallBtn = convertView.findViewById(R.id.call_button);
            viewHolder.mSmsBtn = convertView.findViewById(R.id.sms_button);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.mName.setText(contact.getName());
        viewHolder.mTelephone.setText(contact.getPhoneNumber());

        return convertView;
    }

    @Override
    public int getPosition(Contact item) {
        return super.getPosition(item);
    }

    @Override
    public Contact getItem(int position) {
        return mContactsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return mContactsList.size();
    }

}
