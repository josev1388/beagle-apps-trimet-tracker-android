package com.beagleapps.android.trimettracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBAdapter {

	// Database fields
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_STOPID = "stopid";
	public static final String KEY_DIRECTION = "direction";
	public static final String KEY_ROUTES = "routes";
	private static final String FAVORITES_TABLE = "favorites";
	private static final String VERSION_TABLE = "version_table";
	private static final String KEY_VERSION_ID = "version_number";
	private static final String KEY_VERSION_NUMBER = "version_id";
	private Context context;
	private SQLiteDatabase database;
	private DatabaseHelper dbHelper;

	public DBAdapter(Context context) {
		this.context = context;
	}

	public DBAdapter open() throws SQLException {
		dbHelper = new DatabaseHelper(context);
		database = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	/**
	 * Checks if a stopID is in the DB
	 */
	public boolean checkForFavorite(int stopID) {
		Cursor cursor = database.query(FAVORITES_TABLE, new String[] {KEY_STOPID},
				KEY_STOPID + "=" + stopID, null, null, null, null);
		
		return cursor.getCount() > 0;
	}
	
	/**
	 * Create a new todo If the todo is successfully created return the new
	 * rowId for that note, otherwise return a -1 to indicate failure.
	 */
	public long createFavorite(Favorite newFavorite) {
		ContentValues initialValues = createContentValues(newFavorite);

		return database.insert(FAVORITES_TABLE, null, initialValues);
	}

	/**
	 * Update the todo
	 */
	public boolean updateFavorite(Favorite favorite) {
		ContentValues updateValues = createContentValues(favorite);

		return database.update(FAVORITES_TABLE, updateValues, KEY_STOPID + "="
				+ favorite.getStopID(), null) > 0;
	}

	/**
	 * Deletes todo
	 */
	public boolean deleteFavorite(int stopID) {
		return database.delete(FAVORITES_TABLE, KEY_STOPID + "=" + 
				stopID, null) > 0;
		
	}

	/**
	 * Return a Cursor over the list of all todo in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllFavorites() {
		Cursor mCursor = database.query(FAVORITES_TABLE, new String[] { KEY_STOPID,
				KEY_DESCRIPTION, KEY_DIRECTION, KEY_ROUTES }, null, null, null,
				null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * Return a Cursor positioned at the defined todo
	 */
	public Cursor fetchFavorite(long stopID) throws SQLException {
		Cursor mCursor = database.query(true, FAVORITES_TABLE, new String[] {
				KEY_STOPID, KEY_DESCRIPTION, KEY_DIRECTION, KEY_ROUTES },
				KEY_STOPID + "=" + stopID, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	/**
	 * Returns Version Number
	 */
	public String fetchVersion() throws SQLException {
		Cursor mCursor = database.query(true, VERSION_TABLE, new String[] {
				KEY_VERSION_NUMBER},
				null, null, null, null, null, null);
		if (mCursor != null && mCursor.getCount() > 0) {
			mCursor.moveToLast();
			return mCursor.getString(mCursor.getColumnIndex(KEY_VERSION_NUMBER));
		}
		else{
			return "No Version Found";
		}
		
	}

	private ContentValues createContentValues(Favorite fav) {
		ContentValues values = new ContentValues();
		values.put(KEY_DESCRIPTION, fav.getDescription());
		values.put(KEY_STOPID, fav.getStopID());
		values.put(KEY_DIRECTION, fav.getDirection());
		values.put(KEY_ROUTES, fav.getRoutes());
		return values;
	}

	// For testing purposes, clears and creates the db
	public void Create() {
		dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 2);
		
	}

	public long setCurrentVersion(String currentVersion) {
		ContentValues values = new ContentValues();
		values.put(KEY_VERSION_NUMBER, currentVersion);
		
		return database.insert(VERSION_TABLE, null, values);
	}
}

