package com.beagleapps.android.trimettracker;

public class Route {
	private String mDesc;
	private String mNumber;
	
	Route(){
		
	}
	
	Route(String desc, String number){
		setDesc(desc);
		setNumber(number);
	}

	public String getDesc() {
		return mDesc;
	}

	public void setDesc(String mDesc) {
		this.mDesc = mDesc;
	}

	public String getNumber() {
		return mNumber;
	}

	public void setNumber(String mNumber) {
		this.mNumber = mNumber;
	}
}
