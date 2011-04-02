package com.beagleapps.android.trimettracker;

public class Favorite {
	private String description;
    private int stopID;
    private String direction;
    private String routes;
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	public void setStopID(int stopID) {
		this.stopID = stopID;
	}
	public int getStopID() {
		return stopID;
	}
	
	public void setDirection(String direction) {
		this.direction = direction;
	}
	public String getDirection() {
		return direction;
	}
	public void setRoutes(String routes) {
		this.routes = routes;
	}
	public String getRoutes() {
		return routes;
	}
}
