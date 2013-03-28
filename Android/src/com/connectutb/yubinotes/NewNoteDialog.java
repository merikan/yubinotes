package com.connectutb.yubinotes;

import com.connectutb.yubinotes.util.DbManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
public class NewNoteDialog extends DialogFragment{
		
	EditText noteTitleView;
	EditText noteTextView;
	
	String noteTitle;
	String noteText;
	
	String folderId; 
	
	static NewNoteDialog newInstance(int num){
		NewNoteDialog nnd = new NewNoteDialog();
		
		//Supply num input as argument
		Bundle args = new Bundle();
		args.putInt("num", num);
		nnd.setArguments(args);
		
		return nnd;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        folderId = getArguments().getString("folderId");
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    View v = inflater.inflate(R.layout.new_note_dialog, null, false);
	    builder.setView(v)
	    // Add action buttons
	           .setPositiveButton("Create", new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                   // post comment
	            	   noteTitle = noteTitleView.getText().toString();
	            	   noteText = noteTextView.getText().toString();
	            	   createNote();
	            	   ListNotesActivity callingActivity = (ListNotesActivity) getActivity();
	                   callingActivity.onNoteCreation();
	               }
	           })
	           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   NewNoteDialog.this.getDialog().cancel();
	               }
	           });      

	    noteTitleView = (EditText) v.findViewById(R.id.textViewNewNoteTitle);
	    noteTextView = (EditText) v.findViewById(R.id.textViewNewNoteText);
	    return builder.create();
	}

	public void createNote(){
		//Database manager
		DbManager db = new DbManager(this.getActivity());
		db.addNote(noteTitle, noteText, folderId);
	}
}