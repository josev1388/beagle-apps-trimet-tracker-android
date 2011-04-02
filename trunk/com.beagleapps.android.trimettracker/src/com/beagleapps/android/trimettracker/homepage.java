package com.beagleapps.android.trimettracker;

import java.net.MalformedURLException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class homepage extends Activity {
    /** Called when the activity is first created. */
	
	private String TAG = "homepage";
	
	private ArrayList<Favorite> mFavorites = null;
	private FavoriteAdapter favoriteAdapter;
	private DBAdapter mDbHelper;
	
	private ListView favoriteStopsListView;
	private TextView stopIDTextBox;
	private Button goButton;
	private static ArrivalsDocument xmlArrivalsDoc;
	ArrayAdapter<String> listViewAdapter;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        mDbHelper = new DBAdapter(this);
		mDbHelper.open();
        
        favoriteStopsListView = (ListView)findViewById(R.id.favoriteStopsListView);
        goButton = (Button)findViewById(R.id.goButton);
        stopIDTextBox = (TextView)findViewById(R.id.stopIDTextBox);
        
        mFavorites = new ArrayList<Favorite>();
        
        getFavorites();
        
        favoriteAdapter = new FavoriteAdapter(this, mFavorites);
        favoriteStopsListView.setAdapter(favoriteAdapter);
        
        goButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	showStop(Integer.parseInt(stopIDTextBox.getText().toString()));
            }
        });
        
        favoriteStopsListView.setOnItemClickListener(new OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> a, View v, int position, long id) {
        		showStop(mFavorites.get(position).getStopID());
        	}
        });

    }
    
    public void onWindowFocusChanged (boolean hasFocus){
    	if(hasFocus){
    		getFavorites();
    		favoriteAdapter.notifyDataSetChanged();
    	}
    }


	protected void showStop(int stopID) {
		String urlString = new String(getString(R.string.baseArrivalURL)+ stopID);
        XMLHandler xmlHandler = null;
        
        
    	if (Connectivity.checkForInternetConnection(getApplicationContext()))
        {
        	try {
				xmlHandler = new XMLHandler(urlString);
				xmlHandler.refreshXmlData();
				setXmlArrivalsDoc(new ArrivalsDocument(xmlHandler.getXmlDoc()));
			} catch (MalformedURLException e) {
				Log.e(TAG, e.getMessage());
			}
        	
        	if (xmlHandler.hasError())
        		showError(xmlHandler.getError());
        	else{
        		Intent showStopIntent = new Intent();
        		showStopIntent.setClass(getApplicationContext(), showStop.class);
            	startActivity(showStopIntent);
        	}
        }
        else{
    		Connectivity.showErrorToast(getApplicationContext());
        }
		
	}


	protected void showError(String error) {
		Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
	}


	private void getFavorites() {
		Cursor cursor = mDbHelper.fetchAllFavorites();
		cursor.moveToFirst();
		
		mFavorites.clear();
		while (!cursor.isAfterLast()){
			Favorite fav = constructFavoriteFromCursor(cursor); 
			mFavorites.add(fav);
			cursor.moveToNext();
		}
	}


	private Favorite constructFavoriteFromCursor(Cursor cursor) {
		Favorite fav = new Favorite();
		fav.setDescription(cursor.getString(cursor.getColumnIndex(DBAdapter.KEY_DESCRIPTION)));
		fav.setStopID(cursor.getInt(cursor.getColumnIndex(DBAdapter.KEY_STOPID)));
		return fav;
	}


	public void setXmlArrivalsDoc(ArrivalsDocument xmlArrivalsDoc) {
		homepage.xmlArrivalsDoc = xmlArrivalsDoc;
	}


	public ArrivalsDocument getXmlArrivalsDoc() {
		return xmlArrivalsDoc;
	}
    
    
}