package com.beagleapps.android.trimettracker;

public class Preference {
	public String name;
	public int value;
	public String description;
	
	Preference(){
		
	}
	
	Preference(String name, int value, String description){
		this.name = name;
		this.value = value;
		this.description = description;
	}
}
