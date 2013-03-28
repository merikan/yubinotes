package com.connectutb.yubinotes;

import com.connectutb.yubinotes.util.DbManager;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;

public class ListNotesActivity extends ListActivity{
	
	//Database manager
	DbManager db = new DbManager(this);
	
	private String[] notes = new String[0];
	private String TAG = "YubiNotes";
	
	public String folderId = "0";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		notes = db.listNotes("0");
		
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
    		return true;
		//Delete note
    	case R.id.action_delete:
    		deleteSelectedNotes();
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    private void deleteSelectedNotes(){
    	/** Loop through the notes and delete the entries that are checked **/

    	for (int i = 0; i < getListView().getLastVisiblePosition() + 1; i++){
			Object o = getListAdapter().getItem(i);
	    	CheckBox cbox = (CheckBox) ((View)getListView().getChildAt(i)).findViewById(R.id.checkBoxNoteSelect); 
	    		if( cbox.isChecked() ) { 
	    			String[] keywordArray = o.toString().split(";");
	    			db.deleteNotes(keywordArray[0]);
	    			Log.d(TAG, "DELETED: " + keywordArray[0]);
	    	    	notes = db.listNotes(keywordArray[3]);
	    		}
    	}
    	setListAdapter(new ListNotesListAdapter(this, notes));	
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
    	super.onListItemClick(l, v, position, id);
    	// We retrieve the item that was clicked
    	Object o = this.getListAdapter().getItem(position);
    	String keyword = o.toString();
    	String[] keywordArray = keyword.split(";");
        //Is it a folder or a note?
    	if (keywordArray[2].length() >= 1){
    		//its a folder, show the list of notes in that folder
    		folderId = keywordArray[3];
    		notes = db.listNotes(folderId);
    		setListAdapter(new ListNotesListAdapter(this, notes));	
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
        args.putString("folderId", folderId);
	    // Create and show the dialog.
	    DialogFragment newFragment = NewNoteDialog.newInstance(mStackLevel);
	    newFragment.setArguments(args);
	    newFragment.show(ft, "yubinotedialog");
	    Log.d(TAG, "READY?");
	}
    
    public void onNoteCreation() {
        // Refresh note list
		notes = db.listNotes(folderId);
		setListAdapter(new ListNotesListAdapter(this, notes));	
    }
}
