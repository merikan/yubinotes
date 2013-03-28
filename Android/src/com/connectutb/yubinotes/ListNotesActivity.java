package com.connectutb.yubinotes;

import com.connectutb.yubinotes.util.DbManager;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ListNotesActivity extends ListActivity{
	
	//Database manager
	DbManager db = new DbManager(this);
	
	private String[] notes = new String[0];
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		notes = db.listNotes();
		setListAdapter(new ListNotesListAdapter(this, notes));	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.notes, menu);
		return true;
	}
	
	/* Action on menu selection */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
    	switch(item.getItemId()){
    	//Go home
    	case android.R.id.home:
    		Intent i = new Intent(this, MainActivity.class);
        	startActivity(i);	 
    		return true;
    	//New Note
    	case R.id.action_new:
    		showNewNoteDialog();
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    public void showNewNoteDialog(){
    	int mStackLevel = 1;

	    // DialogFragment.show() will take care of adding the fragment
	    // in a transaction.  We also want to remove any currently showing
	    // dialog, so make our own transaction and take care of that here.
	    FragmentTransaction ft = getFragmentManager().beginTransaction();
	    Fragment prev = getFragmentManager().findFragmentByTag("yubinotedialog");
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    ft.addToBackStack(null);
	    
	 // Supply num input as an argument.
        Bundle args = new Bundle();
	    // Create and show the dialog.
	    DialogFragment newFragment = NewNoteDialog.newInstance(mStackLevel);
	    newFragment.setArguments(args);
	    newFragment.show(ft, "yubinotedialog");
	}
}
