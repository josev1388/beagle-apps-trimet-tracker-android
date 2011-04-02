package com.beagleapps.android.trimettracker;

import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ArrivalsDocument {

	public static Document arrivalsDoc;
	
	public ArrivalsDocument(Document arrivalsDoc) {
		ArrivalsDocument.arrivalsDoc = arrivalsDoc;
	}
	
	public int getStopID(){
		NodeList nodeList = arrivalsDoc.getElementsByTagName("location");
        
		Node locationNode = nodeList.item(0);
		String stopString = locationNode.getAttributes().getNamedItem("locid").getNodeValue();
		return Integer.parseInt(stopString);
	}
	
	public NodeList getArrivalNodes(){
		return arrivalsDoc.getElementsByTagName("arrival");    
	}
	
	public String getStopDescription(){
		NodeList nodeList = arrivalsDoc.getElementsByTagName("location");
	        
		Node locationNode = nodeList.item(0);
        return locationNode.getAttributes().getNamedItem("desc").getNodeValue();
	}
	
	public String getDirection(){
		NodeList nodeList = arrivalsDoc.getElementsByTagName("location");
        
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
}
