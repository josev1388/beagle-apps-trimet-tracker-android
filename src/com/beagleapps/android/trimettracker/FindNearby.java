package com.beagleapps.android.trimettracker;

import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
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
	private StopItemizedOverlay mStopOverlay;
	private GPSMarkerItemizedOverylay mGPSOverlay;
	private DownloadNearbyStopsDataTask mDownloadNearbyStopsTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.findnearby);

		vMapView = (MapView) findViewById(R.id.FNBMapView);
		vMapView.setBuiltInZoomControls(true);

		setupMapOverylays();

		getGPSLocation();

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
				centerMapOnLocation(location);
				placeGPSMarker(location);

				getTrimetData(location);
			}
		};

		LocationHandler locationHandler = new LocationHandler();
		locationHandler.getLocation(this, mLocationResult);

	}

	protected void placeGPSMarker(Location location) {
		OverlayItem currentPosition = new OverlayItem(getGeoPoint(location), null, null);
		
		// Add overlay item
		mGPSOverlay.addOverlay(currentPosition);
		// Then add the overlay list to the mapview
		vMapView.getOverlays().add(mGPSOverlay);

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
		// TODO Auto-generated method stub
		return false;
	}

	protected void showError(String error) {
		Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT)
				.show();
	}
	
	class DownloadNearbyStopsDataTask extends DownloadXMLAsyncTask {
		private FindNearby activity = null;

		DownloadNearbyStopsDataTask(FindNearby activity) {
			attach(activity);
		}

		public boolean isDone() {
			return isDone;
		}

		public void attach(FindNearby activity) {
			this.activity = activity;
		}

		public void detach() {
			activity = null;
		}

		protected void onPreExecute() {
			isDone = false;
			
			if (activity != null){
				//activity.showRoutesDialog();
			}
			else{
				Log.w(TAG, "FindNearby activity is null");
			}
		}

		protected void onPostExecute(XMLHandler newXmlHandler) {
			isDone = true;
			
			if (activity != null){
				//activity.dismissRoutesDialog();
				if (newXmlHandler.hasError())
					activity.showError(newXmlHandler.getError());
				else{
					NearbyStopsDocument.mXMLDoc = newXmlHandler.getXmlDoc();
				
					//activity.launchChooseRoute();
				}
			}
			else{
				Log.w(TAG, "FindNearby activity is null");
			}
		}
	}

}