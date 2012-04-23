package com.beagleapps.android.trimettracker;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RoutesDocument {

	public static Document mRouteXMLDoc;
	public static Document mStopsXMLDoc;
	
	private NodeList mDirectionNodes = null;
	
	// Used when going from chooseDirection to chooseStop
	private static int chosenDirection;
	
	public RoutesDocument(Document routesDoc) {
		RoutesDocument.mRouteXMLDoc = routesDoc;
	}
	
	public RoutesDocument() {
		// TODO Auto-generated constructor stub
	}

	public NodeList getRoutesNodes(){
		return mRouteXMLDoc.getElementsByTagName("route");
	}
	
	public NodeList getDirectionNodes(){
		if (mDirectionNodes == null){
			mDirectionNodes = mStopsXMLDoc.getElementsByTagName("dir");
		}
		
		return mDirectionNodes;
	}
	
	public NodeList getStopNodes(int dir){
		Node dirNode = getDirectionNodes().item(dir);
		
		return dirNode.getChildNodes();
	}
	
	public String getDirDescription(int dir){
		if (getDirectionNodes() != null){
			Node dirNode = getDirectionNodes().item(dir);
	        return dirNode.getAttributes().getNamedItem("desc").getNodeValue();
		}
		else {
			return "";
		}
	}
	
	public String getStopDescription(int dir, int index){
		return getStopNodes(dir).item(index).getAttributes().getNamedItem("desc").getNodeValue();
	}
	
	public String getStopID(int dir, int index){
		return getStopNodes(dir).item(index).getAttributes().getNamedItem("locid").getNodeValue();
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
		
		Node route = mStopsXMLDoc.getElementsByTagName("route").item(0);
		
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

	public static int getChosenDirection() {
		return chosenDirection;
	}

	public static void setChosenDirection(int chosenDirection) {
		RoutesDocument.chosenDirection = chosenDirection;
	}
}
