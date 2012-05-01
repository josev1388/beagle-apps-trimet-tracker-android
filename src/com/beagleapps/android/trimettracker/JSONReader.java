package com.beagleapps.android.trimettracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;

public class JSONReader {
	
	public static String readAsset(String asset, Activity activity) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(activity.getAssets()
					.open(asset)));
			
			String line;
			StringBuilder buffer = new StringBuilder();
			
			while ((line = in.readLine()) != null) {
				buffer.append(line).append('\n');
			}
			
			return buffer.toString();
			
		} 
		
		catch (IOException e) {
			return "";
		}
		
		finally {

			try {
				in.close();
			} catch (IOException e) {

			}
		}
	}
}
