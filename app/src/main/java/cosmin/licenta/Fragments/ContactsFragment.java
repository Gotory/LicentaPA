package cosmin.licenta.Fragments;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import cosmin.licenta.Adapters.ContactsAdapter;
import cosmin.licenta.Common.Contact;
import cosmin.licenta.Common.Helper;
import cosmin.licenta.Common.MyConstants;
import cosmin.licenta.R;


public class ContactsFragment extends Fragment {

    private Context mContext;
    private ListView mListView;
    private ContactsAdapter mAdapter = null;
    private Activity activity;
    public ArrayList<Contact> mContactsList;
    private ArrayList<Contact> originalContactsList;
    private boolean flag;
    private int displayWidth;
    private View buttonsLayout;
//    private View layout;

    public ContactsFragment() {
    }

    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        mContext = getContext();
        mListView = rootView.findViewById(R.id.contacts_list);

        if (activity == null) {
            activity = getActivity();
        }
        flag = false;

        mContactsList = new ArrayList<>();
        originalContactsList = new ArrayList<>();
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (!flag) {
                    displayWidth = view.getWidth();
                    buttonsLayout = view.findViewById(R.id.contact_buttons_layout);
                    buttonsLayout.animate().translationX(MyConstants.START_POINT_TRANSLATE_X).setDuration(MyConstants.TRANSLATE_X_DELETE);
                    buttonsLayout.setVisibility(View.VISIBLE);
                    ImageButton mCallBtn = view.findViewById(R.id.call_button);
                    ImageButton mSMSBtn = view.findViewById(R.id.sms_button);

                    final String phone = mAdapter.getItem(position).getPhoneNumber();

                    mCallBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Helper.getInstance().isPhoneNumber(phone)) {
                                Helper.getInstance().callNumber(phone, mContext);
                            }
                        }
                    });
                    mSMSBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (Helper.getInstance().isPhoneNumber(phone)) {
                                HashMap<String, String> params = new HashMap<>();
                                params.put(MyConstants.paramsType, MyConstants.paramsSMS);
                                params.put(MyConstants.paramsAddress, phone);
                                Helper.getInstance().showDialog(mContext, params);
                            }
                        }
                    });
                    flag = true;
                } else {
                    buttonsLayout.animate().translationX(displayWidth / MyConstants.DIVIDER_DELETE).setDuration(MyConstants.TRANSLATE_X_DELETE);
                    buttonsLayout.setVisibility(View.GONE);
                    flag = false;
                }
            }
        });

        new PopulateContactListAsync().execute();

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search_contact_list:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //the options menu contains a simple search mechanism
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_contact_list, menu);

        MenuItem searchBar = menu.findItem(R.id.action_search_contact_list);
        SearchView searchView = (SearchView) searchBar.getActionView();
        searchView.setFocusable(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filter(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText.trim());
                return true;
            }
        });
    }

    //filter for search
    public void filter(String charText) {

        charText = charText.toLowerCase(Locale.getDefault());

        mContactsList.clear();
        mAdapter.notifyDataSetChanged();

        if (charText.length() == 0) {
            mContactsList.addAll(originalContactsList);
            mAdapter.notifyDataSetChanged();
        } else {
            for (Contact contact : originalContactsList) {
                if (charText.length() != 0 && contact.getName().toLowerCase(Locale.getDefault()).contains(charText)) {
                    mContactsList.add(contact);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private class PopulateContactListAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... args) {
            mContactsList.clear();
            originalContactsList.clear();
            ContentResolver contentResolver = mContext.getContentResolver();

            Cursor contactsCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME);
            if (contactsCursor == null) {
                return null;
            }

            while (contactsCursor.moveToNext()) {
                String id = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Contact contact = new Contact();
                contact.setName(name);
                if (Integer.parseInt(contactsCursor.getString(contactsCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE}, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    if (phoneCursor == null) {
                        return null;
                    }
                    while (phoneCursor.moveToNext()) {
                        String phone = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contact.setPhoneNumber(phone);
                    }
                    mContactsList.add(contact);
                    originalContactsList.add(contact);
                    phoneCursor.close();
                }
            }
            contactsCursor.close();
            return "ok";
        }

        @Override
        protected void onPostExecute(String file_url) {
            mAdapter = new ContactsAdapter(mContext, R.layout.list_item_contact, mContactsList);
            mListView.setAdapter(mAdapter);
        }
    }
}
