package com.beagleapps.android.trimettracker;

import java.net.MalformedURLException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beagleapps.android.trimettrackerfree.DBAdapter;
import com.beagleapps.android.trimettrackerfree.FavoriteAdapter;

public class MainView extends Activity {
	/** Called when the activity is first created. */

	@SuppressWarnings("unused")
	private static String TAG = "homepage";

	private ArrayList<Favorite> mFavorites = null;
	private FavoriteAdapter mFavoriteAdapter;
	private DBAdapter mDbHelper;

	private ListView vFavoriteStopsListView;
	private View vEmptyView;
	
	//private PopupWindow vWelcomePopup;

	private TextView vStopIDTextBox;
	private Button vGoButton;

	private static ArrivalsDocument mArrivalsDoc;
	ArrayAdapter<String> mListViewAdapter;
	
	private DownloadArrivalDataTask mDownloadArrivalTask = null;
	private DownloadRoutesDataTask mDownloadRoutesTask = null;
	private ProgressDialog mArrivalsDialog;
	private ProgressDialog mRoutesDialog;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		mDbHelper = new DBAdapter(this);
		mDbHelper.open();

		vFavoriteStopsListView = (ListView)findViewById(R.id.favoriteStopsListView);
		vEmptyView = (View)findViewById(R.id.HP_emptyView);
		vGoButton = (Button)findViewById(R.id.goButton);
		vStopIDTextBox = (TextView)findViewById(R.id.stopIDTextBox);

		mFavorites = new ArrayList<Favorite>();

		getFavorites();

		mFavoriteAdapter = new FavoriteAdapter(this, mFavorites);
		vFavoriteStopsListView.setAdapter(mFavoriteAdapter);
		
		vFavoriteStopsListView.setEmptyView(vEmptyView);

		setupListeners();
		
		handleRotation();

		checkVersion();
	}


	private void checkVersion() {
		try {
			String currentVersion = ManifestUtils.getCurrentVersion(this);
			String dbVersion = mDbHelper.fetchVersion();
			//showError("Manifest Version: "+currentVersion+ " DB Version: "+ dbVersion);
			if (dbVersion.compareTo(currentVersion) != 0 && !dbVersion.equals(mDbHelper.NO_VERSION_FOUND)){
				mDbHelper.setCurrentVersion(currentVersion);
				showNewFeaturesDialog();
			}
		} catch (Exception e) {
			showError(e.toString());
		}
		
	}

	private void setupListeners() {
		vGoButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (vStopIDTextBox.getText().length() > 0){
					onStopClick(Integer.parseInt(vStopIDTextBox.getText().toString()));
				}
				else{
					showError(getString(R.string.errorNoStopID));
				}
			}
		});

		vFavoriteStopsListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				onStopClick(mFavorites.get(position).getStopID());
			}
		});
	}

	// Resumes the dialog if an async task is still in progress after rotation
	private void handleRotation() {
		HomepageRotationInstance instance = (HomepageRotationInstance)getLastNonConfigurationInstance();
		
		if (instance != null){
			mDownloadArrivalTask = instance.getDownloadArrivalTask();
			mDownloadRoutesTask = instance.getDownloadRoutesTask();
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
		else if (mDownloadRoutesTask != null){
			mDownloadRoutesTask.attach(this);
			if (mDownloadRoutesTask.isDone()){
				dismissRoutesDialog();
			}
			else{
				showRoutesDialog();
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.homepage_menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_findStop) {
			onFindStopClick();
			return true;
		} else if (item.getItemId() == R.id.menu_nearbyStops) {
			onFindNearybyClick();
			return true;
		} else if (item.getItemId() == R.id.menu_about) {
			onAboutClick();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void onAboutClick() {
		Intent aboutIntent = new Intent();
		aboutIntent.setClass(getApplicationContext(), AboutScreen.class);
		startActivity(aboutIntent);
	}

	private void onFindNearybyClick() {
		Intent showStopIntent = new Intent();
		showStopIntent.setClass(getApplicationContext(), FindNearby.class);
		startActivity(showStopIntent);
	}

	private void showNewFeaturesDialog() {
		try {
			final Dialog dialog = new Dialog(MainView.this);
	        dialog.setContentView(R.layout.welcome_popup);
	        dialog.setTitle(R.string.PopupTitle);
	        dialog.setCancelable(true);

	        //set up text
	        TextView text = (TextView) dialog.findViewById(R.id.PopupMessage);
	        text.setText(R.string.PopupMessage);

	        //set up button
	        Button button = (Button) dialog.findViewById(R.id.PopupButton);
	        button.setOnClickListener(new OnClickListener() {
		        public void onClick(View v) {
		                dialog.dismiss();
	            }
	        });
	       
	        LayoutParams params = dialog.getWindow().getAttributes(); 
            params.width = LayoutParams.FILL_PARENT; 
            dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
	        dialog.show();
		} catch (Exception e) {
			showError(e.toString());
		}
	}
	

	private void onFindStopClick() {
		String urlString = new String(getString(R.string.baseRoutesURL));
		
		if (Connectivity.checkForInternetConnection(getApplicationContext()))
		{
			mDownloadRoutesTask = new DownloadRoutesDataTask(this);
			mDownloadRoutesTask.execute(urlString);
		}
		else{
			Connectivity.showErrorToast(getApplicationContext());
		}
		
	}

	@Override 
	protected void onPause(){
		super.onPause();
		//mDbHelper.close();
	}
	
	@Override 
	protected void onDestroy(){
		super.onDestroy();
		//mDbHelper.close();
	}

	public void onWindowFocusChanged (boolean hasFocus){
		if(hasFocus){
			getFavorites();
			mFavoriteAdapter.notifyDataSetChanged();
		}
	}

	protected void onStopClick(int stopID) {
		String urlString = new String(getString(R.string.baseArrivalURL)+ stopID);


		if (Connectivity.checkForInternetConnection(getApplicationContext()))
		{
			mDownloadArrivalTask = new DownloadArrivalDataTask(this);
			mDownloadArrivalTask.execute(urlString);
		}
		else{
			Connectivity.showErrorToast(getApplicationContext());
		}

	}

	protected void launchShowStop() {
		Intent showStopIntent = new Intent();
		showStopIntent.setClass(getApplicationContext(), ShowStop.class);
		startActivity(showStopIntent);
	}
	
	public void launchChooseRoute() {
		Intent chooseRouteIntent = new Intent();
		chooseRouteIntent.setClass(getApplicationContext(), ChooseRoute.class);
		startActivity(chooseRouteIntent);
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
		MainView.mArrivalsDoc = xmlArrivalsDoc;
	}


	public ArrivalsDocument getXmlArrivalsDoc() {
		return mArrivalsDoc;
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		if (mDownloadArrivalTask != null){
			mDownloadArrivalTask.detach();
		}
		if (mDownloadRoutesTask != null){
			mDownloadRoutesTask.detach();
		}
		
		return(new HomepageRotationInstance(mDownloadArrivalTask, mDownloadRoutesTask));
	}
	
	private void setupArrivalsDialog(){
		mArrivalsDialog = null;
		mArrivalsDialog = new ProgressDialog(this);
		mArrivalsDialog.setMessage(getString(R.string.dialogGettingArrivals));
		mArrivalsDialog.setIndeterminate(true);
		mArrivalsDialog.setCancelable(true);
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				MainView.this.mDownloadArrivalTask.cancel(true);
			}
		};
		
		mArrivalsDialog.setOnCancelListener(onCancelListener);
	}
	
	private void setupRoutesDialog(){
		mRoutesDialog = null;
		mRoutesDialog = new ProgressDialog(this);
		mRoutesDialog.setMessage(getString(R.string.dialogGettingRoutes));
		mRoutesDialog.setIndeterminate(true);
		mRoutesDialog.setCancelable(true);
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				MainView.this.mDownloadRoutesTask.cancel(true);
			}
		};
		
		mRoutesDialog.setOnCancelListener(onCancelListener);
	}

	void showArrivalsDialog(){
		setupArrivalsDialog();
		mArrivalsDialog.show();
	}

	void dismissArrivalsDialog(){
		if (mArrivalsDialog != null){
			mArrivalsDialog.dismiss();
		}
	}
	
	void showRoutesDialog(){
		setupRoutesDialog();
		mRoutesDialog.show();
	}

	void dismissRoutesDialog(){
		if (mRoutesDialog != null){
			mRoutesDialog.dismiss();
		}
	}
	
	class DownloadRoutesDataTask extends DownloadXMLAsyncTask<MainView> {
		
		DownloadRoutesDataTask(MainView activity) {
			super(activity);
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
				activity.showRoutesDialog();
			}
			else{
				Log.w(TAG, "homepage activity is null");
			}
		}

		protected void onPostExecute(XMLHandler newXmlHandler) {
			isDone = true;
			
			if (activity != null){
				activity.dismissRoutesDialog();
				if (newXmlHandler.hasError())
					activity.showError(newXmlHandler.getError());
				else{
					RoutesDocument.mRouteXMLDoc = newXmlHandler.getXmlDoc();
					
					activity.launchChooseRoute();
				}
			}
			else{
				Log.w(TAG, "homepage activity is null");
			}
		}
	}
	
	class DownloadArrivalDataTask extends DownloadXMLAsyncTask<MainView> {
	
		DownloadArrivalDataTask(MainView activity) {
			super(activity);
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
					
					activity.launchShowStop();
				}
			}
			else{
				Log.w(TAG, "showStop activity is null");
			}
		}
	}
}