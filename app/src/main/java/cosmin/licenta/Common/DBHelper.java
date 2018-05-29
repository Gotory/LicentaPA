package cosmin.licenta.Common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "PADatabase";

    private static final String TABLE_GPS = "GPDTable";
    private static final String TABLE_NOTES = "notesTable";

    private static final String NOTES_COLUMN_ID = "id";
    private static final String NOTES_COLUMN_NOTE_TITLE = "title";
    private static final String NOTES_COLUMN_NOTE = "note";

    private static final String GPS_COLUMN_ID = "id";
    private static final String GPS_COLUMN_LAST_DESTINATION = "destination";

    private static final String CREATE_TABLE_GPS = "CREATE TABLE "
            + TABLE_GPS + "("
            + GPS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + GPS_COLUMN_LAST_DESTINATION + " TEXT)";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE "
            + TABLE_NOTES + "("
            + NOTES_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + NOTES_COLUMN_NOTE_TITLE + " TEXT, "
            + NOTES_COLUMN_NOTE + " TEXT)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_USERS);
        sqLiteDatabase.execSQL(CREATE_TABLE_GPS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_GPS);
        onCreate(sqLiteDatabase);
    }

    public void addDest(String dest) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(GPS_COLUMN_LAST_DESTINATION, dest);

        db.insert(TABLE_GPS, null, values);

        db.close();
    }

    public ArrayList<String> getDestinations() {
        SQLiteDatabase db = getReadableDatabase();

        ArrayList<String> destinationList = new ArrayList<>();

        Cursor cursor = db.query(TABLE_GPS, new String[]{GPS_COLUMN_ID, GPS_COLUMN_LAST_DESTINATION},
                null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            destinationList.add(cursor.getString(1));
        }

        cursor.close();
        db.close();

        return destinationList;
    }

    public void deleteDestination(String destination) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_GPS, GPS_COLUMN_LAST_DESTINATION + "=?", new String[]{destination});
        db.close();
    }

    public void deleteAllDestinations() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_GPS, null, null);
        db.close();
    }

    public void addNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NOTES_COLUMN_NOTE_TITLE, note.getTitle());
        values.put(NOTES_COLUMN_NOTE, note.getNote());

        db.insert(TABLE_NOTES, null, values);

        db.close();
    }

    public void editNote(String title, String newTitle, String newText) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        if(!newText.isEmpty()) {
            values.put(NOTES_COLUMN_NOTE, newText);
        }
        if(!newTitle.isEmpty()) {
            values.put(NOTES_COLUMN_NOTE_TITLE, newTitle);
        }

        db.update(TABLE_NOTES, values, NOTES_COLUMN_NOTE_TITLE + "= ?", new String[]{title});
        db.close();
    }

    public ArrayList<Note> getNotes() {
        SQLiteDatabase db = getReadableDatabase();

        ArrayList<Note> noteList = new ArrayList<>();

        Cursor cursor = db.query(TABLE_NOTES, new String[]{NOTES_COLUMN_ID, NOTES_COLUMN_NOTE_TITLE, NOTES_COLUMN_NOTE},
                null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            Note note = new Note();
            note.setNote(cursor.getString(2));
            note.setTitle(cursor.getString(1));
            noteList.add(note);
        }

        cursor.close();
        db.close();

        return noteList;
    }

    public void deleteNote(String title) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NOTES, NOTES_COLUMN_NOTE_TITLE + "=?", new String[]{title});
        db.close();
    }

    public void deleteAllNotes() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NOTES, null, null);
        db.close();
    }

}
