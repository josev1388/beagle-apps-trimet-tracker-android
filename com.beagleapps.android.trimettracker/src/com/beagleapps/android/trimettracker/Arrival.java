package com.beagleapps.android.trimettracker;

public class Arrival {
	private String busDescription;
    private String arrivalTime;
    private String scheduledTime;
    private String remainingMinutes;
    
	public void setBusDescription(String busDescription) {
		this.busDescription = busDescription;
	}
	public String getBusDescription() {
		return busDescription;
	}
	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public String getArrivalTime() {
		return arrivalTime;
	}
	public void setScheduledTime(String scheduledTime) {
		this.scheduledTime = scheduledTime;
	}
	public String getScheduledTime() {
		return scheduledTime;
	}
	public void setRemainingMinutes(String remainingMinutes) {
		this.remainingMinutes = remainingMinutes;
	}
	public String getRemainingMinutes() {
		return remainingMinutes;
	}
   
}
