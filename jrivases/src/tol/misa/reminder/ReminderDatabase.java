package tol.misa.reminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.Time;
import android.util.Log;
import java.lang.System;


public class ReminderDatabase {
	
	public static final String KEY_DESCR = "description";
    public static final String KEY_LATIT = "latitude";
    public static final String KEY_LONGI = "longitude";
    public static final String KEY_TSTMP = "timestamp";
    public static final String KEY_STATUS = "status";
    public static final String KEY_ROWID = "_id";
    
    private static final String TAG = "ReminderDatabase";
    private DatabaseHelper mDbHelper;
    public SQLiteDatabase mDb=null;
    
    /**
     * Database creation sql statement
     */

    private static final String DATABASE_CREATE =
            "create table locations (_id integer primary key autoincrement, "
                    + "description text not null, latitude int not null, "
                    + "longitude int not null, timestamp double not null, "
                    + "status integer not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "locations";
    private static final int DATABASE_VERSION = 1;
    
    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {
    	
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS locations");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public ReminderDatabase(Context ctx) {
        this.mCtx = ctx;
    }
    
    /**
     * Open the locations database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public ReminderDatabase open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
    
    /**
     * Create a new reminder using the description provided and the coodinates 
     * of the current position. The Status is 1. If the description is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param description of the reminder
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    public long createReminder(String description, int myLat, int myLong) {
    	//Date d = new Date(-1);
    	Time time = new Time();
    	time.setToNow();

    	ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_DESCR, description);
    	initialValues.put(KEY_LATIT, myLat);
    	initialValues.put(KEY_LONGI, myLong);
    	initialValues.put(KEY_TSTMP, System.currentTimeMillis());
    	initialValues.put(KEY_STATUS, 1);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the reminder with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all reminders in the database
     * 
     * @return Cursor over all reminders
     */
    public Cursor fetchAllReminders() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_DESCR,
                KEY_LATIT, KEY_LONGI}, KEY_STATUS + "=" + 1, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_DESCR}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateNote(long rowId, String description) {
        ContentValues args = new ContentValues();
        args.put(KEY_DESCR, description);
        
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
