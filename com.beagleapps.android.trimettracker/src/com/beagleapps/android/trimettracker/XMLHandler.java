package com.beagleapps.android.trimettracker;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XMLHandler {

	private Document document;
	private URL url;
	private long mRequestTime;
	
	public XMLHandler(String newUrl) throws MalformedURLException {
		url = new URL(newUrl);
		
	}
	
	public void refreshXmlData() 
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
			document = db.parse(new InputSource(url.openStream()));
			document.getDocumentElement().normalize();
			mRequestTime = new Date().getTime();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Document getXmlDoc() 
	{
		return this.document;
	}
	
	public boolean hasError(){
		boolean hasError = true;
		if (document != null){
			NodeList nodeList = document.getElementsByTagName("errorMessage");
			if (nodeList.getLength() <= 0)
				hasError = false;
		}
		
		return hasError;
	}
	
	public String getError(){
		String errorMessage = null;
		if (document != null){
			NodeList nodeList = document.getElementsByTagName("errorMessage");
			if (nodeList.getLength() <= 0)
				errorMessage = "No Error";
			else{
				Element errorElement = (Element) nodeList.item(0);
				NodeList errorNodeList = errorElement.getChildNodes();
		        errorMessage = errorNodeList.item(0).getNodeValue();
			}
		}
		else{
			errorMessage = "Error: Problem getting data";
		}
		
		return errorMessage;
	}

	public long getRequestTime() {
		return mRequestTime;
	}
}
