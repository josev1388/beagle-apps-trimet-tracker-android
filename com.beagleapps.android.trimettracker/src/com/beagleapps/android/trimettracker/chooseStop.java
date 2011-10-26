package com.beagleapps.android.trimettracker;

import java.net.MalformedURLException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class chooseStop extends Activity {

	private TextView vRouteDesc;
	private TextView vDirectionDesc;
	private RoutesDocument mRoutesDocument = null;
	private ArrayList<Stop> mStopList;
	private ListView vStopListView;
	private ArrayList<Stop> mAdapterList;
	private StopAdapter mStopListAdapter;
	private ProgressDialog mArrivalsDialog;
	private DownloadArrivalDataTask mDownloadArrivalTask = null;
	private int mChosenDirection;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choosestop);

		vRouteDesc = (TextView)findViewById(R.id.CS_route_description);
		vDirectionDesc = (TextView)findViewById(R.id.CS_direction_description);
		mRoutesDocument = new RoutesDocument();

		vRouteDesc.setText(mRoutesDocument.getStopsRouteDescription());
		vDirectionDesc.setText(mRoutesDocument.getDirDescription(RoutesDocument.getChosenDirection()));

		mChosenDirection = RoutesDocument.getChosenDirection();
		
		mStopList = new ArrayList<Stop>();

		vStopListView = (ListView)findViewById(R.id.CS_ListView);

		setupListeners();
		
		handleRotation();

		setupStopList();
	}

	private void setupListeners() {

		vStopListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				onStopClick(mStopList.get(position).getStopID());
			}
		});
	}
	
	private void handleRotation() {
		mDownloadArrivalTask = (DownloadArrivalDataTask)getLastNonConfigurationInstance();
		
		if (mDownloadArrivalTask != null){
			mDownloadArrivalTask.attach(this);
			if (mDownloadArrivalTask.isDone()){
				dismissDialog();
			}
			else{
				showDialog();
			}
		}
	}

	private void setupStopList() {
		if (RoutesDocument.mStopsXMLDoc != null){
			int length = mRoutesDocument.getStopNodes(mChosenDirection).getLength();

			for (int index = 0; index < length; index++){
				String desc = mRoutesDocument.getStopDescription(mChosenDirection, index);
				String stopID = mRoutesDocument.getStopID(mChosenDirection, index);

				mStopList.add(new Stop(desc, stopID));
			}
			mAdapterList = new ArrayList<Stop>(mStopList);
			mStopListAdapter = new StopAdapter(this, mAdapterList);

			vStopListView.setAdapter(mStopListAdapter);

		}
		else {
			showError(getString(R.string.errorGeneric));
		}

	}
	
	protected void onStopClick(String stopID) {
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
	
	private void setupDialog(){
		mArrivalsDialog = null;
		mArrivalsDialog = new ProgressDialog(this);
		mArrivalsDialog.setMessage(getString(R.string.dialogGettingArrivals));
		mArrivalsDialog.setIndeterminate(true);
		mArrivalsDialog.setCancelable(true);
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				chooseStop.this.mDownloadArrivalTask.cancel(true);
			}
		};
		
		mArrivalsDialog.setOnCancelListener(onCancelListener);
	}
	
	void showDialog(){
		setupDialog();
		mArrivalsDialog.show();
	}

	void dismissDialog(){
		if (mArrivalsDialog != null){
			mArrivalsDialog.dismiss();
		}
	}
	
	protected void showError(String error) {
		Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
	}
	
	protected void launchShowStop() {
		Intent showStopIntent = new Intent();
		showStopIntent.setClass(getApplicationContext(), showStop.class);
		startActivity(showStopIntent);
	}
	
	class DownloadArrivalDataTask extends AsyncTask<String, Void, XMLHandler> {
		private chooseStop activity = null;
		private boolean isDone = false;
		private final String TAG = "DownloadArrivalData asyncTask";

		DownloadArrivalDataTask(chooseStop activity) {
			attach(activity);
		}

		public boolean isDone() {
			return isDone;
		}

		public void attach(chooseStop activity) {
			this.activity = activity;

		}

		public void detach() {
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
					
					activity.launchShowStop();
				}
			}
			else{
				Log.w(TAG, "showStop activity is null");
			}
		}
		
		@Override
	    protected void onCancelled() {
	        isDone = true;
	    }
	}
}
