package com.beagleapps.android.trimettracker;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class showStop extends Activity {

	private final int COUNTDOWN_DELAY = 5000;
	private final int REFRESH_DELAY = 30000;
	private final int MAX_AGE = 10;

	String TAG = "showStop";
	private int mStopID;

	private ArrayList<Arrival> mArrivals = null;
	private ArrivalAdapter arrivalAdapter;

	private TextView vStopTitle;
	private TextView vDirection;
	private ListView vArrivalsListView;
	private ArrivalsDocument mArrivalsDoc;

	private DBAdapter mDbHelper;
	private boolean mIsFavorite;

	private Handler mTimersHandler = new Handler();
	private DownloadArrivalDataTask mDownloadTask = null;
	private ProgressDialog mDialog;
	
	// Set when dialog is canceled
	private long mRefreshDelayTime;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.showstop);

		mDbHelper = new DBAdapter(this);
		mDbHelper.open();

		vArrivalsListView = (ListView)findViewById(R.id.SS_ArrivalsListView);
		vStopTitle = (TextView)findViewById(R.id.SS_StopTitle);
		vDirection= (TextView)findViewById(R.id.SS_StopID);

		mArrivals = new ArrayList<Arrival>();
		mArrivalsDoc = new ArrivalsDocument();

		mStopID = mArrivalsDoc.getStopID();
		vDirection.setText(mArrivalsDoc.getDirection());
		vStopTitle.setText(mStopID + ": " + mArrivalsDoc.getStopDescription());

		getArrivals();

		arrivalAdapter = new ArrivalAdapter(this, mArrivals);
		vArrivalsListView.setAdapter(arrivalAdapter);

		mDownloadTask = (DownloadArrivalDataTask)getLastNonConfigurationInstance();

		if (mDownloadTask != null){
			mDownloadTask.attach(this);
			if (mDownloadTask.isDone()){
				dismissDialog();
			}
			else{
				showDialog();
			}
		}
	}

	// Receiver tells the app to refresh on unlock,
	// If the window has focus
	@Override 
	protected void onResume(){
		super.onResume();
		startTimers();
	}

	private boolean isDataOutOfDate() {
		// if use cancels a dialog that's refreshing the arrival times, it will immediately refresh again
		// 		since the focus changes again and it sees that the arrivals are out of date. The refresh delay
		//		compensates for this by giving a small amount of padding
		return (mArrivalsDoc.getAge() >= MAX_AGE && getRefreshDelayAge() >= MAX_AGE);
	}

	
	private int getRefreshDelayAge() {
		return (int) ((new Date().getTime() - mRefreshDelayTime)/1000);
	}

	@Override 
	protected void onPause(){
		super.onPause();
		stopTimers();
	}
	
	@Override 
	protected void onDestroy(){
		
		if (mDownloadTask != null){
			mDownloadTask.detach();
		}
		super.onDestroy();
	}

	public void onWindowFocusChanged (boolean hasFocus){
		if (hasFocus && isDataOutOfDate()){
			refreshArrivalData();
			resetTimers();
		}
	}

	private void startTimers() {
		mTimersHandler.postDelayed(mCountDownTask, COUNTDOWN_DELAY);
		mTimersHandler.postDelayed(mRefreshTask, REFRESH_DELAY);
	}

	private void stopTimers() {
		mTimersHandler.removeCallbacks(mRefreshTask);
		mTimersHandler.removeCallbacks(mCountDownTask);
	}

	private void resetTimers() {
		stopTimers();
		startTimers();
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
		
		refreshArrivalData();
	}

	private void refreshArrivalData() {
		String urlString = new String(getString(R.string.baseArrivalURL)+ 
				mArrivalsDoc.getStopID());

		if (Connectivity.checkForInternetConnection(getApplicationContext()))
		{
			mDownloadTask = new DownloadArrivalDataTask(this);
			mDownloadTask.execute(urlString);
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

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (mDownloadTask != null){
			mDownloadTask.detach();
		}

		return(mDownloadTask);
	}

	private void setupDialog(){
		mDialog = null;
		mDialog = new ProgressDialog(this);
		mDialog.setMessage(getString(R.string.dialogGettingArrivals));
		mDialog.setIndeterminate(true);
		mDialog.setCancelable(true);
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				showStop.this.mDownloadTask.cancel(true);
			}
		};
		
		mDialog.setOnCancelListener(onCancelListener);
	}

	private void showDialog(){
		setupDialog();
		mDialog.show();
	}

	private void dismissDialog(){
		if (mDialog != null){
			mDialog.dismiss();
		}
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

	private static class DownloadArrivalDataTask extends AsyncTask<String, Void, XMLHandler> {
		private showStop activity = null;
		private boolean isDone = false;
		private final String TAG = "DownloadArrivalData asyncTask";

		DownloadArrivalDataTask(showStop activity) {
			attach(activity);
		}

		public boolean isDone() {
			return isDone;
		}

		private void attach(showStop activity) {
			this.activity = activity;

		}

		private void detach() {
			activity = null;

		}

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
			isDone = false;

			if (activity != null){
				activity.showDialog();
			}
			else{
				Log.w(TAG, "showStop activity is null");
			}
		}

		protected void onPostExecute(XMLHandler newXmlHandler) {
			isDone = true;

			if (activity != null){
				activity.dismissDialog();
				if (newXmlHandler.hasError())
					activity.showError(newXmlHandler.getError());
				else{
					ArrivalsDocument.mXMLDoc = newXmlHandler.getXmlDoc();
					ArrivalsDocument.mRequestTime = newXmlHandler.getRequestTime();
					
					activity.refreshStopList();
				}
			}
			else{
				Log.w(TAG, "showStop activity is null");
			}
		}
		
		@Override
	    protected void onCancelled() {
			isDone = true;
			activity.mRefreshDelayTime = new Date().getTime();
	    }
	}


	private Runnable mCountDownTask = new Runnable() {
		public void run() {
			refreshStopList();
			mTimersHandler.postDelayed(mCountDownTask, COUNTDOWN_DELAY);
		}
	};

	private Runnable mRefreshTask = new Runnable() {
		public void run() {
			refreshArrivalData();
			mTimersHandler.postDelayed(mRefreshTask, REFRESH_DELAY);
		}
	};
}
