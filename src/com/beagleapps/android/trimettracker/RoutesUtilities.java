package com.beagleapps.android.trimettracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public final class RoutesUtilities {
	private static final String[] RoutesToExclude = {
		"999",
		"193",
		"98",
		"150"};
		
	// These two arrays match up
	private static final String[] NamedRoutes = { 
		"90",
		"100",
		"200",
		"190",
		"203",
		"193",
		"150",
		"98",
		"93"
	};
	
	private static final String[] RouteNames = { 
		"Red",
		"Blue",
		"Green",
		"Yellow",
		"WES",
		"Street Car",
		"Mall Shuttle",
		"Shuttle",
		"Vintage Trolley"
	};
	
	// Changes the elements in the routelist from the route number to the color
	// Also excludes certain route numbers
	// Flag "replaceNumbers" defaults to true. False means it just returns a list of the route numbers
	//		without parsing.
	public static String parseRouteList(ArrayList<String> routeList) {
		 ArrayList<String> newList = new ArrayList<String>();
		 
		 
		 for (int index = 0; index < routeList.size(); ++index){
			 String parsedRoute = parseRoute(routeList.get(index));
			 newList.add(parsedRoute);
		 }
		
		return join(newList, ", ");
	}
	
	public static String parseRoute(String routeNumber){
		String parsedRoute = routeNumber;
			
		if (!Arrays.asList(RoutesToExclude).contains(routeNumber)){
			 int location = Arrays.asList(NamedRoutes).lastIndexOf(routeNumber);
			 
			 // If named, grab the name
			 if (location >= 0){
				 parsedRoute = RouteNames[location];
			 }
			 // If not, just continue and return unchanged string			 
			 
		}
		
		return parsedRoute;
	}
	
	
	public static String join(ArrayList<String> list, String delimiter) {
        StringBuffer buffer = new StringBuffer();
        Iterator<String> iter = list.iterator();
        while (iter.hasNext()) {
            buffer.append(iter.next());
            if (iter.hasNext()) {
                buffer.append(delimiter);
            }
        }
        return buffer.toString();
    }
	
	public static boolean isExcludedRoute (String routeNumber) {
        return (Arrays.asList(RoutesToExclude).contains(routeNumber));
    }
}
