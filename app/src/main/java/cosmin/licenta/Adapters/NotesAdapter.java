package cosmin.licenta.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import cosmin.licenta.Common.Note;
import cosmin.licenta.R;

public class NotesAdapter extends ArrayAdapter<Note> {

    private ArrayList<Note> mNoteList;
    private Context mContext;
    private Note mNote;

    public NotesAdapter(Context context, int textViewResourceId, ArrayList<Note> objects) {
        super(context, textViewResourceId, objects);
        mContext = context;
        mNoteList = objects;
        mNote = new Note();
    }

    private class ViewHolder {
        TextView mNote;
        TextView mTitle;
        ImageButton mDeleteBtn;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final NotesAdapter.ViewHolder viewHolder;

        if (position < mNoteList.size()) {
            mNote = mNoteList.get(position);
        }

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_note, parent, false);

            viewHolder = new NotesAdapter.ViewHolder();
            viewHolder.mNote = convertView.findViewById(R.id.note);
            viewHolder.mTitle = convertView.findViewById(R.id.title);
            viewHolder.mDeleteBtn = convertView.findViewById(R.id.delete_btn);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (NotesAdapter.ViewHolder) convertView.getTag();
        }

        viewHolder.mNote.setText(mNote.getNote());
        viewHolder.mTitle.setText(mNote.getTitle());

        viewHolder.mDeleteBtn.setVisibility(View.GONE);

        return convertView;
    }

    @Override
    public int getPosition(Note item) {
        return super.getPosition(item);
    }

    @Override
    public Note getItem(int position) {
        return mNoteList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return mNoteList.size();
    }

}
