package com.beagleapps.android.trimettracker;

import org.w3c.dom.Node;

public class XMLDocument {

	protected String getAttributeValue(Node node, String itemName) {
		String attributeString = "";
		if(node != null){
			attributeString = node.getAttributes().getNamedItem(itemName).getNodeValue();
		}
		return attributeString;
	}
}
