package com.connectutb.yubinotes;

import android.app.Activity;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ListNotesListAdapter extends ArrayAdapter<String>{
	private static final String TAG ="YubiNotes";
	private final Activity context;
	private final String[][] notesList;


	public ListNotesListAdapter(Activity context, String[][] notesList){
		super(context, R.layout.list_row_layout);
		this.context = context;
		this.notesList = notesList;
	}
	static class ViewHolder{
		public TextView textViewTitle;
		public TextView textViewTimestamp;
		public ImageView imageViewIcon;
		public Button buttonFav;
	}
	
	@Override
	public int getCount(){
		
		return notesList.length;
	}
	
	@Override
    public String getItem(int i) {   
		String[] noteList = notesList[i];
		return noteList[0];    
    }
	
	public String getNoteId(int i){
		String[] noteList = notesList[i];
		return noteList[0];  
	}
	
	public String getNoteTitle(int i){
		String[] noteList = notesList[i];
		return noteList[1];  
	}
	
	public String getNoteText(int i){
		String[] noteList = notesList[i];
		return noteList[2];  
	}
	
	public String getNoteFolderId(int i){
		String[] noteList = notesList[i];
		return noteList[3];  
	}
	
	public String getNoteType(int i){
		String[] noteList = notesList[i];
		return noteList[9];  
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
			holder.buttonFav = (Button) rowView.findViewById(R.id.buttonNoteFav);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}
		
		
		String[] arrayString = notesList[position];
		holder.textViewTitle.setText(arrayString[1]);
		holder.textViewTimestamp.setText(arrayString[5]);
		int isFavorite = Integer.parseInt(arrayString[8]);
		if (isFavorite==0){
			holder.buttonFav.setVisibility(View.GONE);
		}
		
		String noteText = arrayString[2];
		// Set appropriate icon 
		int noteType = Integer.parseInt(arrayString[9]);
		if (noteType==1){
			holder.imageViewIcon.setImageResource(R.drawable.note);
		}else{
			holder.imageViewIcon.setImageResource(R.drawable.collection);
		}
		
		Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
		holder.textViewTitle.setTypeface(tf);
		holder.textViewTimestamp.setTypeface(tf);

		return rowView;
	}
}