package com.beagleapps.android.trimettracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public final class RoutesUtilities {
	private static final String[] RoutesToExclude = {
		"999",
		"93",
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
	public static String parseRouteList(ArrayList<String> routeList) {
		 ArrayList<String> newList = new ArrayList<String>();
		 
		 
		 for (int index = 0; index < routeList.size(); ++index){
			 if (!Arrays.asList(RoutesToExclude).contains(routeList.get(index))){
				 int location = Arrays.asList(NamedRoutes).lastIndexOf(routeList.get(index));
				 
				 //If named, grab the name
				 if (location >= 0){
					 newList.add(RouteNames[location]);
				 }
				 // If not, just continue
				 else{
					 newList.add(routeList.get(index));
				 }
				 
			 }
		 }
		
		return join(newList, ", ");
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
}
