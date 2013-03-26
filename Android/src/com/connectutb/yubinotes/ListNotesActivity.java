package com.connectutb.yubinotes;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;

public class ListNotesActivity extends ListActivity{
	
	private String[] notes = new String[0];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(new ListNotesListAdapter(this, notes));	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.notes, menu);
		return true;
	}
}
