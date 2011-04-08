package com.beagleapps.android.trimettracker;

import java.net.MalformedURLException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
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
	
	private static ArrivalsDocument mXmlArrivalsDoc;
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
            	if (stopIDTextBox.getText().length() > 0){
            		onStopClick(Integer.parseInt(stopIDTextBox.getText().toString()));
            	}
            	else{
            		showError(getString(R.string.errorNoStopID));
            	}
            }
        });
        
        favoriteStopsListView.setOnItemClickListener(new OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> a, View v, int position, long id) {
        		onStopClick(mFavorites.get(position).getStopID());
        	}
        });

    }
    
    public void onWindowFocusChanged (boolean hasFocus){
    	if(hasFocus){
    		getFavorites();
    		favoriteAdapter.notifyDataSetChanged();
    	}
    }

	protected void onStopClick(int stopID) {
		String urlString = new String(getString(R.string.baseArrivalURL)+ stopID);
        
        
    	if (Connectivity.checkForInternetConnection(getApplicationContext()))
        {
        	new DownloadArrivalData().execute(urlString);
        }
        else{
    		Connectivity.showErrorToast(getApplicationContext());
        }
		
	}
	
	protected void showStop() {
		Intent showStopIntent = new Intent();
		showStopIntent.setClass(getApplicationContext(), showStop.class);
    	startActivity(showStopIntent);
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
		fav.setDirection(cursor.getString(cursor.getColumnIndex(DBAdapter.KEY_DIRECTION)));
		fav.setRoutes(cursor.getString(cursor.getColumnIndex(DBAdapter.KEY_ROUTES)));
		return fav;
	}


	public void setXmlArrivalsDoc(ArrivalsDocument xmlArrivalsDoc) {
		homepage.mXmlArrivalsDoc = xmlArrivalsDoc;
	}


	public ArrivalsDocument getXmlArrivalsDoc() {
		return mXmlArrivalsDoc;
	}
	
	private Context getDialogContext() {
	    Context context;
	    if (getParent() != null) context = getParent();
	    else context = this;
	    return context;
	}
	
	private class DownloadArrivalData extends AsyncTask<String, Void, XMLHandler> {
		private ProgressDialog dialog = new ProgressDialog(getDialogContext());
		
        protected XMLHandler doInBackground(String... urls) {
        	XMLHandler newXmlHandler = null;
        	try {
        		newXmlHandler = new XMLHandler(urls[0]);
        		newXmlHandler.refreshXmlData();
			} catch (MalformedURLException e) {
				Log.e(TAG, e.getMessage());
			}
			return newXmlHandler;
        }

        protected void onPreExecute() {
        	dialog.setMessage(getString(R.string.dialogGettingArrivals));
        	dialog.setIndeterminate(true);
        	dialog.setCancelable(false);
        	dialog.show();
        }
        
        protected void onPostExecute(XMLHandler newXmlHandler) {
        	dialog.dismiss();
        	
            mXmlArrivalsDoc = new ArrivalsDocument(newXmlHandler.getXmlDoc());
            
            if (newXmlHandler.hasError())
        		showError(newXmlHandler.getError());
        	else{
        		showStop();
        	}
        }
    }
    
    
}