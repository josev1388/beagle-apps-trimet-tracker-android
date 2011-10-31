package com.beagleapps.android.trimettracker;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
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
	private DownloadArrivalDataTask mDownloadArrivalTask = null;
	private DownloadDetourDataTask mDownloadDetourTask = null;
	private ProgressDialog mArrivalsDialog;
	private ProgressDialog mDetoursDialog;
	private Button mDetourButton;
	private View mBottomDivider;
	private LinearLayout mBottomBar;
	
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
		mDetourButton = (Button)findViewById(R.id.SS_DetourButton);
		mBottomBar = (LinearLayout)findViewById(R.id.SS_BottomBar);
		mBottomDivider = (View)findViewById(R.id.SS_BottomDivider);

		mArrivals = new ArrayList<Arrival>();
		mArrivalsDoc = new ArrivalsDocument();

		mStopID = mArrivalsDoc.getStopID();
		vDirection.setText(mArrivalsDoc.getDirection());
		vStopTitle.setText(mStopID + ": " + mArrivalsDoc.getStopDescription());

		getArrivals();

		arrivalAdapter = new ArrivalAdapter(this, mArrivals);
		vArrivalsListView.setAdapter(arrivalAdapter);

		handleRotation();
		setupListeners();
		
		// Check for detours, display button
		handleDetours();
	}

	private void handleRotation() {
		ShowStopRotationInstance instance = (ShowStopRotationInstance)getLastNonConfigurationInstance();
		
		if (instance != null){
			mDownloadArrivalTask = instance.getDownloadArrivalTask();
			mDownloadDetourTask = instance.getDownloadDetoursTask();
		}
		
		if (mDownloadArrivalTask != null){
			mDownloadArrivalTask.attach(this);
			if (mDownloadArrivalTask.isDone()){
				dismissArrivalsDialog();
			}
			else{
				showArrivalsDialog();
			}
		}
		else if (mDownloadDetourTask != null){
			mDownloadDetourTask.attach(this);
			if (mDownloadDetourTask.isDone()){
				dismissDetoursDialog();
			}
			else{
				showDetoursDialog();
			}
		}
	}
	
	private void handleDetours() {
		int length = mArrivals.size();
		boolean hasDetour = false;
	
		for (int index = 0; index < length; index++) {
			if (mArrivalsDoc.hasDetour(index)){
				hasDetour = true;
				break;
			}
		}
		
		if(hasDetour){
			showDetourButton();
		}
	}

	private void setupListeners() {
		mDetourButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onDetourClick();
			}
		});
	}
	
	private void onDetourClick() {
		String routeListString = RoutesUtilities.join(mArrivalsDoc.getRouteList(), ",");
		String urlString = new String(getString(R.string.baseDetourUrl) + routeListString);


		if (Connectivity.checkForInternetConnection(getApplicationContext()))
		{
			stopTimers();
			mDownloadDetourTask = new DownloadDetourDataTask(this);
			mDownloadDetourTask.execute(urlString);
		}
		else{
			Connectivity.showErrorToast(getApplicationContext());
		}
	}
	
	private void hideDetourButton() {
		mBottomBar.setVisibility(View.GONE);
		mBottomDivider.setVisibility(View.GONE);
	}
	
	private void showDetourButton() {
		mBottomBar.setVisibility(View.VISIBLE);
		mBottomDivider.setVisibility(View.VISIBLE);
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
		
		if (mDownloadArrivalTask != null){
			mDownloadArrivalTask.detach();
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
	
	private void resetRefreshDelay() {
		mRefreshDelayTime = new Date().getTime();
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
			mDownloadArrivalTask = new DownloadArrivalDataTask(this);
			mDownloadArrivalTask.execute(urlString);
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
		if (mDownloadArrivalTask != null){
			mDownloadArrivalTask.detach();
		}
		if (mDownloadDetourTask != null){
			mDownloadDetourTask.detach();
		}
		
		return(new ShowStopRotationInstance(mDownloadArrivalTask, mDownloadDetourTask));
	}


	private void showDetoursDialog(){
		setupDetoursDialog();
		mDetoursDialog.show();
	}

	private void dismissDetoursDialog(){
		if (mDetoursDialog != null){
			mDetoursDialog.dismiss();
		}
	}
	
	private void setupDetoursDialog(){
		mDetoursDialog = null;
		mDetoursDialog = new ProgressDialog(this);
		mDetoursDialog.setMessage(getString(R.string.dialogGettingDetours));
		mDetoursDialog.setIndeterminate(true);
		mDetoursDialog.setCancelable(true);
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				showStop.this.mDownloadDetourTask.cancel(true);
			}
		};
		
		mDetoursDialog.setOnCancelListener(onCancelListener);
	}
	
	private void setupArrivalsDialog(){
		mArrivalsDialog = null;
		mArrivalsDialog = new ProgressDialog(this);
		mArrivalsDialog.setMessage(getString(R.string.dialogGettingArrivals));
		mArrivalsDialog.setIndeterminate(true);
		mArrivalsDialog.setCancelable(true);
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				showStop.this.mDownloadArrivalTask.cancel(true);
			}
		};
		
		mArrivalsDialog.setOnCancelListener(onCancelListener);
	}

	private void showArrivalsDialog(){
		setupArrivalsDialog();
		mArrivalsDialog.show();
	}

	private void dismissArrivalsDialog(){
		if (mArrivalsDialog != null){
			mArrivalsDialog.dismiss();
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
	
	protected void launchShowDetour() {
		Intent showStopIntent = new Intent();
		showStopIntent.setClass(getApplicationContext(), showDetour.class);
		startActivity(showStopIntent);
	}

	protected void showError(String error) {
		Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
	}

	class DownloadArrivalDataTask extends AsyncTask<String, Void, XMLHandler> {
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
				activity.showArrivalsDialog();
			}
			else{
				Log.w(TAG, "showStop activity is null");
			}
		}

		protected void onPostExecute(XMLHandler newXmlHandler) {
			isDone = true;

			if (activity != null){
				activity.dismissArrivalsDialog();
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
			activity.resetRefreshDelay();
	    }

		
	}

	
	class DownloadDetourDataTask extends AsyncTask<String, Void, XMLHandler> {
		private showStop activity = null;
		private boolean isDone = false;
		private final String TAG = "DownloadDetourData asyncTask";

		DownloadDetourDataTask(showStop activity) {
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
				activity.showDetoursDialog();
			}
			else{
				Log.w(TAG, "showStop activity is null");
			}
		}

		protected void onPostExecute(XMLHandler newXmlHandler) {
			isDone = true;

			if (activity != null){
				activity.dismissDetoursDialog();
				if (newXmlHandler.hasError())
					activity.showError(newXmlHandler.getError());
				else{
					DetoursDocument.mXMLDoc = newXmlHandler.getXmlDoc();
					
					activity.launchShowDetour();
				}
			}
			else{
				Log.w(TAG, "showStop activity is null");
			}
		}
		
		@Override
	    protected void onCancelled() {
			isDone = true;
			activity.startTimers();
			activity.resetRefreshDelay();
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
