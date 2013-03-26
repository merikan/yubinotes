package com.connectutb.yubinotes;

import android.app.Activity;
import android.widget.ArrayAdapter;

public class ListNotesListAdapter extends ArrayAdapter<String>{
	private final Activity context;
	private final String[] noteslist;


	public ListNotesListAdapter(Activity context, String[] noteslist){
		super(context, R.layout.main_row_layout, noteslist);
		this.context = context;
		this.noteslist = noteslist;
	}
	
}