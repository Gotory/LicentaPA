package cosmin.licenta.Widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

import cosmin.licenta.Common.DBHelper;
import cosmin.licenta.Common.MyConstants;
import cosmin.licenta.Common.Note;
import cosmin.licenta.R;

public class ViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private ArrayList<Note> noteList;
    private Context context;
    private int appWidgetId;

    public ViewsFactory(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        noteList = new DBHelper(context).getNotes();
    }

    @Override
    public void onCreate() { }

    @Override
    public void onDestroy() { }

    @Override
    public int getCount() {
        return (noteList.size());
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.list_item_note);

        row.setTextViewText(R.id.title, noteList.get(position).getTitle());
        row.setTextViewText(R.id.note, noteList.get(position).getNote());

        Intent intent = new Intent();

        intent.putExtra(MyConstants.paramsTitle, noteList.get(position).getTitle());
        intent.putExtra(MyConstants.paramsNote, noteList.get(position).getNote());

        row.setOnClickFillInIntent(R.id.note, intent);

        return (row);
    }

    @Override
    public RemoteViews getLoadingView() {
        return (null);
    }

    @Override
    public int getViewTypeCount() {
        return (1);
    }

    @Override
    public long getItemId(int position) {
        return (position);
    }

    @Override
    public boolean hasStableIds() {
        return (true);
    }

    @Override
    public void onDataSetChanged() { }
}