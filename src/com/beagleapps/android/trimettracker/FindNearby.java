package com.beagleapps.android.trimettracker;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class FindNearby extends MapActivity {
	private MapView vMapView;
	private LocationHandler.LocationResult mLocationResult;
	private Location mCurrentLocation;
	private StopItemizedOverlay mStopOverlay;
	private GPSMarkerItemizedOverylay mGPSOverlay;
	private DownloadNearbyStopsDataTask mDownloadNearbyStopsTask;
	private ProgressDialog mFindingStopsDialog;
	private ProgressDialog mGettingGPSDialog;

	private NearbyStopsDocument mStopsDocument;
	private LocationHandler mLocationHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.findnearby);
		
		mStopsDocument = new NearbyStopsDocument();

		vMapView = (MapView) findViewById(R.id.FNBMapView);
		vMapView.setBuiltInZoomControls(true);

		mCurrentLocation = null;

		setupMapOverylays();

		getGPSLocation();
		
		
		// TODO I'm disabling rotation for now, might fix later
		//handleRotation();

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.find_nearby_menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menuRefresh:
			onRefreshClick();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void onRefreshClick() {
		getGPSLocation();
	}

	/*private void handleRotation() {
		mDownloadNearbyStopsTask = (DownloadNearbyStopsDataTask)getLastNonConfigurationInstance();
		
		if (mDownloadNearbyStopsTask != null){
			mDownloadNearbyStopsTask.attach(this);
			if (mDownloadNearbyStopsTask.isDone()){
				dismissFindingStopsDialog();
			}
			else{
				showFindingStopsDialog();
			}
		}
	}*/
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		if (mDownloadNearbyStopsTask != null){
			mDownloadNearbyStopsTask.detach();
		}

		return(mDownloadNearbyStopsTask);
	}

	private void setupMapOverylays() {
		Drawable flagMarker = this.getResources().getDrawable(R.drawable.overlay_bus_icon);
		Drawable GPSMarker = this.getResources().getDrawable(R.drawable.overlay_crosshair);
		
		mStopOverlay = new StopItemizedOverlay(flagMarker, this);
		mGPSOverlay = new GPSMarkerItemizedOverylay(GPSMarker, this);
	}

	// Takes lat/lon as double, converts to GeoPoint
	private GeoPoint getGeoPoint(double lat, double lon) {
		return new GeoPoint((int) (lat * 1000000), (int) (lon * 1000000));
	}

	// Takes location, converts to GeoPoint
	private GeoPoint getGeoPoint(Location location) {
		// multiply by 1E6, convert to int to match what GeoPoint expects
		return new GeoPoint((int) (location.getLatitude()* 1000000), (int) (location.getLongitude() * 1000000));
	}

	private void getGPSLocation() {
		mLocationResult = new LocationHandler.LocationResult() {
			@Override
			public void gotLocation(Location location) {
				mCurrentLocation = new Location(location);
				
				if(mCurrentLocation != null){
					centerMapOnLocation(mCurrentLocation);
					
					// Clear out the overlays first
					vMapView.getOverlays().clear();
					placeGPSMarker(mCurrentLocation);
					dismissGettingGPSDialog();
					getTrimetData(mCurrentLocation);
				}
				else{
					showError(getString(R.string.problemGettingGPS));
				}
				
			}
		};

		mLocationHandler = new LocationHandler();
		boolean success = mLocationHandler.getLocation(this, mLocationResult);
		showGettingGPSDialog();
		
		if(!success){
			showError(getString(R.string.GPSDisabled));
		}

	}

	protected void placeGPSMarker(Location location) {
		OverlayItem currentPosition = new OverlayItem(getGeoPoint(location), null, null);
		
		// Add overlay item
		mGPSOverlay.clearOverlays();
		mGPSOverlay.addOverlay(currentPosition);
		
		// Then add the overlay list to the mapview
		vMapView.getOverlays().add(mGPSOverlay);
		// Refresh overlays
		vMapView.postInvalidate();

	}

	protected void getTrimetData(Location location) {
		String urlString = new String(getString(R.string.baseStopsSearchUrl)+ location.getLongitude() + "," + location.getLatitude());
		
		if (Connectivity.checkForInternetConnection(getApplicationContext()))
		{
			mDownloadNearbyStopsTask = new DownloadNearbyStopsDataTask(this);
			mDownloadNearbyStopsTask.execute(urlString);
		}
		else{
			Connectivity.showErrorToast(getApplicationContext());
		}
	}

	private void centerMapOnLocation(Location location) {
		MapController mapController = vMapView.getController();

		GeoPoint currentGeoPoint = getGeoPoint(location);
		mapController.setCenter(currentGeoPoint);
		mapController.setZoom(16);
	}

	@Override
	protected boolean isRouteDisplayed() {
		// Method required for this activity.
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public void setupStopsOverylay() {
		int length = mStopsDocument.lengthLocations();
		ArrayList<Route> routeList = new ArrayList<Route>();
		ArrayList<String> stopIDList = new ArrayList<String>();
		
		mStopOverlay.clearOverlays();
		
		for(int i = 0; i < length; i++){
			routeList.clear();
			GeoPoint geoPoint = getGeoPoint(mStopsDocument.getLat(i), mStopsDocument.getLon(i));
			String title = mStopsDocument.getDescription(i);
			String direction = mStopsDocument.getDirection(i);
			String stopID = mStopsDocument.getLocationID(i);
		
			for(int j = 0; j < mStopsDocument.lengthRoutes(i); j++){
				routeList.add(new Route(mStopsDocument.getRouteDesc(i, j), mStopsDocument.getRouteNumber(i, j)));
			}
			OverlayItem item = new OverlayItem(geoPoint, title, direction);
			
			// Add overlay item
			mStopOverlay.addOverlay(item, (ArrayList<Route>) routeList.clone(), stopID);
		}
		
		// Then add the overlay list to the mapview
		vMapView.getOverlays().add(mStopOverlay);
		vMapView.postInvalidate();
		
	}

	protected void showError(String error) {
		Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT)
				.show();
	}
	
	private void showGettingGPSDialog(){
		setupGettingGPSDialog();
		mGettingGPSDialog.show();
	}

	private void dismissGettingGPSDialog(){
		mGettingGPSDialog.dismiss();
	}
	
	private void setupGettingGPSDialog(){
		mGettingGPSDialog = null;
		mGettingGPSDialog = new ProgressDialog(this);
		mGettingGPSDialog.setMessage(getString(R.string.dialogFindingLocation));
		mGettingGPSDialog.setIndeterminate(true);
		mGettingGPSDialog.setCancelable(true);
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				mLocationHandler.stopLocationUpdates();
			}
		};
		
		mGettingGPSDialog.setOnCancelListener(onCancelListener);
	}
	
	private void showFindingStopsDialog(){
		setupFindingStopsDialog();
		mFindingStopsDialog.show();
	}

	private void dismissFindingStopsDialog(){
		mFindingStopsDialog.dismiss();
	}
	
	private void setupFindingStopsDialog(){
		mFindingStopsDialog = null;
		mFindingStopsDialog = new ProgressDialog(this);
		mFindingStopsDialog.setMessage(getString(R.string.dialogFindingStops));
		mFindingStopsDialog.setIndeterminate(true);
		mFindingStopsDialog.setCancelable(true);
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				mDownloadNearbyStopsTask.cancel(true);
			}
		};
		
		mFindingStopsDialog.setOnCancelListener(onCancelListener);
	}
	
	class DownloadNearbyStopsDataTask extends DownloadXMLAsyncTask<FindNearby> {

		public DownloadNearbyStopsDataTask(FindNearby activity) {
			super(activity);
		}

		protected void onPreExecute() {
			isDone = false;
			
			if (activity != null){
				activity.showFindingStopsDialog();
			}
			else{
				Log.w(TAG, "FindNearby activity is null");
			}
		}

		protected void onPostExecute(XMLHandler newXmlHandler) {
			isDone = true;
			
			if (activity != null){
				activity.dismissFindingStopsDialog();
				if (newXmlHandler.hasError())
					activity.showError(newXmlHandler.getError());
				else{
					NearbyStopsDocument.setXMLDoc(newXmlHandler.getXmlDoc());
				
					activity.setupStopsOverylay();
				}
			}
			else{
				Log.w(TAG, "FindNearby activity is null");
			}
		}
	}
}
