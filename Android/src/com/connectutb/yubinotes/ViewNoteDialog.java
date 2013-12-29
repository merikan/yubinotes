package com.connectutb.yubinotes;

import com.connectutb.yubinotes.util.DbManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ViewNoteDialog extends DialogFragment{

	private String TAG = "YubiNotes";
	
	TextView noteTitleView;
	EditText noteTextView;
	
	String noteTitle;
	String noteText;
	int noteId; 
	
	String folderId; 
	
	static ViewNoteDialog newInstance(int num){
		ViewNoteDialog vnd = new ViewNoteDialog();
		
		//Supply num input as argument
		Bundle args = new Bundle();
		args.putInt("num", num);
		vnd.setArguments(args);
		
		return vnd;
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        folderId = getArguments().getString("folderId");
        noteTitle = getArguments().getString("title");
        noteText = getArguments().getString("text");
        
        //TODO: Remove the added whitespaces at the end of the note
        while (noteText.endsWith(" ")){
        	noteText = noteText.substring(0,noteText.length()-1);
        }
       
        noteId = getArguments().getInt("noteId");
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    View v = inflater.inflate(R.layout.view_note_dialog, null, false);
	    builder.setView(v)
	    // Add action buttons
	           .setPositiveButton(R.string.save_note, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	                   // post comment
	            	   noteTitle = noteTitleView.getText().toString();
	            	   noteText = noteTextView.getText().toString();
	            	   if (noteText.length() < 1){
	            		   //add a space character so that crypto doesnt blow up
	            		   noteText = " ";
	            	   }
	            	   updateNote();
	            	   ListNotesActivity callingActivity = (ListNotesActivity) getActivity();
	                   callingActivity.onNoteCreation();
	               }
	           })
	           .setNegativeButton(R.string.note_cancel, new DialogInterface.OnClickListener() {
	               public void onClick(DialogInterface dialog, int id) {
	            	   ViewNoteDialog.this.getDialog().cancel();
	               }
	           });      
	    
	    noteTitleView = (TextView) v.findViewById(R.id.textViewViewNoteTitle);
	    noteTextView = (EditText) v.findViewById(R.id.textViewViewNoteText);
	    noteTitleView.setText(noteTitle);
	    noteTextView.setText(noteText);
	    Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
	    noteTitleView.setTypeface(tf);
	    noteTextView.setTypeface(tf);
	    return builder.create();
	}

	public void updateNote(){
		//Database manager
		DbManager db = new DbManager(this.getActivity());
		db.updateNote(noteTitle, noteText, noteId);
	}
}