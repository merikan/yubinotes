package com.connectutb.yubinotes;

import com.connectutb.yubinotes.util.DbManager;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Menu;

public class ListNotesActivity extends ListActivity{
	
	//Database manager
	DbManager db = new DbManager(this);
	
	private String[] notes = new String[0];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		notes = db.listNotes();
		setListAdapter(new ListNotesListAdapter(this, notes));	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.notes, menu);
		return true;
	}
}
