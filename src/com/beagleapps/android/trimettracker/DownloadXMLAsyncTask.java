package com.beagleapps.android.trimettracker;

import java.net.MalformedURLException;

import android.os.AsyncTask;
import android.util.Log;

public abstract class DownloadXMLAsyncTask extends AsyncTask<String, Void, XMLHandler> {

	protected boolean isDone = false;
	protected final String TAG = "DownloadXMLAsyncTask";

	DownloadXMLAsyncTask() {
		
	}

	public boolean isDone() {
		return isDone;
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

	// These two methods are implemented by each view that uses this class.
	abstract protected void onPreExecute();

	abstract protected void onPostExecute(XMLHandler newXmlHandler);
	
	@Override
    protected void onCancelled() {
        isDone = true;
    }
}

