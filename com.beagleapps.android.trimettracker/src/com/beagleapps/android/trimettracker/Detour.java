package com.beagleapps.android.trimettracker;

public class Detour {
	private String mRoutes;
	private String mDesc;
	
	Detour(){
		
	}
	
	Detour(String desc, String routes){
		setRoutes(routes);
		setDesc(desc);
	}
	

	public String getDesc() {
		return mDesc;
	}

	public void setDesc(String mDesc) {
		this.mDesc = mDesc;
	}


	public String getRoutes() {
		return mRoutes;
	}


	public void setRoutes(String mRoutes) {
		this.mRoutes = mRoutes;
	}
}
