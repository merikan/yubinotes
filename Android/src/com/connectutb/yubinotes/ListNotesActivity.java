package com.connectutb.yubinotes;

import com.connectutb.yubinotes.util.DbManager;

import android.app.ActionBar;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

public class ListNotesActivity extends ListActivity{
	
	//Database manager
	DbManager db = new DbManager(this);
	
	private String[] notes = new String[0];
	private String TAG = "YubiNotes";
	
	public SharedPreferences settings;
	
	public String folderId = "0";
	public int mode = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
	    mode = getIntent().getIntExtra("mode", 0);
		notes = db.listNotes("0",(int)mode);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
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
    		//Return to MainActivity if folderId = 0, else return to folderId = 0
    		if (Integer.parseInt(folderId)==0){
    		Intent i = new Intent(this, MainActivity.class);
        	startActivity(i);
    		}else{
    			folderId = "0";
    			notes = db.listNotes(folderId, mode);
        		setListAdapter(new ListNotesListAdapter(this, notes));	
    		}
    		return true;
    	//New Note
    	case R.id.action_new:
    		showNewNoteDialog();
    		return true;
    	//New Folder
    	case R.id.action_new_folder:
    		showNewFolderDialog();
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
	    			if (mode==3 || settings.getBoolean("use_trash", true) == false ){
	    				db.deleteNotes(keywordArray[0],false);
	    				Toast.makeText(this, R.string.note_deleted, Toast.LENGTH_SHORT).show();
	    			}
	    			else{
	    				db.deleteNotes(keywordArray[0],true);
	    				Toast.makeText(this, R.string.note_trashed, Toast.LENGTH_SHORT).show();
	    			}
	    	    	notes = db.listNotes(keywordArray[3],mode);
	    		}
    	}
    	setListAdapter(new ListNotesListAdapter(this, notes));	
    }
    
    private void favoriteSelectedNotes(){
    	
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
    	super.onListItemClick(l, v, position, id);
    	// We retrieve the item that was clicked
    	Object o = this.getListAdapter().getItem(position);
    	String keyword = o.toString();
    	String[] keywordArray = keyword.split(";");
    	
		folderId = keywordArray[3];
		String selectedNoteTitle = keywordArray[1];
		String selectedNoteText = keywordArray[2];
		int noteId = Integer.parseInt(keywordArray[0]);
        //Is it a folder or a note?
		Log.d(TAG, "TYPE: " + keywordArray[5]);
    	if (Integer.parseInt(keywordArray[5]) == 0){
    		//its a folder, show the list of notes in that folder
    		folderId = keywordArray[0];
    		notes = db.listNotes(folderId, mode);
    		setListAdapter(new ListNotesListAdapter(this, notes));	
    	}else{
    		//Show the note
    		int mStackLevel = 1;

    	    // DialogFragment.show() will take care of adding the fragment
    	    // in a transaction.  We also want to remove any currently showing
    	    // dialog, so make our own transaction and take care of that here.
    	    FragmentTransaction ft = getFragmentManager().beginTransaction();
    	    Fragment prev = getFragmentManager().findFragmentByTag("yubinotesviewdialog");
    	    if (prev != null) {
    	        ft.remove(prev);
    	    }
    	    ft.addToBackStack(null);
    	    
    	    // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putString("folderId", folderId);
            args.putString("title", selectedNoteTitle);
            args.putString("text",selectedNoteText);
            args.putInt("noteId", noteId);
    	    // Create and show the dialog.
    	    DialogFragment newFragment = ViewNoteDialog.newInstance(mStackLevel);
    	    newFragment.setArguments(args);
    	    newFragment.show(ft, "yubinotesviewdialog");
    	}
    }
    
    public void showNewFolderDialog(){
    	int mStackLevel = 1;

	    // DialogFragment.show() will take care of adding the fragment
	    // in a transaction.  We also want to remove any currently showing
	    // dialog, so make our own transaction and take care of that here.
	    FragmentTransaction ft = getFragmentManager().beginTransaction();
	    Fragment prev = getFragmentManager().findFragmentByTag("yubinotefolderdialog");
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    ft.addToBackStack(null);
	    
	    // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putString("folderId", folderId);
	    // Create and show the dialog.
	    DialogFragment newFragment = NewFolderDialog.newInstance(mStackLevel);
	    newFragment.setArguments(args);
	    newFragment.show(ft, "yubinotefolderdialog");
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
	}
    
    public void onNoteCreation() {
        // Refresh note list
		notes = db.listNotes(folderId,mode);
		Log.d(TAG, "Showing notes with folderId " + folderId + " and mode " + mode);
		setListAdapter(new ListNotesListAdapter(this, notes));	
    }
    
    @Override
    protected void onStop() {
        setResult(2);
        super.onStop();
    }
    @Override
    protected void onDestroy() {
        setResult(2);
        super.onDestroy();
    }
}