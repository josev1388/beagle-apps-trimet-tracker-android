package com.beagleapps.android.trimettracker;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ArrivalAdapter extends ArrayAdapter<Arrival> {
	private final Activity context;
	private final ArrayList<Arrival> items;

	public ArrivalAdapter(Activity context, ArrayList<Arrival> items) {
		super(context, R.layout.arrivals_list_item, items);
		this.context = context;
		this.items = items;
	}

	// static to save the reference to the outer class and to avoid access to
	// any members of the containing class
	static class ViewHolder {
		public TextView left;
		public TextView right;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ViewHolder will buffer the assess to the individual fields of the row
		// layout

		ViewHolder holder;
		// Recycle existing view if passed as parameter
		// This will save memory and time on Android
		// This only works if the base layout for all classes are the same
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.arrivals_list_item, null, true);
			holder = new ViewHolder();
			holder.left = (TextView) rowView.findViewById(R.id.busDescription);
			holder.right = (TextView) rowView.findViewById(R.id.arrivalTime);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}

		holder.left.setText(items.get(position).getBusDescription());
		holder.right.setText(items.get(position).getRemainingMinutes());

		return rowView;
	}
}