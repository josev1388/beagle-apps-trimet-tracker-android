package com.beagleapps.android.trimettracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	// Database fields
		public static final String KEY_DESCRIPTION = "description";
		public static final String KEY_STOPID = "stopid";
		public static final String KEY_DIRECTION = "direction";
		public static final String KEY_ROUTES = "routes";
		private static final String FAVORITES_TABLE = "favorites";
		private static final String VERSION_TABLE = "version_table";
		private static final String KEY_VERSION_ID = "version_number";
		private static final String KEY_VERSION_NUMBER = "version_id";
	private static final String DATABASE_NAME = "applicationdata";

	private static final int DATABASE_VERSION = 2;

	// Database creation sql statement
	private static final String FAVORITE_TABLE_CREATE = "create table "+ FAVORITES_TABLE+ " ("+KEY_STOPID+" integer primary key, "
			+ KEY_DESCRIPTION + " text not null, "+KEY_ROUTES+" text not null, "+KEY_DIRECTION+" text not null);";
	private static final String VERSION_TABLE_CREATE= "create table "+VERSION_TABLE+" ("+KEY_VERSION_ID+" integer primary key, "
			+ KEY_VERSION_NUMBER+" text not null);";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(FAVORITE_TABLE_CREATE);
		database.execSQL(VERSION_TABLE_CREATE);
	}

	// Method is called during an update of the database, e.g. if you increase
	// the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(DatabaseHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS "+FAVORITES_TABLE+";");
		database.execSQL("DROP TABLE IF EXISTS "+VERSION_TABLE+";");
		onCreate(database);
	}
}
