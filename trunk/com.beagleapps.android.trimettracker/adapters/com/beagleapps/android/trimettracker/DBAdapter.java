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
	private static final String DATABASE_TABLE = "favorites";
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
		Cursor cursor = database.query(DATABASE_TABLE, new String[] {KEY_STOPID},
				KEY_STOPID + "=" + stopID, null, null, null, null);
		
		return cursor.getCount() > 0;
	}
	
	/**
	 * Create a new todo If the todo is successfully created return the new
	 * rowId for that note, otherwise return a -1 to indicate failure.
	 */
	public long createFavorite(Favorite newFavorite) {
		ContentValues initialValues = createContentValues(newFavorite);

		return database.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * Update the todo
	 */
	public boolean updateFavorite(Favorite favorite) {
		ContentValues updateValues = createContentValues(favorite);

		return database.update(DATABASE_TABLE, updateValues, KEY_STOPID + "="
				+ favorite.getStopID(), null) > 0;
	}

	/**
	 * Deletes todo
	 */
	public boolean deleteFavorite(int stopID) {
		return database.delete(DATABASE_TABLE, KEY_STOPID + "=" + 
				stopID, null) > 0;
		
	}

	/**
	 * Return a Cursor over the list of all todo in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllFavorites() {
		Cursor mCursor = database.query(DATABASE_TABLE, new String[] { KEY_STOPID,
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
		Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {
				KEY_STOPID, KEY_DESCRIPTION, KEY_DIRECTION, KEY_ROUTES },
				KEY_STOPID + "=" + stopID, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	private ContentValues createContentValues(Favorite fav) {
		ContentValues values = new ContentValues();
		values.put(KEY_DESCRIPTION, fav.getDescription());
		values.put(KEY_STOPID, fav.getStopID());
		values.put(KEY_DIRECTION, fav.getDirection());
		values.put(KEY_ROUTES, fav.getRoutes());
		return values;
	}
}

