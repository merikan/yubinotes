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
import android.widget.ShareActionProvider;
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
	
	private ShareActionProvider mShareActionProvider;
	
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
	    if (mode==3){
	        menu.getItem(0).setEnabled(false);
	        menu.getItem(1).setEnabled(false);
	        menu.getItem(2).setEnabled(false);
	    }
	    
	 // Get the ActionProvider
	    mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.action_share_note)
	        .getActionProvider();
	    // Initialize the share intent
	    Intent intent = new Intent(Intent.ACTION_SEND);
	    intent.setType("text/plain");
	    mShareActionProvider.setShareIntent(intent);
	    
		return true;
	}
	
	private Intent createShareIntent(){
		/** Grab selected note(s) and create a nicely formatted output **/
		String selectedNoteTitle = "";
		String selectedNoteText = "";
		int count = 0;
		for (int i = 0; i < getListView().getLastVisiblePosition() + 1; i++){
			Object o = getListAdapter().getItem(i);
	    	CheckBox cbox = (CheckBox) ((View)getListView().getChildAt(i)).findViewById(R.id.checkBoxNoteSelect); 
	    		if( cbox.isChecked() ) { 
	    			
	    			if (count > 1){
	    				//If we are sharing more than one note, reorganize the output a bit..
	    				selectedNoteTitle = String.valueOf(count) + " " + getString(R.string.share_multiple_notes);
	    				selectedNoteText += System.getProperty("line.separator");
	    				selectedNoteText += "------------------" + System.getProperty("line.separator");
	    				selectedNoteText += lnla.getNoteText(i);
	    			}else{
	    			selectedNoteTitle = lnla.getNoteTitle(i);
	    			selectedNoteText = lnla.getNoteText(i);
	    			}
	    			//Increase selected counter
	    			count ++;
	    			Log.d(TAG, "Increasing sharing counter");
	    		}
    	}

		Intent I= new Intent(Intent.ACTION_SEND);
        I.setType("text/plain");
        I.putExtra(android.content.Intent.EXTRA_SUBJECT, R.string.share_title + " " + selectedNoteTitle);
        I.putExtra(android.content.Intent.EXTRA_TEXT, selectedNoteText);
        return I;
	}
	
	// Call to update the share intent
	@SuppressWarnings("unused")
	private void setShareIntent(Intent shareIntent) {
	    if (mShareActionProvider != null) {
	        mShareActionProvider.setShareIntent(shareIntent);
	    }
	}
	
	public void shareNote(){
		
		/** Grab selected note(s) and create a nicely formatted output **/
		String selectedNoteTitle = "";
		String selectedNoteText = "";
		int count = 0;
		for (int i = 0; i < getListView().getLastVisiblePosition() + 1; i++){
			Object o = getListAdapter().getItem(i);
	    	CheckBox cbox = (CheckBox) ((View)getListView().getChildAt(i)).findViewById(R.id.checkBoxNoteSelect); 
	    		if( cbox.isChecked() ) { 
	    			
	    			if (count >= 1){
	    				//If we are sharing more than one note, reorganize the output a bit..
	    				selectedNoteTitle = String.valueOf(count+1) + " " + getString(R.string.share_multiple_notes);
	    				selectedNoteText += System.getProperty("line.separator");
	    				selectedNoteText += "-----" + System.getProperty("line.separator");
	    				selectedNoteText += lnla.getNoteText(i);
	    			}else{
	    				selectedNoteTitle = lnla.getNoteTitle(i);
	    				selectedNoteText = lnla.getNoteText(i);
	    			}
	    			//Increase selected counter
	    			count ++;
	    		}
    	}
		if (count > 0){
		// Populate the share intent with data
	    Intent intent = new Intent(Intent.ACTION_SEND);
	    intent.setType("text/plain");
	    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.share_title) + " " + selectedNoteTitle);
	    intent.putExtra(android.content.Intent.EXTRA_TEXT, selectedNoteText);
	    mShareActionProvider.setShareIntent(intent);
		} else{
			Toast.makeText(this, R.string.share_no_selection, Toast.LENGTH_SHORT).show();
		}
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
    		return true;
    	case R.id.action_share_note:
    		shareNote();
    		return true;
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