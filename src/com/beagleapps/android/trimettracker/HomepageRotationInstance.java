package com.beagleapps.android.trimettracker;

import com.beagleapps.android.trimettracker.MainView.DownloadArrivalDataTask;
import com.beagleapps.android.trimettracker.MainView.DownloadRoutesDataTask;



public class HomepageRotationInstance {
	private DownloadArrivalDataTask mDownloadArrivalTask = null;
	private DownloadRoutesDataTask mDownloadRoutesTask = null;
	
	public HomepageRotationInstance(DownloadArrivalDataTask mDownloadArrivalTask,
			DownloadRoutesDataTask mDownloadRoutesTask) {
		this.mDownloadArrivalTask = mDownloadArrivalTask;
		this.mDownloadRoutesTask = mDownloadRoutesTask;
	}
	
	public HomepageRotationInstance() {
		this.mDownloadArrivalTask = null;
		this.mDownloadRoutesTask = null;
	}

	public DownloadArrivalDataTask getDownloadArrivalTask() {
		return mDownloadArrivalTask;
	}
	
	public void setDownloadArrivalTask(DownloadArrivalDataTask mDownloadArrivalTask) {
		this.mDownloadArrivalTask = mDownloadArrivalTask;
	}
	
	public DownloadRoutesDataTask getDownloadRoutesTask() {
		return mDownloadRoutesTask;
	}
	
	public void setDownloadRoutesTask(DownloadRoutesDataTask mDownloadRoutesTask) {
		this.mDownloadRoutesTask = mDownloadRoutesTask;
	}
	

}
