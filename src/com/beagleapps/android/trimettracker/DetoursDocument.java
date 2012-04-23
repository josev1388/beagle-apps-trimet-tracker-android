package com.beagleapps.android.trimettracker;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DetoursDocument {
	
	public static Document mXMLDoc;
	
	public DetoursDocument(Document detoursDoc) {
		DetoursDocument.mXMLDoc = detoursDoc;
	}
	
	public DetoursDocument() {
	}

	
	public NodeList getDetourNodes(){
		return mXMLDoc.getElementsByTagName("detour");
	}
	
	public Node getDetourNode(int index){
		return mXMLDoc.getElementsByTagName("detour").item(index);
	}
	
	
	public String getDetourDescription(int index){
		String description = null;
		NodeList detourNodes = getDetourNodes();
        
		Node detour = detourNodes.item(index);
		
		if(detour != null){
			description = detour.getAttributes().getNamedItem("desc").getNodeValue();
		}
        return description;
	}


	public int length() {
		return getDetourNodes().getLength();
	}

	// Gets the routes from a detour
	public String getRoutesString(int index) {
		ArrayList<String> routeList = new ArrayList<String>();
		
		NodeList routeNodes = getRoutes(index);
		
		for (int i=0; i < routeNodes.getLength(); i++){
			routeList.add(routeNodes.item(i).getAttributes().getNamedItem("route").getNodeValue());
		}
		
		return RoutesUtilities.parseRouteList(routeList);
	}

	private NodeList getRoutes(int index) {
		return getDetourNode(index).getChildNodes();
	}
}
