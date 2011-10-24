package com.beagleapps.android.trimettracker;

public class Direction {
	private int mDir;
	private String mDesc;
	
	Direction(){
		
	}
	
	Direction(String desc, int dir){
		setDir(dir);
		setDesc(desc);
	}

	public int getDir() {
		return mDir;
	}

	public void setDir(int mDir) {
		this.mDir = mDir;
	}

	public String getDesc() {
		return mDesc;
	}

	public void setDesc(String mDesc) {
		this.mDesc = mDesc;
	}
}
