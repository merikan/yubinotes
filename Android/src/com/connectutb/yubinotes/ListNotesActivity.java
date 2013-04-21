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
import android.widget.RelativeLayout;
import android.widget.Toast;

public class ListNotesActivity extends ListActivity{
	
	//Database manager
	DbManager db = new DbManager(this);
	
	private String[][] notes;
	private String TAG = "YubiNotes";
	ListNotesListAdapter lnla;
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
		lnla = new ListNotesListAdapter(this, notes);
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		setListAdapter(lnla);	
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
    			updateListAdapter(folderId, mode);;	
    		}
    		return true;
    	//New Note
    	case R.id.action_new:
    		showNewNoteDialog();
    		return true;
    	//New Folder
    	case R.id.action_new_folder:
    		showNewFolderDialog();
    		return true;
		//Delete note
    	case R.id.action_delete:
    		deleteSelectedNotes();
    		return true;
    	case R.id.action_favorite:
    		favoriteSelectedNotes();
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
 
    
    private void deleteSelectedNotes(){
    	/** Loop through the notes and delete the entries that are checked **/

    	for (int i = 0; i < getListView().getLastVisiblePosition() + 1; i++){
			Object o = getListAdapter().getItem(i);
			String dirId = lnla.getNoteFolderId(i);
	    	CheckBox cbox = (CheckBox) ((View)getListView().getChildAt(i)).findViewById(R.id.checkBoxNoteSelect); 
	    	String noteId = o.toString();
	    		if( cbox.isChecked() ) { 
	    			if (mode==3 || settings.getBoolean("use_trash", true) == false ){
	    				db.deleteNotes(noteId,false);
	    				Toast.makeText(this, R.string.note_deleted, Toast.LENGTH_SHORT).show();
	    			}
	    			else{
	    				db.deleteNotes(noteId,true);
	    				Toast.makeText(this, R.string.note_trashed, Toast.LENGTH_SHORT).show();
	    			}
	    	    	notes = db.listNotes(dirId,mode);
	    		}
    	}
    	
    	//setListAdapter(new ListNotesListAdapter(this, notes));	
    	updateListAdapter("0", mode);
    }
    
    private void favoriteSelectedNotes(){
    	/** Loop through the notes and favorite the ones that are checked **/
    	String dirId = "0";
    	for (int i = 0; i < getListView().getLastVisiblePosition() + 1; i++){
			Object o = getListAdapter().getItem(i);
			dirId = lnla.getNoteFolderId(i);
	    	CheckBox cbox = (CheckBox) ((View)getListView().getChildAt(i)).findViewById(R.id.checkBoxNoteSelect); 
	    		if( cbox.isChecked() ) { 
	    			String noteId = o.toString();
	    			Log.d(TAG,noteId);
	    			db.setFavoriteNotes(noteId,true);
	    			Toast.makeText(this, R.string.note_starred, Toast.LENGTH_SHORT).show();
	    		}
    	}
    	updateListAdapter(dirId, mode);
    }
    
    public void unFavoriteNote(View v){
    	 final int position = getListView().getPositionForView((RelativeLayout)v.getParent());
    	 String dirId = "0";
         if (position >= 0) {
        	 dirId = lnla.getNoteFolderId(position);
        	 Log.d(TAG, lnla.getNoteId(position));
        	 String noteId = lnla.getNoteId(position);
        	 db.setFavoriteNotes(noteId,false);
        	 Toast.makeText(this, R.string.note_unstarred, Toast.LENGTH_SHORT).show();
         }
         
         updateListAdapter(dirId, mode);
    }
    
    public void updateListAdapter(String dirId, int mode){
    	notes = db.listNotes(dirId,mode);
    	lnla = new ListNotesListAdapter(this, notes);
        setListAdapter(lnla);	
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
    	super.onListItemClick(l, v, position, id);
    	//If we are in trash mode, dont do anything.. 
    	if (mode!=3){
	    	// We retrieve the item that was clicked
	    	Log.d(TAG, "Item clicked!");
			folderId = lnla.getNoteFolderId(position);
			String selectedNoteTitle = lnla.getNoteTitle(position);
			String selectedNoteText = lnla.getNoteText(position);
			int noteId = Integer.parseInt(lnla.getNoteId(position));
	        //Is it a folder or a note?
			Log.d(TAG, "TYPE: " + lnla.getNoteType(position));
	    	if (Integer.parseInt(lnla.getNoteType(position)) == 0){
	    		//its a folder, show the list of notes in that folder
	    		folderId = lnla.getNoteId(position);
	    		updateListAdapter(folderId, mode);
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
    	updateListAdapter(folderId, mode);
		notes = db.listNotes(folderId,mode);
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