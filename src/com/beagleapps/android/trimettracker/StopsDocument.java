package com.beagleapps.android.trimettracker;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StopsDocument {

	public static Document mXMLDoc;
	
	public StopsDocument(Document routesDoc) {
		StopsDocument.mXMLDoc = routesDoc;
	}
	
	public StopsDocument() {
		// TODO Auto-generated constructor stub
	}

	public NodeList getRoutesNodes(){
		return mXMLDoc.getElementsByTagName("route");
	}
	
	public String getRouteDescription(int index){
		if (getRoutesNodes() != null){
			Node routeNode = getRoutesNodes().item(index);
	        return routeNode.getAttributes().getNamedItem("desc").getNodeValue();
		}
		else {
			return "";
		}
	}
	
	public String getStopsRouteDescription(){
		
		Node route = mXMLDoc.getElementsByTagName("route").item(0);
		
		if (route != null){
	        return route.getAttributes().getNamedItem("desc").getNodeValue();
		}
		else {
			return "";
		}
	}
	
	public String getRouteNumber(int index){
		if (getRoutesNodes() != null){
			Node routeNode = getRoutesNodes().item(index);
	        return routeNode.getAttributes().getNamedItem("route").getNodeValue();
		}
		else{
			return "";
		}
	}
}
