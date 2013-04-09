package com.connectutb.yubinotes;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MainListAdapter extends ArrayAdapter<String>{
	private final Activity context;
	private final String[] nav_items;

	public MainListAdapter(Activity context, String[] nav_items){
		super(context, R.layout.main_row_layout, nav_items);
		this.context = context;
		this.nav_items = nav_items;
	}
	
	static class ViewHolder{
		public TextView textViewTitle;
		public ImageView imageViewIcon;
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
			rowView = inflater.inflate(R.layout.main_row_layout, null, true);
			holder = new ViewHolder();
			holder.textViewTitle = (TextView) rowView.findViewById(R.id.textViewTitle);
			holder.imageViewIcon = (ImageView) rowView.findViewById(R.id.imageViewIcon);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}
		
		String[] arrayString = nav_items[position].split(";");
		int id = Integer.parseInt(arrayString[0]);
		holder.textViewTitle.setText(arrayString[1]);
		
		if (id==0){
			holder.imageViewIcon.setImageResource(R.drawable.collection);
		}
		else if(id==1){
			holder.imageViewIcon.setImageResource(R.drawable.recent);
		}
		else if(id==2){
			holder.imageViewIcon.setImageResource(R.drawable.star);
		}
		else if(id==3){
			holder.imageViewIcon.setImageResource(R.drawable.trash);
		}
		
		Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
		holder.textViewTitle.setTypeface(tf);
		return rowView;
	}
}