package com.connectutb.yubinotes;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListNotesListAdapter extends ArrayAdapter<String>{
	private final Activity context;
	private final String[] noteslist;


	public ListNotesListAdapter(Activity context, String[] noteslist){
		super(context, R.layout.list_row_layout, noteslist);
		this.context = context;
		this.noteslist = noteslist;
	}
	static class ViewHolder{
		public TextView textViewTitle;
		public TextView textViewTimestamp;
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
			rowView = inflater.inflate(R.layout.list_row_layout, null, true);
			holder = new ViewHolder();
			holder.textViewTitle = (TextView) rowView.findViewById(R.id.textViewNoteTitle);
			holder.textViewTimestamp = (TextView) rowView.findViewById(R.id.textViewNoteDate);
			holder.imageViewIcon = (ImageView) rowView.findViewById(R.id.imageViewNoteIcon);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}
		
		Log.d("YubiNotes",noteslist[position]);
		String[] arrayString = noteslist[position].split(";");
		holder.textViewTitle.setText(arrayString[1]);
		holder.textViewTimestamp.setText(arrayString[4]);
		
		String noteText = arrayString[2];
		// Set appropriate icon 
		int noteType = Integer.parseInt(arrayString[5]);
		if (noteType==1){
			holder.imageViewIcon.setImageResource(R.drawable.note);
		}else{
			holder.imageViewIcon.setImageResource(R.drawable.collection);
		}
			
		return rowView;
	}
}