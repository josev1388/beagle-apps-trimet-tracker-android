package com.beagleapps.android.trimettracker;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class showStop extends Activity {
	
	@SuppressWarnings("unused")
	private String TAG = "showStop";
	
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
	         // to do
	         return true;
	     case R.id.menuFavorite:
	         onMenuFavoriteClick();
	         return true;
	     default:
	         return super.onOptionsItemSelected(item);
	     }
	 }

	 private void onMenuFavoriteClick() {
		if (isStopFavorite()){
			// Remove stop from favorites
			mDbHelper.deleteFavorite(mArrivalsDoc.getStopID());
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
		fav.setDescription(mArrivalsDoc.getStopDescription());
		fav.setStopID(mArrivalsDoc.getStopID());
		fav.setDirection("");
		fav.setRoutes("");
		return fav;
	}

	private void getArrivals()
	 {
		 mArrivals = new ArrayList<Arrival>();
		 int length = mArrivalsDoc.getNumArrivals();
		 
		 for (int index = 0; index < length; index++){
			 Arrival newArrival = new Arrival();
			 
			 newArrival.setBusDescription(mArrivalsDoc.getBusDescription(index));
			 newArrival.setScheduledTime(mArrivalsDoc.getScheduledTime(index));
			 if (mArrivalsDoc.isEstimated(index)){
				 newArrival.setArrivalTime(mArrivalsDoc.getEstimatedTime(index));
			 }
			 else{
				 newArrival.setArrivalTime("");
			 }
			 newArrival.setRemainingMinutes(mArrivalsDoc.getRemainingMinutes(index));
			 mArrivals.add(newArrival);
		 }
	 }
}
