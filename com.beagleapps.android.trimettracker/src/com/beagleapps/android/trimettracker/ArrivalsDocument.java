package com.beagleapps.android.trimettracker;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ArrivalsDocument {

	private static final String[] DaysOfWeek = 
	{"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
	
	public static Document mArrivalsDoc;
	public static long mRequestTime;
	
	public ArrivalsDocument(Document arrivalsDoc, long requestTime) {
		ArrivalsDocument.mArrivalsDoc = arrivalsDoc;
		mRequestTime = requestTime;
	}
	
	public int getStopID(){
		NodeList nodeList = mArrivalsDoc.getElementsByTagName("location");
        
		Node locationNode = nodeList.item(0);
		String stopString = locationNode.getAttributes().getNamedItem("locid").getNodeValue();
		return Integer.parseInt(stopString);
	}
	
	public NodeList getArrivalNodes(){
		return mArrivalsDoc.getElementsByTagName("arrival");    
	}
	
	public String getStopDescription(){
		NodeList nodeList = mArrivalsDoc.getElementsByTagName("location");
	        
		Node locationNode = nodeList.item(0);
        return locationNode.getAttributes().getNamedItem("desc").getNodeValue();
	}
	
	public String getDirection(){
		NodeList nodeList = mArrivalsDoc.getElementsByTagName("location");
        
		Node locationNode = nodeList.item(0);
        return locationNode.getAttributes().getNamedItem("dir").getNodeValue();
	}
	
	public String getBusDescription(int index){
		String description = null;
		NodeList arrivalNodes = getArrivalNodes();
        
		Node arrival = arrivalNodes.item(index);
		
		if(arrival != null){
			description = arrival.getAttributes().getNamedItem("shortSign").getNodeValue();
		}
        return description;
	}
	
	public String getScheduledTime(int index){
		String scheduledTime = null;
        
		Node arrival = getArrivalNodes().item(index);
		
		if(arrival != null){
			scheduledTime = arrival.getAttributes().getNamedItem("scheduled").getNodeValue();
		}
        return scheduledTime;
	}
	
	// Returns the time of the request in milliseconds since epoch
	public String getRequestTime(){
        Node resultSet = mArrivalsDoc.getElementsByTagName("resultSet").item(0);
		String requestTime = null;
		
		if(resultSet != null){
			requestTime = resultSet.getAttributes().getNamedItem("queryTime").getNodeValue();
		}
        return requestTime;
	}
	
	public String getScheduledTimeText(int index){
		String timeText = null;
        
		Node arrival = getArrivalNodes().item(index);
		
		if(arrival != null){
			long unixTime = Long.parseLong(arrival.getAttributes().getNamedItem("scheduled").getNodeValue());
			
			timeText = getReadableTime(unixTime);
		}
        return timeText;
	}
	
	private String getReadableTime(long unixTime) {
		Date arrivalTime = new Date(unixTime);
		Date now = new Date();
		String timeString;
		timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(arrivalTime);
		
		if(now.getDay() != arrivalTime.getDay()){
			timeString = timeString + ", " + DaysOfWeek[arrivalTime.getDay()];
		}
		
		return timeString;
	}

	public String getRemainingMinutes(int index) {
		Date currentTime = new Date();
		String epochTimeString, timeLeftString;
		long epochTimeLong, timeLeftLong;
		
		
		if(getArrivalNodes().item(index) != null){
			if (isEstimated(index))
				epochTimeString = getEstimatedTime(index);
			else
				epochTimeString = getScheduledTime(index);
			
			epochTimeLong = Long.parseLong(epochTimeString);
			
			timeLeftLong = ((epochTimeLong - currentTime.getTime())/1000)/60;
			
			timeLeftString = Long.toString(timeLeftLong);
		}
		else{
			timeLeftString = "Error";
		}
		
		return timeLeftString;
	}

	public String getEstimatedTime(int index){
		String estimatedTime = null;
		NodeList arrivalNodes = getArrivalNodes();
        
		Node arrival = arrivalNodes.item(index);
		
		if(arrival != null){
			estimatedTime = arrival.getAttributes().getNamedItem("estimated").getNodeValue();
		}
        return estimatedTime;
	}
	
	public boolean isEstimated(int index){
		Node arrival = getArrivalNodes().item(index);
		String status = arrival.getAttributes().getNamedItem("status").getNodeValue();
		
        return status.compareTo("estimated") == 0;
        
	}

	public int getNumArrivals() {
		return getArrivalNodes().getLength();
	}
	
	public ArrayList<String> getRouteList(){
		ArrayList<String> busRouteList = new ArrayList<String>();
		
		for (int index = 0; index < this.getArrivalNodes().getLength(); ++index) {
			String routeNumber = 
				getArrivalNodes().item(index).getAttributes().getNamedItem("route").getNodeValue();;
			
			if (busRouteList.lastIndexOf((routeNumber)) < 0){
				busRouteList.add(routeNumber);
			}
		}
		
		Collections.sort(busRouteList);
		
		return busRouteList;
	}

	// Returns number of seconds since the time of the request
	public int getAge() {
		return (int) ((new Date().getTime() - mRequestTime)/1000);
	}
}
