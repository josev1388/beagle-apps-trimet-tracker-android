package com.beagleapps.android.trimettracker;

import java.net.MalformedURLException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class showStop extends Activity {
	
	private String TAG = "showStop";
	private int mStopID;
	
	private ArrayList<Arrival> mArrivals = null;
	private ArrivalAdapter arrivalAdapter;
	
	private TextView stopTitle;
	private ListView arrivalsListView;
	private ArrivalsDocument mArrivalsDoc;
	
	private DBAdapter mDbHelper;
	private boolean mIsFavorite;
	
	 @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        setContentView(R.layout.showstop);
	        
	        mDbHelper = new DBAdapter(this);
			mDbHelper.open();
	        
	        arrivalsListView = (ListView)findViewById(R.id.arrivalsListView);
	        stopTitle = (TextView)findViewById(R.id.stopTitle);
	        
	        mArrivals = new ArrayList<Arrival>();
	        mArrivalsDoc = new ArrivalsDocument(ArrivalsDocument.arrivalsDoc);
	        
	        stopTitle.setText(mArrivalsDoc.getStopDescription());
	        mStopID = mArrivalsDoc.getStopID();
	        
	        getArrivals();
	        
	        arrivalAdapter = new ArrivalAdapter(this, mArrivals);
	        arrivalsListView.setAdapter(arrivalAdapter);
	 }
	 
	 private boolean isStopFavorite() {
		int stopID = mArrivalsDoc.getStopID();
		
		mIsFavorite = mDbHelper.checkForFavorite(stopID);
		
		return mIsFavorite;
	}

	@Override
	 public boolean onCreateOptionsMenu(Menu menu) {
	     MenuInflater inflater = getMenuInflater();
	     inflater.inflate(R.menu.showstop_menu, menu);
	     return true;
	 }
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		// See if the stop is favorited
		MenuItem favoriteButton = menu.findItem(R.id.menuFavorite);
	     if (isStopFavorite()){
	    	 favoriteButton.setTitle(R.string.favorited);
	    	 favoriteButton.setIcon(R.drawable.rate_star_med_on);
	     }
	     else{
	    	 favoriteButton.setTitle(R.string.addToFavorites);
	    	 favoriteButton.setIcon(R.drawable.ic_menu_star);
	     }
        return super.onPrepareOptionsMenu(menu);
    }
	 
	 @Override
	 public boolean onOptionsItemSelected(MenuItem item) {
	     // Handle item selection
	     switch (item.getItemId()) {
	     case R.id.menuRefresh:
	         onRefreshClick();
	         return true;
	     case R.id.menuFavorite:
	         onFavoriteClick();
	         return true;
	     default:
	         return super.onOptionsItemSelected(item);
	     }
	 }

	private void onRefreshClick() {
		String urlString = new String(getString(R.string.baseArrivalURL)+ 
				mArrivalsDoc.getStopID());
		
		if (Connectivity.checkForInternetConnection(getApplicationContext()))
        {
        	new DownloadArrivalData().execute(urlString);
        }
        else{
    		Connectivity.showErrorToast(getApplicationContext());
        }
		
	}
	
	private void refreshStopList() {
		getArrivals();
		arrivalAdapter.notifyDataSetChanged();
	}

	private void onFavoriteClick() {
		if (isStopFavorite()){
			// Remove stop from favorites
			mDbHelper.deleteFavorite(mStopID);
			ShowRemovedToast();
		}
		else{
			// Add stop to favorites
			mDbHelper.createFavorite(constructFavorite());
			ShowAddedToast();
		}
		
	}

	private void ShowAddedToast() {
		Toast.makeText(getApplicationContext(), getString(R.string.stopFavorited),
				Toast.LENGTH_SHORT).show();
		
	}
	
	private void ShowRemovedToast() {
		Toast.makeText(getApplicationContext(), getString(R.string.stopRemoved),
				Toast.LENGTH_SHORT).show();
		
	}

	private Favorite constructFavorite() {
		Favorite fav = new Favorite();
		if (mArrivalsDoc != null){
			fav.setDescription(mArrivalsDoc.getStopDescription());
			fav.setStopID(mArrivalsDoc.getStopID());
			fav.setDirection(mArrivalsDoc.getDirection());
			String routes = RoutesUtilities.parseRouteList(mArrivalsDoc.getRouteList());
			fav.setRoutes(routes);
		}
		return fav;
	}

	private void getArrivals()
	 {
		 mArrivals.clear();
		 
		 
		 if(mArrivalsDoc != null){
			 int length = mArrivalsDoc.getNumArrivals();
			 
			 for (int index = 0; index < length; index++){
				 Arrival newArrival = new Arrival();
				 
				 newArrival.setBusDescription(mArrivalsDoc.getBusDescription(index));
				 newArrival.setScheduledTime(mArrivalsDoc.getScheduledTime(index));
				 newArrival.setScheduledTimeText(mArrivalsDoc.getScheduledTimeText(index));
				 if (mArrivalsDoc.isEstimated(index)){
					 newArrival.setArrivalTime(mArrivalsDoc.getEstimatedTime(index));
					 newArrival.setEstimated(true);
				 }
				 else{
					 newArrival.setArrivalTime("");
					 newArrival.setEstimated(false);
				 }
				 newArrival.setRemainingMinutes(mArrivalsDoc.getRemainingMinutes(index));
				 mArrivals.add(newArrival);
			 }
		 }
	 }
	
	protected void showError(String error) {
		Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
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
            
            if (newXmlHandler.hasError())
        		showError(newXmlHandler.getError());
        	else{
        		mArrivalsDoc = new ArrivalsDocument(newXmlHandler.getXmlDoc());
        		refreshStopList();
        	}
        }
    }
}
